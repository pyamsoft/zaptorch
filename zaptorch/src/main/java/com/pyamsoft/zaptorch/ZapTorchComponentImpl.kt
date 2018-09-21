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
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.zaptorch.base.ZapTorchModuleImpl
import com.pyamsoft.zaptorch.main.MainComponent
import com.pyamsoft.zaptorch.main.MainComponentImpl
import com.pyamsoft.zaptorch.main.MainFragment
import com.pyamsoft.zaptorch.main.MainModule
import com.pyamsoft.zaptorch.service.ServiceComponent
import com.pyamsoft.zaptorch.service.ServiceComponentImpl
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.settings.ConfirmationDialog
import com.pyamsoft.zaptorch.settings.SettingsComponent
import com.pyamsoft.zaptorch.settings.SettingsComponentImpl
import com.pyamsoft.zaptorch.settings.SettingsModule

internal class ZapTorchComponentImpl internal constructor(
  application: Application,
  moduleProvider: ModuleProvider,
  serviceClass: Class<out IntentService>,
  notificationColor: Int
) : ZapTorchComponent {

  private val zapTorchModule =
    ZapTorchModuleImpl(application, moduleProvider.loaderModule(), serviceClass, notificationColor)
  private val mainModule = MainModule(moduleProvider.enforcer(), zapTorchModule)
  private val volumeServiceModule = VolumeServiceModule(zapTorchModule, moduleProvider.enforcer())
  private val settingsPreferenceFragmentModule =
    SettingsModule(moduleProvider.enforcer(), zapTorchModule)

  override fun inject(mainFragment: MainFragment) {
    mainFragment.publisher = settingsPreferenceFragmentModule.getPublisher()
    mainFragment.imageLoader = zapTorchModule.provideImageLoader()
  }

  override fun inject(confirmationDialog: ConfirmationDialog) {
    confirmationDialog.publisher = settingsPreferenceFragmentModule.getPublisher()
  }

  override fun inject(torchOffService: TorchOffService) {
    torchOffService.servicePublisher = volumeServiceModule.getPublisher()
  }

  override fun plusSettingsComponent(owner: LifecycleOwner): SettingsComponent =
    SettingsComponentImpl(owner, settingsPreferenceFragmentModule, volumeServiceModule)

  override fun plusMainComponent(
    owner: LifecycleOwner,
    key: String
  ): MainComponent = MainComponentImpl(owner, mainModule, key)

  override fun plusServiceComponent(owner: LifecycleOwner): ServiceComponent =
    ServiceComponentImpl(owner, volumeServiceModule)
}
