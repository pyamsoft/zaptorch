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

package com.pyamsoft.zaptorch.main

import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.UnitViewEvent
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.zaptorch.core.MainInteractor
import com.pyamsoft.zaptorch.main.ToolbarControllerEvent.HandleKeypress
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MainToolbarViewModel @Inject internal constructor(
    @Named("debug") debug: Boolean,
    interactor: MainInteractor
) : UiViewModel<UnitViewState, UnitViewEvent, ToolbarControllerEvent>(
    initialState = UnitViewState, debug = debug
) {

    init {
        doOnBind {
            viewModelScope.launch(context = Dispatchers.Default) {
                val handle = interactor.isKeyPressHandled()
                publish(HandleKeypress(handle))

                val listener = interactor.onHandleKeyPressChanged {
                    publish(HandleKeypress(it))
                }

                withContext(context = Dispatchers.Main) {
                    doOnTeardown { listener.cancel() }
                }
            }
        }
    }

    override fun handleViewEvent(event: UnitViewEvent) {
    }
}
