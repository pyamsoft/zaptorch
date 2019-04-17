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

import com.pyamsoft.pydroid.arch.UiEventHandler
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.zaptorch.settings.SettingsHandler.SettingsEvent
import com.pyamsoft.zaptorch.settings.SettingsHandler.SettingsEvent.Explain
import com.pyamsoft.zaptorch.settings.SettingsHandler.SettingsEvent.ScrollEvent
import com.pyamsoft.zaptorch.settings.SettingsView.Callback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class SettingsHandler @Inject internal constructor(
  bus: EventBus<SettingsEvent>
) : UiEventHandler<SettingsEvent, Callback>(bus),
    Callback {

  override fun onExplainClicked() {
    publish(Explain)
  }

  override fun onSignificantScrollEvent(visible: Boolean) {
    publish(ScrollEvent(visible))
  }

  override fun handle(delegate: Callback): Disposable {
    return listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          return@subscribe when (it) {
            is Explain -> delegate.onExplainClicked()
            is ScrollEvent -> delegate.onSignificantScrollEvent(it.visible)
          }
        }
  }

  sealed class SettingsEvent {
    object Explain : SettingsEvent()
    data class ScrollEvent(val visible: Boolean) : SettingsEvent()

  }

}
