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

package com.pyamsoft.zaptorch

import android.app.Application
import android.app.IntentService
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.zaptorch.base.ZapTorchModuleImpl
import com.pyamsoft.zaptorch.main.MainComponent
import com.pyamsoft.zaptorch.main.MainComponentImpl
import com.pyamsoft.zaptorch.main.MainModule
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import com.pyamsoft.zaptorch.settings.ConfirmationDialog
import com.pyamsoft.zaptorch.settings.SettingsModule
import com.pyamsoft.zaptorch.settings.TorchPreferenceFragment

internal class ZapTorchComponentImpl internal constructor(
  application: Application,
  moduleProvider: ModuleProvider,
  serviceClass: Class<out IntentService>,
  notificationColor: Int
) : ZapTorchComponent {

  private val theming = moduleProvider.theming()
  private val loaderModule = moduleProvider.loaderModule()
  private val zapTorchModule =
    ZapTorchModuleImpl(application, serviceClass, notificationColor)
  private val mainModule = MainModule(moduleProvider.enforcer(), zapTorchModule)
  private val volumeServiceModule = VolumeServiceModule(zapTorchModule, moduleProvider.enforcer())
  private val settingsPreferenceFragmentModule =
    SettingsModule(moduleProvider.enforcer(), zapTorchModule)

  override fun inject(activity: CameraErrorExplanation) {
    activity.theming = theming
  }

  override fun inject(dialog: ConfirmationDialog) {
    dialog.publisher = settingsPreferenceFragmentModule.getPublisher()
  }

  override fun inject(service: TorchOffService) {
    service.servicePublisher = volumeServiceModule.getPublisher()
  }

  override fun inject(fragment: TorchPreferenceFragment) {
    fragment.publisher = volumeServiceModule.getPublisher()
    fragment.viewModel = settingsPreferenceFragmentModule.getViewModel()
    fragment.theming = theming
  }

  override fun inject(service: VolumeMonitorService) {
    service.viewModel = volumeServiceModule.getViewModel()
  }

  override fun plusMainComponent(
    key: String
  ): MainComponent = MainComponentImpl(
      theming,
      mainModule,
      key,
      settingsPreferenceFragmentModule,
      loaderModule,
      volumeServiceModule
  )
}
