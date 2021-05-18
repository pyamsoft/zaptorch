/*
 * Copyright 2020 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.service.notification

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.ColorRes
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.NotifyData
import com.pyamsoft.pydroid.notify.NotifyDispatcher
import com.pyamsoft.pydroid.notify.NotifyId
import com.pyamsoft.zaptorch.service.R
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class TorchNotificationDispatcher
@Inject
internal constructor(
    private val context: Context,
    receiverClass: Class<out BroadcastReceiver>,
    @ColorRes notificationColor: Int,
) : NotifyDispatcher<TorchNotification> {

  private val color = ContextCompat.getColor(context, notificationColor)
  private val notificationManager by lazy {
    requireNotNull(context.getSystemService<NotificationManager>())
  }
  private val pendingIntent by lazy {
    PendingIntent.getBroadcast(
        context, RC, Intent(context, receiverClass), PendingIntent.FLAG_UPDATE_CURRENT)
  }

  private fun setupNotificationChannel(channelInfo: NotifyChannelInfo) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      Timber.d("No channel below Android O")
      return
    }

    val channel: NotificationChannel? = notificationManager.getNotificationChannel(channelInfo.id)
    if (channel != null) {
      Timber.d("Channel already exists: ${channel.id}")
      return
    }

    val notificationGroup = NotificationChannelGroup(channelInfo.id, channelInfo.title)
    val importance = NotificationManager.IMPORTANCE_DEFAULT
    val notificationChannel =
        NotificationChannel(channelInfo.id, channelInfo.title, importance).apply {
          lockscreenVisibility = Notification.VISIBILITY_PUBLIC
          description = channelInfo.description
          enableLights(false)
          enableVibration(false)
        }

    Timber.d(
        "Create notification channel and group ${notificationChannel.id} ${notificationGroup.id}")
    notificationManager.apply {
      createNotificationChannelGroup(notificationGroup)
      createNotificationChannel(notificationChannel)
    }
  }

  override fun build(
      id: NotifyId,
      channelInfo: NotifyChannelInfo,
      notification: TorchNotification
  ): Notification {
    setupNotificationChannel(channelInfo)
    return NotificationCompat.Builder(context, channelInfo.id)
        .setContentIntent(pendingIntent)
        .setContentTitle("Torch is On")
        .setContentText("Click to turn off")
        .setSmallIcon(R.drawable.ic_light_notification)
        .setAutoCancel(true)
        .setWhen(0)
        .setOngoing(false)
        .setColor(color)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .build()
  }

  override fun canShow(notification: NotifyData): Boolean {
    return notification is TorchNotification
  }

  companion object {

    private const val RC = 1009
  }
}
