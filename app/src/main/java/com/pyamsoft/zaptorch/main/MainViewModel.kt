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

package com.pyamsoft.zaptorch.main

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.EventBus
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.main.MainControllerEvent.ServiceAction
import com.pyamsoft.zaptorch.main.MainViewEvent.ActionClick
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

internal class MainViewModel @Inject internal constructor(
    @Named("debug") debug: Boolean,
    serviceInteractor: VolumeServiceInteractor,
    visibilityBus: EventBus<SignificantScrollEvent>
) : UiViewModel<MainViewState, MainViewEvent, MainControllerEvent>(
    initialState = MainViewState(isVisible = true, isServiceRunning = false), debug = debug
) {

    init {
        doOnInit {
            viewModelScope.launch {
                serviceInteractor.observeServiceState()
                    .subscribe { setState { copy(isServiceRunning = it) } }
            }
        }

        doOnInit {
            visibilityBus.scopedEvent {
                setState { copy(isVisible = it.visible) }
            }
        }
    }

    override fun handleViewEvent(event: MainViewEvent) {
        return when (event) {
            is ActionClick -> publish(ServiceAction(event.isServiceRunning))
        }
    }
}
