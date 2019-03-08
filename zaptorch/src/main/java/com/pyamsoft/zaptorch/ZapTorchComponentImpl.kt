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

package com.pyamsoft.zaptorch

import android.app.Application
import android.app.IntentService
import android.view.ViewGroup
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.zaptorch.base.ZapTorchModuleImpl
import com.pyamsoft.zaptorch.main.MainComponent
import com.pyamsoft.zaptorch.main.MainComponentImpl
import com.pyamsoft.zaptorch.main.MainFragmentComponent
import com.pyamsoft.zaptorch.main.MainFragmentComponentImpl
import com.pyamsoft.zaptorch.main.MainModule
import com.pyamsoft.zaptorch.service.ServiceFinishEvent
import com.pyamsoft.zaptorch.service.ServiceFinishPresenterImpl
import com.pyamsoft.zaptorch.service.ServicePresenterImpl
import com.pyamsoft.zaptorch.service.ServiceStatePresenterImpl
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.service.TorchPresenterImpl
import com.pyamsoft.zaptorch.service.TorchToggleEvent
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.settings.ClearAllEvent
import com.pyamsoft.zaptorch.settings.ClearAllPresenterImpl
import com.pyamsoft.zaptorch.settings.ConfirmationDialog
import com.pyamsoft.zaptorch.settings.SettingsComponent
import com.pyamsoft.zaptorch.settings.SettingsComponentImpl
import com.pyamsoft.zaptorch.settings.SettingsModule
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent

internal class ZapTorchComponentImpl internal constructor(
  application: Application,
  moduleProvider: ModuleProvider,
  serviceClass: Class<out IntentService>,
  handleVolumeKeysKey: String,
  notificationColor: Int
) : ZapTorchComponent {

  private val clearAllBus = RxBus.create<ClearAllEvent>()
  private val significantScrollBus = RxBus.create<SignificantScrollEvent>()

  private val torchBus = RxBus.create<TorchToggleEvent>()
  private val serviceFinishBus = RxBus.create<ServiceFinishEvent>()

  private val schedulerProvider = moduleProvider.schedulerProvider()
  private val navModule = moduleProvider.failedNavigationModule()
  private val theming = moduleProvider.theming()
  private val loaderModule = moduleProvider.loaderModule()
  private val zapTorchModule = ZapTorchModuleImpl(application, serviceClass, notificationColor)
  private val settingsModule = SettingsModule(moduleProvider.enforcer(), zapTorchModule)
  private val serviceModule = VolumeServiceModule(zapTorchModule, moduleProvider.enforcer())
  private val mainModule = MainModule(
      handleVolumeKeysKey, moduleProvider.enforcer(), zapTorchModule
  )

  override fun inject(dialog: ConfirmationDialog) {
    dialog.apply {
      this.presenter = ClearAllPresenterImpl(settingsModule.interactor, clearAllBus)
    }
  }

  override fun inject(service: TorchOffService) {
    service.apply {
      this.presenter = TorchPresenterImpl(serviceModule.interactor)
    }
  }

  override fun inject(service: VolumeMonitorService) {
    service.apply {
      this.finishPresenter = ServiceFinishPresenterImpl(serviceFinishBus)
      this.servicePresenter = ServicePresenterImpl(serviceModule.interactor)
      this.statePresenter = ServiceStatePresenterImpl(serviceModule.interactor)
      this.torchPresenter = TorchPresenterImpl(serviceModule.interactor)
    }
  }

  override fun plusMainComponent(parent: ViewGroup): MainComponent =
    MainComponentImpl(theming, parent, mainModule.interactor, navModule.bus, schedulerProvider)

  override fun plusMainFragmentComponent(parent: ViewGroup): MainFragmentComponent =
    MainFragmentComponentImpl(
        parent, loaderModule.provideImageLoader(), serviceModule.interactor, significantScrollBus
    )

  override fun plusSettingsComponent(
    recyclerView: RecyclerView,
    preferenceScreen: PreferenceScreen
  ): SettingsComponent = SettingsComponentImpl(
      recyclerView, preferenceScreen, settingsModule.interactor,
      clearAllBus, significantScrollBus, serviceFinishBus
  )

}
