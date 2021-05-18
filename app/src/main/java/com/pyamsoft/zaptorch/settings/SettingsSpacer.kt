/*
 * Copyright 2020 Peter Kenji Yamanaka
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
 */

package com.pyamsoft.zaptorch.settings

import androidx.preference.PreferenceScreen
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.pydroid.ui.arch.PrefUiView
import javax.inject.Inject

class SettingsSpacer @Inject internal constructor(parent: PreferenceScreen) :
    PrefUiView<UnitViewState, SettingsViewEvent>(parent) {

  init {
    doOnInflate {
      val pref = PreferenceBottomSpace(parent.context)
      parent.addPreference(pref)
      doOnTeardown { parent.removePreference(pref) }
    }
  }
}
