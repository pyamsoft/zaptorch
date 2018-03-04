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

package com.pyamsoft.zaptorch.main

import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.api.MainInteractor
import io.reactivex.Scheduler
import timber.log.Timber

class MainPresenter internal constructor(
  private val handleKeyPressKey: String,
  private val interactor: MainInteractor,
  computationScheduler: Scheduler,
  ioScheduler: Scheduler,
  mainScheduler: Scheduler
) : SchedulerPresenter<MainPresenter.View>(
    computationScheduler, ioScheduler,
    mainScheduler
) {

  override fun onCreate() {
    super.onCreate()
    shouldHandleKeycode()
    interactor.register(handleKeyPressKey, { view?.onHandleKeyPress(it) })
  }

  override fun onDestroy() {
    super.onDestroy()
    interactor.unregister()
  }

  private fun shouldHandleKeycode() {
    dispose {
      interactor.shouldHandleKeys()
          .subscribeOn(ioScheduler)
          .observeOn(mainThreadScheduler)
          .subscribe({
            view?.onHandleKeyPress(it)
          }, {
            Timber.e(it, "Error while handling keycode")
            view?.onError(it)
          })
    }
  }

  interface View : OnHandleKeyPressChangedCallback

  interface OnHandleKeyPressChangedCallback {

    fun onHandleKeyPress(handle: Boolean)

    fun onError(throwable: Throwable)
  }
}
