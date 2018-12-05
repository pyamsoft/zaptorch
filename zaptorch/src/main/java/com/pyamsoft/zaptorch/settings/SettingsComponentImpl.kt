/*
 * Copyright (C) 2018 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.zaptorch.settings

import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.zaptorch.service.VolumeServiceModule

internal class SettingsComponentImpl internal constructor(
  private val owner: LifecycleOwner,
  private val preferenceScreen: PreferenceScreen,
  private val tag: String,
  private val settingsModule: SettingsModule,
  private val serviceModule: VolumeServiceModule
) : SettingsComponent {

  override fun inject(fragment: TorchPreferenceFragment) {
    fragment.publisher = serviceModule.getPublisher()
    fragment.viewModel = settingsModule.getViewModel(tag)
    fragment.settingsView = SettingsViewImpl(owner, preferenceScreen)
  }

}
