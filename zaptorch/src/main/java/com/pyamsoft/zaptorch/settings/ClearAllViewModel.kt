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

import com.pyamsoft.pydroid.arch.UiState
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.zaptorch.api.SettingsInteractor
import com.pyamsoft.zaptorch.settings.ClearAllViewModel.ClearState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ClearAllViewModel @Inject internal constructor(
  private val interactor: SettingsInteractor,
  private val bus: EventBus<ClearAllEvent>
) : UiViewModel<ClearState>(
    initialState = ClearState(isClearing = false, throwable = null)
) {

  private var clearDisposable by singleDisposable()

  override fun onBind() {
    bus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { handleClearAll() }
        .destroy()
  }

  override fun onUnbind() {
    clearDisposable.tryDispose()
  }

  fun clearAll() {
    clearDisposable = interactor.clearAll()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ bus.publish(ClearAllEvent) }, {
          Timber.e(it, "Error clearing all settings")
          handleClearAllError(it)
        })
  }

  private fun handleClearAll() {
    setState {
      copy(throwable = null).also {
        setUniqueState(true, old = { it.isClearing }) { state, value ->
          state.copy(isClearing = value)
        }
      }
    }
  }

  private fun handleClearAllError(throwable: Throwable) {
    setState {
      copy(isClearing = false, throwable = throwable)
    }
  }

  data class ClearState(
    val isClearing: Boolean,
    val throwable: Throwable?
  ) : UiState
}

