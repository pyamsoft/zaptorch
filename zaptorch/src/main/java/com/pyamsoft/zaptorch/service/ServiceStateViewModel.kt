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

import com.pyamsoft.pydroid.arch.UiState
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.service.ServiceStateViewModel.ServiceState
import com.pyamsoft.zaptorch.service.ServiceStateViewModel.ServiceState.Started
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class ServiceStateViewModel @Inject internal constructor(
  private val interactor: VolumeServiceInteractor
) : UiViewModel<ServiceState>(
    initialState = ServiceState(isStarted = null)
) {

  override fun onBind() {
    interactor.observeServiceState()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          if (it) {
            handleStart()
          } else {
            handleStop()
          }
        }
        .disposeOnDestroy()
  }

  private fun handleStart() {
    setState { copy(isStarted = Started(true)) }
  }

  private fun handleStop() {
    setState { copy(isStarted = Started(false)) }
  }

  override fun onUnbind() {
  }

  fun start() {
    interactor.setServiceState(true)
  }

  fun stop() {
    interactor.setServiceState(false)
  }

  data class ServiceState(val isStarted: Started?) : UiState {
    data class Started(val isStarted: Boolean)
  }

}
