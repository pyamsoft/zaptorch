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

import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.zaptorch.api.SettingsInteractor
import com.pyamsoft.zaptorch.service.ServiceFinishEvent
import com.pyamsoft.zaptorch.service.ServiceFinishPresenterImpl
import com.pyamsoft.zaptorch.widget.ToolbarView

internal class SettingsComponentImpl internal constructor(
  private val recyclerView: RecyclerView,
  private val preferenceScreen: PreferenceScreen,
  private val interactor: SettingsInteractor,
  private val clearAllBus: EventBus<ClearAllEvent>,
  private val significantScrollBus: EventBus<SignificantScrollEvent>,
  private val serviceFinishBus: EventBus<ServiceFinishEvent>
) : SettingsComponent {

  override fun inject(fragment: TorchPreferenceFragment) {
    val settingsPresenter = SettingsPresenterImpl(significantScrollBus)
    val view = SettingsView(recyclerView, preferenceScreen, settingsPresenter)
    val clearPresenter = ClearAllPresenterImpl(interactor, clearAllBus)
    val serviceFinishPresenter = ServiceFinishPresenterImpl(serviceFinishBus)

    fragment.apply {
      this.component = SettingsUiComponentImpl(
          view, settingsPresenter, serviceFinishPresenter, clearPresenter
      )
      this.toolbarView = ToolbarView(fragment.requireToolbarActivity())
    }
  }

}
