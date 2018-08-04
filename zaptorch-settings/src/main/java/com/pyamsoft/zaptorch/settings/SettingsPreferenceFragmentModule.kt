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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.threads.Enforcer
import com.pyamsoft.zaptorch.api.SettingsPreferenceFragmentInteractor
import com.pyamsoft.zaptorch.api.ZapTorchModule

class SettingsPreferenceFragmentModule(
  enforcer: Enforcer,
  module: ZapTorchModule
) {

  private val interactor: SettingsPreferenceFragmentInteractor
  private val bus = SettingsBus()

  init {
    interactor =
        SettingsPreferenceFragmentInteractorImpl(enforcer, module.provideClearPreferences())
  }

  @CheckResult
  fun getPreferenceFragmentPresenter(): SettingsPreferenceFragmentPresenter =
    SettingsPreferenceFragmentPresenter(bus, interactor)

  @CheckResult
  fun getPresenter(): SettingPublisher = SettingPublisher(bus)
}
