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

import com.pyamsoft.pydroid.arch.impl.BaseUiViewModel
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.main.MainControllerEvent.ServiceAction
import com.pyamsoft.zaptorch.main.MainViewEvent.ActionClick
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class MainViewModel @Inject internal constructor(
  private val serviceInteractor: VolumeServiceInteractor,
  private val visibilityBus: EventBus<SignificantScrollEvent>
) : BaseUiViewModel<MainViewState, MainViewEvent, MainControllerEvent>(
    initialState = MainViewState(
        isVisible = true,
        isServiceRunning = false
    )
) {

  private var serviceDisposable by singleDisposable()
  private var visibilityDisposable by singleDisposable()

  override fun onBind() {
    serviceDisposable = serviceInteractor.observeServiceState()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setState { copy(isServiceRunning = it) } }

    visibilityDisposable = visibilityBus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { setState { copy(isVisible = it.visible) } }
  }

  override fun handleViewEvent(event: MainViewEvent) {
    return when (event) {
      is ActionClick -> publish(ServiceAction(event.isServiceRunning))
    }
  }

  override fun onUnbind() {
    serviceDisposable.tryDispose()
  }

}
