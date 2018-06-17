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

import android.content.Intent
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.core.presenter.Presenter
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.FINISH
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.TORCH
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class VolumeServicePresenter internal constructor(
  private val interactor: VolumeServiceInteractor,
  private val bus: EventBus<ServiceEvent>
) : Presenter<VolumeServicePresenter.View>() {

  private var keyDisposable: Disposable = Disposables.empty()

  override fun onCreate() {
    super.onCreate()
    registerOnBus()
  }

  override fun onDestroy() {
    super.onDestroy()
    interactor.releaseCamera()
    keyDisposable.dispose()
  }

  override fun onStart() {
    super.onStart()
    setupCamera()
  }

  private fun setupCamera() {
    interactor.setupCamera {
      view?.onError(it)
    }
  }

  private fun toggleTorch() {
    Timber.d("Toggle torch")
    interactor.toggleTorch()
  }

  fun handleKeyEvent(
    action: Int,
    keyCode: Int
  ) {
    keyDisposable.dispose()
    keyDisposable = interactor.handleKeyPress(action, keyCode)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ time -> Timber.d("Set back after %d delay", time) }
            , { throwable -> Timber.e(throwable, "onError handleKeyEvent") })
  }

  private fun registerOnBus() {
    dispose {
      bus.listen()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe({ (type) ->
            when (type) {
              TORCH -> toggleTorch()
              FINISH -> view?.onFinishService()
              else -> throw IllegalArgumentException("Invalid ServiceEvent.Type: $type")
            }
          }, { Timber.e(it, "onError event bus") })
    }
  }

  interface View : ServiceCallback, ErrorHandlerCallback

  interface ErrorHandlerCallback {

    fun onError(intent: Intent)
  }

  interface ServiceCallback {

    fun onFinishService()
  }
}
