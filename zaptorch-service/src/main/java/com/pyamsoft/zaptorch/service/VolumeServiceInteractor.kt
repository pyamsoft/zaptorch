/*
 * Copyright 2017 Peter Kenji Yamanaka
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
import android.support.annotation.CheckResult
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.view.KeyEvent
import com.pyamsoft.zaptorch.base.preference.CameraPreferences
import io.reactivex.Scheduler
import io.reactivex.Single
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS

internal class VolumeServiceInteractor internal constructor(context: Context,
    val preferences: CameraPreferences,
    torchOffServiceClass: Class<out IntentService>) {
  private val appContext: Context = context.applicationContext
  val notificationManagerCompat: NotificationManagerCompat = NotificationManagerCompat.from(
      context.applicationContext)
  val notification: Notification
  val onStateChangedCallback: CameraCommon.OnStateChangedCallback
  private val cameraApiOld = 0
  private val cameraApiLollipop = 1
  private val cameraApiMarshmallow = 2
  var pressed: Boolean = false
  private var cameraInterface: CameraCommon? = null

  init {
    val intent = Intent(appContext, torchOffServiceClass)

    val notificationChannelId: String = "zaptorch_foreground"
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      setupNotificationChannel(notificationChannelId)
    }

    notification = NotificationCompat.Builder(appContext, notificationChannelId).setContentIntent(
        PendingIntent.getService(appContext, NOTIFICATION_RC, intent,
            PendingIntent.FLAG_UPDATE_CURRENT))
        .setContentTitle("Torch is On")
        .setContentText("Click to turn off")
        .setSmallIcon(R.drawable.ic_light_notification)
        .setAutoCancel(true)
        .setColor(ContextCompat.getColor(appContext, R.color.purple500))
        .setWhen(0)
        .setOngoing(false)
        .build()

    onStateChangedCallback = object : CameraCommon.OnStateChangedCallback {
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

  @RequiresApi(VERSION_CODES.O) private fun setupNotificationChannel(
      notificationChannelId: String) {
    val name = "Torch Service"
    val description = "Notification related to the ZapTorch service"
    val importance = NotificationManagerCompat.IMPORTANCE_MIN
    val notificationChannel = NotificationChannel(notificationChannelId, name, importance)
    notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
    notificationChannel.description = description
    notificationChannel.enableLights(false)
    notificationChannel.enableVibration(false)

    Timber.d("Create notification channel with id: %s", notificationChannelId)
    val notificationManager: NotificationManager = appContext.getSystemService(
        Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(notificationChannel)
  }

  /**
   * public
   */
  @CheckResult fun handleKeyPress(action: Int, keyCode: Int): Single<Long> {
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

  /**
   * public
   */
  @CheckResult fun shouldShowErrorDialog(): Single<Boolean> {
    return Single.fromCallable { preferences.shouldShowErrorDialog() }
  }

  /**
   * public
   */
  fun setupCamera(onCameraError: (Intent) -> Unit,
      obsScheduler: Scheduler, subScheduler: Scheduler) {
    val cameraApi = preferences.cameraApi
    val camera: CameraCommon
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraApi == cameraApiMarshmallow) {
      camera = MarshmallowCamera(appContext, this, obsScheduler, subScheduler)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && cameraApi == cameraApiLollipop) {
      camera = LollipopCamera(appContext, this, obsScheduler, subScheduler)
    } else if (cameraApi == cameraApiOld) {
      camera = OriginalCamera(appContext, this, obsScheduler, subScheduler)
    } else {
      throw RuntimeException("Invalid Camera API selected: " + cameraApi)
    }

    camera.setOnStateChangedCallback(object : CameraCommon.OnStateChangedCallback {
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

  fun toggleTorch() {
    cameraInterface?.toggleTorch()
  }

  fun releaseCamera() {
    val obj = cameraInterface
    if (obj != null) {
      obj.stop()
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
