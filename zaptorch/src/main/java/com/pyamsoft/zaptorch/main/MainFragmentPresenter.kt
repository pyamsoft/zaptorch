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

import com.pyamsoft.pydroid.arch.Presenter
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.zaptorch.main.MainFragmentPresenter.FragmentState
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal class MainFragmentPresenter internal constructor(
  private val bus: EventBus<SignificantScrollEvent>
) : Presenter<FragmentState, MainFragmentPresenter.Callback>(),
    MainActionView.Callback {

  override fun initialState(): FragmentState {
    return FragmentState(isVisible = false)
  }

  override fun onBind() {
    bus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { handleSignificantScrollEvent(it.visible) }
        .destroy()
  }

  override fun onUnbind() {
  }

  override fun onActionButtonClicked(running: Boolean) {
    if (running) {
      callback.handleServiceStarted()
    } else {
      callback.handleServiceStopped()
    }
  }

  private fun handleSignificantScrollEvent(visible: Boolean) {
    setState {
      copy(isVisible = visible)
    }
  }

  data class FragmentState(val isVisible: Boolean)

  interface Callback : Presenter.Callback<FragmentState> {

    fun handleServiceStarted()

    fun handleServiceStopped()
  }

}

