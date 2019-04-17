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
import android.os.Build
import android.os.Build.VERSION_CODES
import android.view.KeyEvent
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.pydroid.core.threads.Enforcer
import com.pyamsoft.zaptorch.api.CameraInterface
import com.pyamsoft.zaptorch.api.CameraInterface.CameraError
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import io.reactivex.Observable
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
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

  private val notificationManagerCompat = NotificationManagerCompat.from(context)
  private val notification: Notification

  private var pressed: Boolean = false

  private var cameraInterface: CameraInterface? = null
  private val cameraErrorBus = RxBus.create<CameraError>()

  private var running = false
  private val runningStateBus = RxBus.create<Boolean>()

  init {
    val intent = Intent(context, torchOffServiceClass)

    val notificationChannelId = "zaptorch_foreground"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

  override fun setServiceState(changed: Boolean) {
    running = changed
    runningStateBus.publish(changed)
  }

  override fun observeServiceState(): Observable<Boolean> {
    return Observable.defer {
      enforcer.assertNotOnMainThread()
      return@defer runningStateBus.listen()
          .startWith(running)
    }
  }

  override fun observeCameraState(): Observable<CameraError> {
    return Observable.defer {
      enforcer.assertNotOnMainThread()
      return@defer cameraErrorBus.listen()
    }
  }

  override fun handleKeyPress(
    action: Int,
    keyCode: Int
  ): Single<Long> {
    return Single.defer {
      enforcer.assertNotOnMainThread()
      var keyPressSingle: Single<Long> = Single.never<Long>()
      if (action == KeyEvent.ACTION_UP) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
          var result: Single<Long>? = null
          if (pressed) {
            Timber.d("Key has been double pressed")
            pressed = false
            toggleTorch()
          } else {
            pressed = true
            result = Single.timer(preferences.buttonDelayTime, MILLISECONDS)
                .doOnSuccess {
                  Timber.d("Set pressed back to false")
                  pressed = false
                }
          }

          if (result != null) {
            keyPressSingle = result
          }
        }
      }

      return@defer keyPressSingle
    }
  }

  override fun shouldShowErrorDialog(): Single<Boolean> = Single.fromCallable {
    enforcer.assertNotOnMainThread()
    return@fromCallable preferences.shouldShowErrorDialog()
  }

  override fun setupCamera() {
    val camera: CameraCommon = MarshmallowCamera(
        context, this
    )
        .apply {
          setOnStateChangedCallback(object : CameraInterface.OnStateChangedCallback {
            override fun onOpened() {
              notificationManagerCompat.notify(NOTIFICATION_ID, notification)
            }

            override fun onClosed() {
              notificationManagerCompat.cancel(NOTIFICATION_ID)
            }

            override fun onError(error: CameraError) {
              cameraErrorBus.publish(error)
            }
          })
        }

    releaseCamera()
    cameraInterface = camera
  }

  override fun toggleTorch() {
    cameraInterface?.toggleTorch()
  }

  override fun releaseCamera() {
    cameraInterface?.also {
      it.destroy()
      it.setOnStateChangedCallback(null)
    }
    cameraInterface = null
  }

  companion object {

    private const val NOTIFICATION_ID = 1345
    private const val NOTIFICATION_RC = 1009
  }
}
