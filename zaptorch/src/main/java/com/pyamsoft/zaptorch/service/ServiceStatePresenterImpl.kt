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

import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.pydroid.arch.BasePresenter
import com.pyamsoft.pydroid.arch.destroy
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal class ServiceStatePresenterImpl internal constructor(
  private val interactor: VolumeServiceInteractor
) : BasePresenter<Unit, ServiceStatePresenter.Callback>(RxBus.empty()),
    ServiceStatePresenter {

  override fun onBind() {
    interactor.observeServiceState()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          if (it) {
            callback.onServiceStarted()
          } else {
            callback.onServiceStopped()
          }
        }
        .destroy(owner)
  }

  override fun onUnbind() {
  }

  override fun start() {
    interactor.setServiceState(true)
  }

  override fun stop() {
    interactor.setServiceState(false)
  }

}
