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

package com.pyamsoft.zaptorch.main

import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.main.MainPresenter.Callback
import io.reactivex.Scheduler
import timber.log.Timber

class MainPresenter internal constructor(
    private val handleKeyPressKey: String,
    private val interactor: MainInteractor,
    computationScheduler: Scheduler, ioScheduler: Scheduler,
    mainScheduler: Scheduler) : SchedulerPresenter<Callback>(computationScheduler, ioScheduler,
    mainScheduler) {

  override fun onStart(bound: Callback) {
    super.onStart(bound)
    shouldHandleKeycode(bound::onHandleKeyPress, bound::onError)
    interactor.register(handleKeyPressKey, bound::onHandleKeyPress)
  }

  override fun onStop() {
    super.onStop()
    interactor.unregister()
  }

  private fun shouldHandleKeycode(onHandleKeyPress: (Boolean) -> Unit,
      onError: (Throwable) -> Unit) {
    disposeOnStop {
      interactor.shouldHandleKeys()
          .subscribeOn(ioScheduler)
          .observeOn(mainThreadScheduler)
          .subscribe({
            onHandleKeyPress(it)
          }, {
            Timber.e(it, "Error while handling keycode")
            onError(it)
          })
    }
  }

  interface Callback : OnHandleKeyPressChangedCallback {

    fun onError(throwable: Throwable)

  }

  interface OnHandleKeyPressChangedCallback {

    fun onHandleKeyPress(handle: Boolean)
  }
}
