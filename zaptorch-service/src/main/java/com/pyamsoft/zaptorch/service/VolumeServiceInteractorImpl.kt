/*
 * Copyright (C) 2018 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.zaptorch.service

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import com.pyamsoft.zaptorch.api.CameraInterface
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import io.reactivex.Scheduler
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS

internal class VolumeServiceInteractorImpl internal constructor(
  private val context: Context,
  private val preferences: CameraPreferences,
  torchOffServiceClass: Class<out IntentService>
) : VolumeServiceInteractor {

  private val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(
      context
  )
  private val notification: Notification
  private val onStateChangedCallback: CameraInterface.OnStateChangedCallback
  private var pressed: Boolean = false
  private var cameraInterface: CameraInterface? = null

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
          color = ContextCompat.getColor(context, R.color.purple500)
          priority = NotificationCompat.PRIORITY_DEFAULT
        }
        .build()

    onStateChangedCallback = object : CameraInterface.OnStateChangedCallback {
      override fun onOpened() {
        notificationManagerCompat.notify(NOTIFICATION_ID, notification)
      }

      override fun onClosed() {
        notificationManagerCompat.cancel(NOTIFICATION_ID)
      }

      override fun onError(errorIntent: Intent) {
        throw RuntimeException("Not handled!")
      }
    }

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

  override fun handleKeyPress(
    action: Int,
    keyCode: Int
  ): Single<Long> {
    var keyPressSingle = Single.never<Long>()
    if (action == KeyEvent.ACTION_UP) {
      if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
        if (pressed) {
          Timber.d("Key has been double pressed")
          pressed = false
          toggleTorch()
        } else {
          pressed = true
          keyPressSingle = Single.timer(preferences.buttonDelayTime, MILLISECONDS)
              .doOnSuccess {
                Timber.d("Set pressed back to false")
                pressed = false
              }
        }
      }
    }

    return keyPressSingle
  }

  override fun shouldShowErrorDialog(): Single<Boolean> =
    Single.fromCallable { preferences.shouldShowErrorDialog() }

  override fun setupCamera(
    computationScheduler: Scheduler,
    mainThreadScheduler: Scheduler,
    onCameraError: (Intent) -> Unit
  ) {
    val camera: CameraCommon
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      // Assign
      camera = MarshmallowCamera(
          context, this, computationScheduler,
          mainThreadScheduler
      )
    } else {
      // Assign
      camera = LollipopCamera(
          context, this, computationScheduler,
          mainThreadScheduler
      )
    }

    camera.setOnStateChangedCallback(object : CameraInterface.OnStateChangedCallback {
      override fun onOpened() {
        onStateChangedCallback.onOpened()
      }

      override fun onClosed() {
        onStateChangedCallback.onClosed()
      }

      override fun onError(errorIntent: Intent) {
        onCameraError(errorIntent)
      }
    })

    releaseCamera()
    cameraInterface = camera
  }

  override fun toggleTorch() {
    cameraInterface?.toggleTorch()
  }

  override fun releaseCamera() {
    val obj = cameraInterface
    if (obj != null) {
      obj.destroy()
      obj.setOnStateChangedCallback(null)
      cameraInterface = null
    }
  }

  companion object {

    private const val NOTIFICATION_ID = 1345
    private const val NOTIFICATION_RC = 1009
  }
}
