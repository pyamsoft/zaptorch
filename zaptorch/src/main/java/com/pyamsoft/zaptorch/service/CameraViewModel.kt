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
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.zaptorch.api.CameraInterface.CameraError
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.service.CameraViewModel.ServiceState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class CameraViewModel @Inject internal constructor(
  private val interactor: VolumeServiceInteractor
) : UiViewModel<ServiceState>(
    initialState = ServiceState(throwable = null)
) {

  private var keyEventDisposable by singleDisposable()

  override fun onBind() {
    interactor.observeCameraState()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { interactor.setupCamera() }
        .doAfterTerminate { interactor.releaseCamera() }
        .subscribe { handleCameraError(it) }
        .disposeOnDestroy()
  }

  private fun handleCameraError(error: CameraError) {
    setState { copy(throwable = error) }
  }

  override fun onUnbind() {
    keyEventDisposable.tryDispose()
  }

  fun handleKeyEvent(
    action: Int,
    keyCode: Int
  ) {
    keyEventDisposable = interactor.handleKeyPress(action, keyCode)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
  }

  data class ServiceState(val throwable: CameraError?) : UiState

}
