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

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiComponent
import com.pyamsoft.pydroid.arch.doOnDestroy
import com.pyamsoft.pydroid.arch.renderOnChange
import com.pyamsoft.pydroid.ui.arch.InvalidIdException
import com.pyamsoft.zaptorch.service.ServiceFinishViewModel
import com.pyamsoft.zaptorch.settings.ClearAllViewModel.ClearState
import com.pyamsoft.zaptorch.settings.SettingsUiComponent.Callback
import com.pyamsoft.zaptorch.settings.SettingsViewModel.SettingsState
import timber.log.Timber
import javax.inject.Inject

internal class SettingsUiComponentImpl @Inject internal constructor(
  private val settingsView: SettingsView,
  private val viewModel: SettingsViewModel,
  private val serviceFinishViewModel: ServiceFinishViewModel,
  private val clearViewModel: ClearAllViewModel
) : BaseUiComponent<Callback>(),
    SettingsUiComponent {

  override fun id(): Int {
    throw InvalidIdException
  }

  override fun onBind(
    owner: LifecycleOwner,
    savedInstanceState: Bundle?,
    callback: Callback
  ) {
    owner.doOnDestroy {
      settingsView.teardown()
      viewModel.unbind()
      clearViewModel.unbind()
    }

    settingsView.inflate(savedInstanceState)
    viewModel.bind { state, oldState ->
      renderExplain(state, oldState)
    }
    clearViewModel.bind { state, oldState ->
      renderClear(state, oldState)
      renderError(state, oldState)
    }
  }

  private fun renderExplain(
    state: SettingsState,
    oldState: SettingsState?
  ) {
    state.renderOnChange(oldState, value = { it.isExplaining }) { explain ->
      if (explain) {
        callback.showHowTo()
      }
    }
  }

  override fun onSaveState(outState: Bundle) {
    settingsView.saveState(outState)
  }

  private fun renderClear(
    state: ClearState,
    oldState: ClearState?
  ) {
    state.renderOnChange(oldState, value = { it.isClearing }) { clearing ->
      if (clearing) {
        try {
          serviceFinishViewModel.finish()
        } catch (e: NullPointerException) {
          Timber.e(e, "Expected exception when Service is NULL")
        }
        callback.onKillApplication()
      }
    }
  }

  private fun renderError(
    state: ClearState,
    oldState: ClearState?
  ) {
    state.throwable.let { throwable ->
      if (oldState == null || oldState.throwable != throwable) {
        if (throwable == null) {
          settingsView.clearError()
        } else {
          settingsView.showError("Error resetting settings, please try again later")
        }
      }
    }
  }
}