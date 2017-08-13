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
import com.pyamsoft.zaptorch.main.MainComponent
import com.pyamsoft.zaptorch.main.MainComponentImpl
import com.pyamsoft.zaptorch.main.MainModule
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.settings.ConfirmationDialog
import com.pyamsoft.zaptorch.settings.SettingsComponent
import com.pyamsoft.zaptorch.settings.SettingsComponentImpl
import com.pyamsoft.zaptorch.settings.SettingsFragment
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragmentModule

internal class ZapTorchComponentImpl internal constructor(
    zapTorchModule: ZapTorchModule) : ZapTorchComponent {

  private val mainModule: MainModule = MainModule(zapTorchModule)
  private val volumeServiceModule: VolumeServiceModule = VolumeServiceModule(zapTorchModule)
  private val settingsPreferenceFragmentModule: SettingsPreferenceFragmentModule = SettingsPreferenceFragmentModule(
      zapTorchModule)

  override fun inject(settingsFragment: SettingsFragment) {
    settingsFragment.publisher = settingsPreferenceFragmentModule.getPresenter()
  }

  override fun inject(confirmationDialog: ConfirmationDialog) {
    confirmationDialog.publisher = settingsPreferenceFragmentModule.getPresenter()
  }

  override fun inject(volumeMonitorService: VolumeMonitorService) {
    volumeMonitorService.presenter = volumeServiceModule.getServicePresenter()
  }

  override fun inject(torchOffService: TorchOffService) {
    torchOffService.servicePublisher = volumeServiceModule.getPresenter()
  }

  override fun plusSettingsComponent(cameraApiKey: String): SettingsComponent {
    return SettingsComponentImpl(settingsPreferenceFragmentModule, volumeServiceModule,
        cameraApiKey)
  }

  override fun plusMainComponent(key: String): MainComponent {
    return MainComponentImpl(mainModule, key)
  }

}
