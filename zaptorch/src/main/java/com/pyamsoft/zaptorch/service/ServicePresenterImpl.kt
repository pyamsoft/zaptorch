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
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.pydroid.arch.BasePresenter
import com.pyamsoft.pydroid.arch.destroy
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal class ServicePresenterImpl internal constructor(
  private val interactor: VolumeServiceInteractor
) : BasePresenter<Unit, ServicePresenter.Callback>(RxBus.empty()),
    ServicePresenter {

  private var keyEventDisposable by singleDisposable()

  override fun onBind() {
    interactor.observeCameraState()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { interactor.setupCamera() }
        .doAfterTerminate { interactor.releaseCamera() }
        .subscribe { callback.onCameraError(it) }
        .destroy(owner)
  }

  override fun onUnbind() {
    keyEventDisposable.tryDispose()
  }

  override fun handleKeyEvent(
    action: Int,
    keyCode: Int
  ) {
    keyEventDisposable = interactor.handleKeyPress(action, keyCode)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
  }

}
