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

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.EventBus
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.zaptorch.service.monitor.ServiceFinishEvent
import com.pyamsoft.zaptorch.settings.SettingsControllerEvent.ClearAll
import com.pyamsoft.zaptorch.settings.SettingsViewEvent.SignificantScroll
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class SettingsViewModel @Inject internal constructor(
    private val scrollBus: EventBus<SignificantScrollEvent>,
    private val serviceFinishBus: EventBus<ServiceFinishEvent>,
    @Named("debug") debug: Boolean,
    clearBus: EventBus<ClearAllEvent>
) : UiViewModel<UnitViewState, SettingsViewEvent, SettingsControllerEvent>(
    initialState = UnitViewState, debug = debug
) {

    init {
        doOnInit {
            viewModelScope.launch(context = Dispatchers.Default) {
                clearBus.onEvent { killApplication() }
            }
        }
    }

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

    private suspend fun killApplication() {
        serviceFinishBus.send(ServiceFinishEvent)
        publish(ClearAll)
    }
}
