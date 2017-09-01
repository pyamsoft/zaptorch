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

import android.support.annotation.CheckResult
import com.pyamsoft.zaptorch.base.ZapTorchModule
import io.reactivex.Scheduler

class VolumeServiceModule(module: ZapTorchModule) {

  private val interactor: VolumeServiceInteractor
  private val bus = ServiceBus()
  private val computationScheduler: Scheduler = module.provideComputationScheduler()
  private val ioScheduler: Scheduler = module.provideIoScheduler()
  private val mainScheduler: Scheduler = module.provideMainThreadScheduler()

  init {
    interactor = VolumeServiceInteractorImpl(module.provideContext(),
        module.provideCameraPreferences(), module.provideTorchOffServiceClass())
  }

  @CheckResult
  fun getServicePresenter(): VolumeServicePresenter =
      VolumeServicePresenter(interactor, bus, computationScheduler, ioScheduler, mainScheduler)

  @CheckResult
  fun getPresenter(): ServicePublisher = ServicePublisher(bus)
}
