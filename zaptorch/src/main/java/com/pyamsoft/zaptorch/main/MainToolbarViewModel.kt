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

import com.pyamsoft.pydroid.arch.UiEventHandler
import com.pyamsoft.pydroid.arch.UiState
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.zaptorch.main.MainToolbarHandler.ToolbarEvent
import com.pyamsoft.zaptorch.main.MainToolbarViewModel.ToolbarState
import javax.inject.Inject

internal class MainToolbarViewModel @Inject internal constructor(
  private val handler: UiEventHandler<ToolbarEvent, MainToolbarView.Callback>
) : UiViewModel<ToolbarState>(
    initialState = ToolbarState(isShowingPolicy = false)
), MainToolbarView.Callback {

  override fun onBind() {
    handler.handle(this)
        .disposeOnDestroy()
  }

  override fun onUnbind() {
  }

  override fun onPrivacyPolicyClicked() {
    setUniqueState(true, old = { it.isShowingPolicy }) { state, value ->
      state.copy(isShowingPolicy = value)
    }
  }

  data class ToolbarState(val isShowingPolicy: Boolean) : UiState

}
