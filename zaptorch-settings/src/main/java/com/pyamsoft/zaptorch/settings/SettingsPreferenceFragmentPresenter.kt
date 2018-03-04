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

package com.pyamsoft.zaptorch.settings

import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.api.SettingsPreferenceFragmentInteractor
import com.pyamsoft.zaptorch.model.ConfirmEvent
import io.reactivex.Scheduler
import timber.log.Timber

class SettingsPreferenceFragmentPresenter internal constructor(
  private val bus: EventBus<ConfirmEvent>,
  private val interactor: SettingsPreferenceFragmentInteractor,
  computationScheduler: Scheduler,
  ioScheduler: Scheduler,
  mainThreadScheduler: Scheduler
) : SchedulerPresenter<SettingsPreferenceFragmentPresenter.View>(
    computationScheduler, ioScheduler, mainThreadScheduler
) {

  override fun onCreate() {
    super.onCreate()
    registerEventBus()
  }

  private fun registerEventBus() {
    dispose {
      bus.listen()
          .flatMapSingle { interactor.clearAll() }
          .subscribeOn(ioScheduler)
          .observeOn(mainThreadScheduler)
          .subscribe({ view?.onClearAll() }, { Timber.e(it, "onError event bus") })
    }
  }

  interface View : ClearCallback

  interface ClearCallback {

    fun onClearAll()
  }
}
