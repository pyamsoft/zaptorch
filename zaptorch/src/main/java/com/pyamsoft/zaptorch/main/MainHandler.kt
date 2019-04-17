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
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.zaptorch.main.MainActionView.Callback
import com.pyamsoft.zaptorch.main.MainHandler.MainEvent
import com.pyamsoft.zaptorch.main.MainHandler.MainEvent.ActionClicked
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class MainHandler @Inject internal constructor(
  bus: EventBus<MainEvent>
) : UiEventHandler<MainEvent, Callback>(bus),
    Callback {

  override fun onActionButtonClicked(running: Boolean) {
    publish(ActionClicked(running))
  }

  override fun handle(delegate: Callback): Disposable {
    return listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          return@subscribe when (it) {
            is ActionClicked -> delegate.onActionButtonClicked(it.running)
          }
        }
  }

  sealed class MainEvent {
    data class ActionClicked(val running: Boolean) : MainEvent()
  }
}