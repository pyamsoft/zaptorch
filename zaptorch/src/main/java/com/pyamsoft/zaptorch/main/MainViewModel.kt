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

import com.pyamsoft.pydroid.arch.UiState
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.zaptorch.api.MainInteractor
import com.pyamsoft.zaptorch.main.MainViewModel.MainState
import com.pyamsoft.zaptorch.main.MainViewModel.MainState.Handle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class MainViewModel @Inject internal constructor(
  private val interactor: MainInteractor
) : UiViewModel<MainState>(
    initialState = MainState(isHandling = null)
) {

  override fun onBind() {
    interactor.onHandleKeyPressChanged()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { handleKeypressChanged(it) }
        .destroy()
  }

  private fun handleKeypressChanged(handle: Boolean) {
    setUniqueState(Handle(handle), old = { it.isHandling }) { state, value ->
      state.copy(isHandling = value)
    }
  }

  override fun onUnbind() {
  }

  data class MainState(val isHandling: Handle?) : UiState {
    data class Handle(val isHandling: Boolean)
  }
}
