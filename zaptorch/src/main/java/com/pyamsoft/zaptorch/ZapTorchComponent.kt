/*
 * Copyright 2017 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch

import com.pyamsoft.zaptorch.base.ZapTorchModule
import com.pyamsoft.zaptorch.main.MainActivity
import com.pyamsoft.zaptorch.main.MainModule
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.settings.ConfirmationDialog
import com.pyamsoft.zaptorch.settings.SettingsFragment
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragment
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragmentModule

class ZapTorchComponent internal constructor(zapTorchModule: ZapTorchModule) {

  private val mainModule: MainModule = MainModule(zapTorchModule)
  private val volumeServiceModule: VolumeServiceModule = VolumeServiceModule(zapTorchModule)
  private val settingsPreferenceFragmentModule: SettingsPreferenceFragmentModule = SettingsPreferenceFragmentModule(
      zapTorchModule)

  fun inject(settingsPreferenceFragment: SettingsPreferenceFragment) {
    settingsPreferenceFragment.presenter = settingsPreferenceFragmentModule.getPreferenceFragmentPresenter()
  }

  fun inject(settingsPreferenceFragment: SettingsFragment) {
    settingsPreferenceFragment.presenter = settingsPreferenceFragmentModule.getPresenter()
  }

  fun inject(confirmationDialog: ConfirmationDialog) {
    confirmationDialog.presenter = settingsPreferenceFragmentModule.getPresenter()
  }

  fun inject(volumeMonitorService: VolumeMonitorService) {
    volumeMonitorService.presenter = volumeServiceModule.getServicePresenter()
  }

  fun inject(mainActivity: MainActivity) {
    mainActivity.presenter = mainModule.getPresenter()
  }

  fun inject(torchOffService: TorchOffService) {
    torchOffService.servicePresenter = volumeServiceModule.getPresenter()
  }

}
