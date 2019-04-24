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

import com.pyamsoft.pydroid.arch.UiEventHandler
import com.pyamsoft.pydroid.arch.UiState
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.zaptorch.settings.SettingsHandler.SettingsEvent
import com.pyamsoft.zaptorch.settings.SettingsViewModel.SettingsState
import javax.inject.Inject

internal class SettingsViewModel @Inject internal constructor(
  private val handler: UiEventHandler<SettingsEvent, SettingsView.Callback>,
  private val bus: EventBus<SignificantScrollEvent>
) : UiViewModel<SettingsState>(
    initialState = SettingsState(isExplaining = false)
), SettingsView.Callback {

  override fun onBind() {
    handler.handle(this)
        .disposeOnDestroy()
  }

  override fun onUnbind() {
  }

  override fun onExplainClicked() {
    setUniqueState(true, old = { it.isExplaining }) { state, value ->
      state.copy(isExplaining = value)
    }
  }

  override fun onSignificantScrollEvent(visible: Boolean) {
    bus.publish(SignificantScrollEvent(visible))
  }

  data class SettingsState(val isExplaining: Boolean) : UiState

}
