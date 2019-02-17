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

import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.ui.arch.BasePresenter

internal class SettingsPresenterImpl internal constructor(
  bus: EventBus<SignificantScrollEvent>
) : BasePresenter<SignificantScrollEvent, SettingsPresenter.Callback>(bus),
    SettingsView.Callback,
    SettingsPresenter {

  override fun onExplainClicked() {
    callback.onShowExplanation()
  }

  override fun onSignificantScrollEvent(visible: Boolean) {
    publish(SignificantScrollEvent(visible))
  }

  override fun onBind() {
  }

  override fun onUnbind() {
  }

}
