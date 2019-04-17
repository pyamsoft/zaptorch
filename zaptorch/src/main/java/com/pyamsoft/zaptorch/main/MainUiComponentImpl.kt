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
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiComponent
import com.pyamsoft.pydroid.arch.doOnDestroy
import com.pyamsoft.pydroid.arch.renderOnChange
import com.pyamsoft.zaptorch.main.MainUiComponent.Callback
import com.pyamsoft.zaptorch.main.MainViewModel.MainState
import javax.inject.Inject

internal class MainUiComponentImpl @Inject internal constructor(
  private val frameView: MainFrameView,
  private val viewModel: MainViewModel
) : BaseUiComponent<Callback>(),
    MainUiComponent {

  override fun id(): Int {
    return frameView.id()
  }

  override fun onBind(
    owner: LifecycleOwner,
    savedInstanceState: Bundle?,
    callback: Callback
  ) {
    owner.doOnDestroy {
      frameView.teardown()
      viewModel.unbind()
    }

    frameView.inflate(savedInstanceState)
    viewModel.bind { state, oldState ->
      renderKeypress(state, oldState)
    }
  }

  private fun renderKeypress(
    state: MainState,
    oldState: MainState?
  ) {
    state.renderOnChange(oldState, value = { it.isHandling }) { handling ->
      if (handling != null) {
        callback.onHandleKeyPressChanged(handling.isHandling)
      }
    }
  }

  override fun onSaveState(outState: Bundle) {
    frameView.saveState(outState)
  }

  override fun failedNavigation(error: ActivityNotFoundException) {
    frameView.showError()
  }

}