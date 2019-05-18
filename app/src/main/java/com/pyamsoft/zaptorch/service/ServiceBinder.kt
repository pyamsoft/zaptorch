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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.service.ServiceControllerEvent.Finish
import com.pyamsoft.zaptorch.service.ServiceControllerEvent.RenderError
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class ServiceBinder @Inject internal constructor(
  private val finishBus: EventBus<ServiceFinishEvent>,
  private val interactor: VolumeServiceInteractor
) {

  private var keyEventDisposable by singleDisposable()
  private var cameraStateDisposable by singleDisposable()
  private var finishDisposable by singleDisposable()

  @CheckResult
  fun bind(onEvent: (event: ServiceControllerEvent) -> Unit): Disposable {
    cameraStateDisposable = interactor.observeCameraState()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { interactor.setupCamera() }
        .doAfterTerminate { interactor.releaseCamera() }
        .subscribe { onEvent(RenderError(it)) }

    finishDisposable = finishBus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { onEvent(Finish) }

    return object : Disposable {
      override fun isDisposed(): Boolean {
        return (
            keyEventDisposable.isDisposed &&
                cameraStateDisposable.isDisposed &&
                finishDisposable.isDisposed
            )
      }

      override fun dispose() {
        keyEventDisposable.tryDispose()
        cameraStateDisposable.tryDispose()
        finishDisposable.tryDispose()
      }

    }

  }

  fun handleKeyEvent(
    action: Int,
    keyCode: Int
  ) {
    keyEventDisposable = interactor.handleKeyPress(action, keyCode)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doAfterTerminate { keyEventDisposable.tryDispose() }
        .subscribe()
  }

  fun start() {
    interactor.setServiceState(true)
  }

  fun stop() {
    interactor.setServiceState(false)
  }

}
