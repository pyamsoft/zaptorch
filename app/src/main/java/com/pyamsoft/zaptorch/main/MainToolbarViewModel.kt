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

import android.content.ActivityNotFoundException
import androidx.lifecycle.viewModelScope
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.zaptorch.api.MainInteractor
import com.pyamsoft.zaptorch.main.ToolbarControllerEvent.HandleKeypress
import com.pyamsoft.zaptorch.main.ToolbarControllerEvent.PrivacyPolicy
import com.pyamsoft.zaptorch.main.ToolbarViewEvent.ViewPrivacyPolicy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

internal class MainToolbarViewModel @Inject internal constructor(
  private val interactor: MainInteractor
) : UiViewModel<ToolbarViewState, ToolbarViewEvent, ToolbarControllerEvent>(
    initialState = ToolbarViewState(throwable = null)
) {

  override fun onInit() {
    viewModelScope.launch(context = Dispatchers.Default) {
      interactor.onHandleKeyPressChanged()
          .onEvent { handleKeypressChanged(it) }
    }
  }

  override fun handleViewEvent(event: ToolbarViewEvent) {
    return when (event) {
      is ViewPrivacyPolicy -> publish(PrivacyPolicy)
    }
  }

  private fun handleKeypressChanged(handle: Boolean) {
    publish(HandleKeypress(handle))
  }

  fun failedNavigation(error: ActivityNotFoundException) {
    setState { copy(throwable = error) }
  }

}
