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

import com.pyamsoft.pydroid.notify.Notifier
import com.pyamsoft.pydroid.notify.NotifyChannelInfo
import com.pyamsoft.pydroid.notify.toNotifyId
import com.pyamsoft.zaptorch.core.NotificationHandler
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class NotificationHandlerImpl
@Inject
internal constructor(@InternalApi private val notifier: Notifier) : NotificationHandler {

  override fun start() {
    stop()
    notifier.show(
            ID,
            NotifyChannelInfo(
                id = "zaptorch_foreground",
                title = "Torch Service",
                description = "Notification related to the ZapTorch service"),
            TorchNotification)
        .also { Timber.d("Start notification: $it") }
  }

  override fun stop() {
    notifier.cancel(ID)
  }

  companion object {

    private val ID = 1345.toNotifyId()
  }
}
