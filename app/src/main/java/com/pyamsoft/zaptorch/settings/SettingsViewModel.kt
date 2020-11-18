/*
 * Copyright 2020 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.settings

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.EventBus
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.zaptorch.settings.SettingsViewEvent.SignificantScroll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class SettingsViewModel @Inject internal constructor(
    private val scrollBus: EventBus<SignificantScrollEvent>,
) : UiViewModel<UnitViewState, SettingsViewEvent, SettingsControllerEvent>(UnitViewState) {

    override fun handleViewEvent(event: SettingsViewEvent) {
        return when (event) {
            is SignificantScroll -> scroll(event.visible)
        }
    }

    private fun scroll(visible: Boolean) {
        viewModelScope.launch(context = Dispatchers.Default) {
            scrollBus.send(SignificantScrollEvent(visible))
        }
    }
}
