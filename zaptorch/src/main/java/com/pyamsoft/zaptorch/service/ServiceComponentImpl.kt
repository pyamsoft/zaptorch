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

import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor

internal class ServiceComponentImpl internal constructor(
  private val owner: LifecycleOwner,
  private val interactor: VolumeServiceInteractor,
  private val torchBus: EventBus<TorchToggleEvent>,
  private val serviceFinishBus: EventBus<ServiceFinishEvent>
) : ServiceComponent {

  override fun inject(service: TorchOffService) {
    service.apply {
      this.presenter = TorchPresenterImpl(interactor, owner, torchBus)
    }
  }

  override fun inject(service: VolumeMonitorService) {
    service.apply {
      this.finishPresenter = ServiceFinishPresenterImpl(owner, serviceFinishBus)
      this.servicePresenter = ServicePresenterImpl(interactor, owner)
      this.statePresenter = ServiceStatePresenterImpl(interactor, owner)
      this.torchPresenter = TorchPresenterImpl(interactor, owner, torchBus)
    }
  }

}
