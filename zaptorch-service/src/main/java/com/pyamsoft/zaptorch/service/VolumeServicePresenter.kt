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
import io.reactivex.Scheduler
import timber.log.Timber

class VolumeServicePresenter(private val interactor: VolumeServiceInteractor,
    private val bus: EventBus<ServiceEvent>,
    observeScheduler: Scheduler, subscribeScheduler: Scheduler) : SchedulerPresenter(
    observeScheduler, subscribeScheduler) {

  override fun onStop() {
    super.onStop()
    interactor.releaseCamera()
  }

  /**
   * public
   */
  fun toggleTorch() {
    interactor.toggleTorch()
  }

  /**
   * public
   */
  fun handleKeyEvent(action: Int, keyCode: Int) {
    disposeOnStop(interactor.handleKeyPress(action, keyCode)
        .subscribeOn(backgroundScheduler)
        .observeOn(foregroundScheduler)
        .subscribe({ time -> Timber.d("Set back after %d delay", time) }
        ) { throwable -> Timber.e(throwable, "onError handleKeyEvent") })
  }

  /**
   * public
   */
  fun setupCamera(errorHandler: (Intent) -> Unit) {
    interactor.setupCamera({
      errorHandler(it)
    }, foregroundScheduler, backgroundScheduler)
  }

  /**
   * public
   */
  fun registerOnBus(onToggleTorch: () -> Unit, onChangeCameraApi: () -> Unit,
      onFinishService: () -> Unit) {
    disposeOnDestroy {
      bus.listen()
          .subscribeOn(backgroundScheduler)
          .observeOn(foregroundScheduler)
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
}
