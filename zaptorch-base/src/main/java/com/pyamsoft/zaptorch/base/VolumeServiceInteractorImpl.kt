/*
 * Copyright 2019 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.pyamsoft.zaptorch.base

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.KeyEvent
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pyamsoft.pydroid.arch.EventConsumer
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.api.CameraInterface
import com.pyamsoft.zaptorch.api.CameraInterface.CameraError
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class VolumeServiceInteractorImpl @Inject internal constructor(
  private val enforcer: Enforcer,
  private val context: Context,
  private val preferences: CameraPreferences,
  torchOffServiceClass: Class<out IntentService>,
  @ColorRes notificationColor: Int
) : VolumeServiceInteractor {

  private val mutex = Mutex()

  private val notificationManagerCompat = NotificationManagerCompat.from(context)
  private val notification: Notification

  private var pressed: Boolean = false

  private var cameraInterface: CameraInterface? = null

  @ExperimentalCoroutinesApi
  private val cameraErrorBus = BroadcastChannel<CameraError>(1)

  private var running = false

  @ExperimentalCoroutinesApi
  private val runningStateBus = BroadcastChannel<Boolean>(1)

  init {
    val intent = Intent(context, torchOffServiceClass)

    val notificationChannelId = "zaptorch_foreground"
    if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
      setupNotificationChannel(notificationChannelId)
    }

    val pendingIntent =
      PendingIntent.getService(context, NOTIFICATION_RC, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    notification = NotificationCompat.Builder(context, notificationChannelId)
        .apply {
          setContentIntent(pendingIntent)
          setContentTitle("Torch is On")
          setContentText("Click to turn off")
          setSmallIcon(R.drawable.ic_light_notification)
          setAutoCancel(true)
          setWhen(0)
          setOngoing(false)
          color = ContextCompat.getColor(context, notificationColor)
          priority = NotificationCompat.PRIORITY_DEFAULT
        }
        .build()

    pressed = false
  }

  @RequiresApi(VERSION_CODES.O)
  private fun setupNotificationChannel(
    notificationChannelId: String
  ) {
    val name = "Torch Service"
    val desc = "Notification related to the ZapTorch service"
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val notificationChannel = NotificationChannel(notificationChannelId, name, importance).apply {
      lockscreenVisibility = Notification.VISIBILITY_PUBLIC
      description = desc
      enableLights(false)
      enableVibration(false)
    }

    Timber.d("Create notification channel with id: %s", notificationChannelId)
    val notificationManager: NotificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(notificationChannel)
  }

  @ExperimentalCoroutinesApi
  override fun setServiceState(changed: Boolean) {
    running = changed
    if (!runningStateBus.offer(changed)) {
      Timber.w("Failed to offer service state change: $changed")
    }
  }

  @ExperimentalCoroutinesApi
  override fun observeServiceState(): EventConsumer<Boolean> {
    return object : EventConsumer<Boolean> {

      override suspend fun onEvent(emitter: suspend (event: Boolean) -> Unit) {
        emitter(running)

        runningStateBus.openSubscription()
            .consumeEach { emitter(it) }
      }

    }
  }

  @ExperimentalCoroutinesApi
  override fun observeCameraState(): EventConsumer<CameraError> {
    return object : EventConsumer<CameraError> {

      override suspend fun onEvent(emitter: suspend (event: CameraError) -> Unit) {
        cameraErrorBus.openSubscription()
            .consumeEach { emitter(it) }
      }

    }
  }

  override suspend fun handleKeyPress(
    action: Int,
    keyCode: Int,
    onError: suspend (error: CameraAccessException?) -> Unit
  ) = coroutineScope {
    if (action != KeyEvent.ACTION_UP) {
      return@coroutineScope
    }
    if (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
      return@coroutineScope
    }

    enforcer.assertNotOnMainThread()
    if (pressed) {
      mutex.withLock {
        Timber.d("Key has been double pressed")
        pressed = false
        toggleTorch(onError)
      }
    } else {
      mutex.withLock {
        pressed = true
      }

      launch {
        delay(preferences.buttonDelayTime)
        mutex.withLock {
          if (pressed) {
            Timber.d("Set pressed back to false")
            pressed = false
          }
        }
      }
    }
  }

  @ExperimentalCoroutinesApi
  override fun setupCamera() {
    val camera: CameraCommon = MarshmallowCamera(context, enforcer, preferences).apply {
      setOnStateChangedCallback(object : CameraInterface.OnStateChangedCallback {
        override fun onOpened() {
          notificationManagerCompat.notify(NOTIFICATION_ID, notification)
        }

        override fun onClosed() {
          notificationManagerCompat.cancel(NOTIFICATION_ID)
        }

        override fun onError(error: CameraError) {
          if (!cameraErrorBus.offer(error)) {
            Timber.w("Unable to offer camera error event: $error")
          }
        }
      })
    }

    releaseCamera()
    cameraInterface = camera
  }

  override suspend fun toggleTorch(onError: suspend (error: CameraAccessException?) -> Unit) {
    coroutineScope {
      cameraInterface?.toggleTorch(onError)
    }
  }

  override fun releaseCamera() {
    cameraInterface?.also {
      it.destroy()
      it.setOnStateChangedCallback(null)
    }
    cameraInterface = null
  }

  override fun showError(error: CameraAccessException?) {
    cameraInterface?.showError(error)
  }

  companion object {

    private const val NOTIFICATION_ID = 1345
    private const val NOTIFICATION_RC = 1009
  }
}
