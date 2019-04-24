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

import com.pyamsoft.zaptorch.main.MainFragmentViewModel.FragmentState
import com.pyamsoft.zaptorch.main.MainFragmentViewModel.FragmentState.Visible
import com.pyamsoft.zaptorch.main.MainHandler.MainEvent
import com.pyamsoft.zaptorch.service.ServiceStateViewModel.ServiceState.Started
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent
import com.pyamsoft.pydroid.arch.UiEventHandler
import com.pyamsoft.pydroid.arch.UiState
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.core.bus.EventBus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class MainFragmentViewModel @Inject internal constructor(
  private val handler: UiEventHandler<MainEvent, MainActionView.Callback>,
  private val bus: EventBus<SignificantScrollEvent>
) : UiViewModel<FragmentState>(
    initialState = FragmentState(isStarted = null, isVisible = null)
), MainActionView.Callback {

  override fun onBind() {
    handler.handle(this)
        .disposeOnDestroy()

    bus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { handleSignificantScrollEvent(it.visible) }
        .disposeOnDestroy()
  }

  override fun onUnbind() {
  }

  override fun onActionButtonClicked(running: Boolean) {
    if (running) {
      handleStart()
    } else {
      handleStop()
    }
  }

  private fun handleStart() {
    setUniqueState(Started(true), old = { it.isStarted }) { state, value ->
      state.copy(isStarted = value)
    }
  }

  private fun handleStop() {
    setUniqueState(Started(false), old = { it.isStarted }) { state, value ->
      state.copy(isStarted = value)
    }
  }

  private fun handleSignificantScrollEvent(visible: Boolean) {
    setState {
      copy(isVisible = Visible(visible))
    }
  }

  data class FragmentState(
    val isVisible: Visible?,
    val isStarted: Started?
  ) : UiState {
    data class Visible(val isVisible: Boolean)
  }

}
