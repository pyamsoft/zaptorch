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

package com.pyamsoft.zaptorch.service

import android.content.Intent
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.bus.Publisher
import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.pydroid.core.threads.Enforcer
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.api.ZapTorchModule
import com.pyamsoft.zaptorch.model.ServiceEvent

class VolumeServiceModule(
  module: ZapTorchModule,
  enforcer: Enforcer
) {

  private val interactor: VolumeServiceInteractor
  private val bus = RxBus.create<ServiceEvent>()
  private val errorBus = RxBus.create<Intent>()

  init {
    interactor = VolumeServiceInteractorImpl(
        enforcer,
        errorBus,
        module.provideContext(),
        module.provideCameraPreferences(),
        module.provideTorchOffServiceClass(),
        module.provideNotificationColor()
    )
  }

  @CheckResult
  fun getViewModel() = VolumeServiceViewModel(errorBus, interactor, bus)

  @CheckResult
  fun getPublisher(): Publisher<ServiceEvent> = bus
}
