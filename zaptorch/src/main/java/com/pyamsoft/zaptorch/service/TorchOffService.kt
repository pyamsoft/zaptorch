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
import android.content.Intent
import com.pyamsoft.pydroid.core.bus.Publisher
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.model.ServiceEvent
import timber.log.Timber

class TorchOffService : IntentService(TorchOffService::class.java.name) {

  internal lateinit var servicePublisher: Publisher<ServiceEvent>

  override fun onCreate() {
    super.onCreate()
    Injector.obtain<ZapTorchComponent>(applicationContext)
        .inject(this)
  }

  override fun onDestroy() {
    super.onDestroy()
    ZapTorch.getRefWatcher(this)
        .watch(this)
  }

  override fun onHandleIntent(intent: Intent?) {
    try {
      servicePublisher.publish(ServiceEvent(ServiceEvent.Type.TORCH))
    } catch (e: IllegalStateException) {
      Timber.e(e, "onError")
    }
  }
}
