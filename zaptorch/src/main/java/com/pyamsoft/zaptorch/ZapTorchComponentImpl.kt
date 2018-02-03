/*
 *     Copyright (C) 2017 Peter Kenji Yamanaka
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.pyamsoft.zaptorch

import com.pyamsoft.zaptorch.api.ZapTorchModule
import com.pyamsoft.zaptorch.main.MainComponent
import com.pyamsoft.zaptorch.main.MainComponentImpl
import com.pyamsoft.zaptorch.main.MainFragment
import com.pyamsoft.zaptorch.main.MainModule
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.settings.ConfirmationDialog
import com.pyamsoft.zaptorch.settings.SettingsComponent
import com.pyamsoft.zaptorch.settings.SettingsComponentImpl
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragmentModule

internal class ZapTorchComponentImpl internal constructor(
  private val zapTorchModule: ZapTorchModule
) : ZapTorchComponent {

  private val mainModule: MainModule = MainModule(zapTorchModule)
  private val volumeServiceModule: VolumeServiceModule = VolumeServiceModule(zapTorchModule)
  private val settingsPreferenceFragmentModule: SettingsPreferenceFragmentModule =
    SettingsPreferenceFragmentModule(
        zapTorchModule
    )

  override fun inject(mainFragment: MainFragment) {
    mainFragment.publisher = settingsPreferenceFragmentModule.getPresenter()
    mainFragment.imageLoader = zapTorchModule.provideImageLoader()
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

  override fun plusSettingsComponent(): SettingsComponent =
    SettingsComponentImpl(settingsPreferenceFragmentModule, volumeServiceModule)

  override fun plusMainComponent(key: String): MainComponent = MainComponentImpl(mainModule, key)
}
