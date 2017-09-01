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

import android.content.Intent
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.CHANGE_CAMERA
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.FINISH
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.TORCH
import com.pyamsoft.zaptorch.service.VolumeServicePresenter.Callback
import io.reactivex.Scheduler
import timber.log.Timber

class VolumeServicePresenter internal constructor(private val interactor: VolumeServiceInteractor,
    private val bus: EventBus<ServiceEvent>,
    computationScheduler: Scheduler, ioScheduler: Scheduler,
    mainThreadScheduler: Scheduler) : SchedulerPresenter<Callback>(
    computationScheduler, ioScheduler, mainThreadScheduler) {

  override fun onBind(v: Callback) {
    super.onBind(v)
    registerOnBus(v::onToggleTorch, v::onChangeCameraApi, v::onFinishService)
  }

  override fun onUnbind() {
    super.onUnbind()
    interactor.releaseCamera()
  }

  fun toggleTorch() {
    interactor.toggleTorch()
  }

  fun handleKeyEvent(action: Int, keyCode: Int) {
    dispose(interactor.handleKeyPress(action, keyCode)
        .subscribeOn(ioScheduler)
        .observeOn(mainThreadScheduler)
        .subscribe({ time -> Timber.d("Set back after %d delay", time) }
        ) { throwable -> Timber.e(throwable, "onError handleKeyEvent") })
  }

  fun setupCamera(errorHandler: (Intent) -> Unit) {
    interactor.setupCamera({
      errorHandler(it)
    }, computationScheduler, ioScheduler, mainThreadScheduler)
  }

  private fun registerOnBus(onToggleTorch: () -> Unit, onChangeCameraApi: () -> Unit,
      onFinishService: () -> Unit) {
    dispose {
      bus.listen()
          .subscribeOn(ioScheduler)
          .observeOn(mainThreadScheduler)
          .subscribe({ (type) ->
            when (type) {
              TORCH -> onToggleTorch()
              CHANGE_CAMERA -> onChangeCameraApi()
              FINISH -> onFinishService()
              else -> throw IllegalArgumentException(
                  "Invalid ServiceEvent.Type: " + type)
            }
          }, { Timber.e(it, "onError event bus") })
    }
  }

  interface Callback {

    fun onToggleTorch()

    fun onChangeCameraApi()

    fun onFinishService()
  }
}
