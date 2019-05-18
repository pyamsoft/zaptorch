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

package com.pyamsoft.zaptorch.settings

import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.zaptorch.service.ServiceFinishEvent
import com.pyamsoft.zaptorch.settings.SettingsControllerEvent.ClearAll
import com.pyamsoft.zaptorch.settings.SettingsControllerEvent.Explain
import com.pyamsoft.zaptorch.settings.SettingsViewEvent.ShowExplanation
import com.pyamsoft.zaptorch.settings.SettingsViewEvent.SignificantScroll
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class SettingsViewModel @Inject internal constructor(
  private val scrollBus: EventBus<SignificantScrollEvent>,
  private val serviceFinishBus: EventBus<ServiceFinishEvent>,
  private val bus: EventBus<ClearAllEvent>
) : UiViewModel<SettingsViewState, SettingsViewEvent, SettingsControllerEvent>(
    initialState = SettingsViewState(throwable = null)
) {

  private var clearDisposable by singleDisposable()

  override fun onCleared() {
    clearDisposable.tryDispose()
  }

  override fun handleViewEvent(event: SettingsViewEvent) {
    return when (event) {
      is SignificantScroll -> scrollBus.publish(SignificantScrollEvent(event.visible))
      is ShowExplanation -> publish(Explain)
    }
  }

  fun beginWatchingForClear() {
    clearDisposable = bus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { killApplication() }
  }

  private fun killApplication() {
    serviceFinishBus.publish(ServiceFinishEvent)
    publish(ClearAll)
  }
}