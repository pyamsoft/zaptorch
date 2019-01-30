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
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.pydroid.ui.ModuleProvider
import com.pyamsoft.zaptorch.base.ZapTorchModuleImpl
import com.pyamsoft.zaptorch.main.ActionViewEvent
import com.pyamsoft.zaptorch.main.MainComponent
import com.pyamsoft.zaptorch.main.MainComponentImpl
import com.pyamsoft.zaptorch.main.MainFragmentComponent
import com.pyamsoft.zaptorch.main.MainFragmentComponentImpl
import com.pyamsoft.zaptorch.main.MainModule
import com.pyamsoft.zaptorch.main.MainStateEvent
import com.pyamsoft.zaptorch.main.MainViewEvent
import com.pyamsoft.zaptorch.service.ServiceFinishEvent
import com.pyamsoft.zaptorch.service.ServiceFinishWorker
import com.pyamsoft.zaptorch.service.ServiceStateEvent
import com.pyamsoft.zaptorch.service.ServiceStateWorker
import com.pyamsoft.zaptorch.service.ServiceWorker
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.service.TorchWorker
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.settings.ClearAllWorker
import com.pyamsoft.zaptorch.settings.ConfirmationDialog
import com.pyamsoft.zaptorch.settings.SettingsComponent
import com.pyamsoft.zaptorch.settings.SettingsComponentImpl
import com.pyamsoft.zaptorch.settings.SettingsModule
import com.pyamsoft.zaptorch.settings.SettingsStateEvent
import com.pyamsoft.zaptorch.settings.SettingsViewEvent

internal class ZapTorchComponentImpl internal constructor(
  application: Application,
  moduleProvider: ModuleProvider,
  serviceClass: Class<out IntentService>,
  handleVolumeKeysKey: String,
  notificationColor: Int
) : ZapTorchComponent {

  private val mainViewBus = RxBus.create<MainViewEvent>()
  private val mainStateBus = RxBus.create<MainStateEvent>()

  private val actionViewBus = RxBus.create<ActionViewEvent>()

  private val settingsViewBus = RxBus.create<SettingsViewEvent>()
  private val settingsStateBus = RxBus.create<SettingsStateEvent>()

  private val serviceFinishBus = RxBus.create<ServiceFinishEvent>()
  private val serviceStateBus = RxBus.create<ServiceStateEvent>()

  private val theming = moduleProvider.theming()
  private val loaderModule = moduleProvider.loaderModule()
  private val zapTorchModule = ZapTorchModuleImpl(application, serviceClass, notificationColor)
  private val settingsModule = SettingsModule(moduleProvider.enforcer(), zapTorchModule)
  private val serviceModule = VolumeServiceModule(zapTorchModule, moduleProvider.enforcer())
  private val mainModule = MainModule(
      handleVolumeKeysKey, moduleProvider.enforcer(), zapTorchModule
  )

  override fun inject(dialog: ConfirmationDialog) {
    dialog.worker = ClearAllWorker(settingsModule.interactor, settingsStateBus)
  }

  override fun inject(service: TorchOffService) {
    service.worker = TorchWorker(serviceStateBus)
  }

  override fun inject(service: VolumeMonitorService) {
    service.serviceWorker = ServiceWorker(serviceModule.interactor)
    service.stateWorker = ServiceStateWorker(serviceModule.interactor)
    service.finishWorker = ServiceFinishWorker(serviceFinishBus)
    service.torchWorker = TorchWorker(serviceStateBus)
  }

  override fun plusMainComponent(
    parent: ViewGroup,
    owner: LifecycleOwner
  ): MainComponent = MainComponentImpl(
      theming, parent, owner, mainModule, mainViewBus, mainStateBus
  )

  override fun plusMainFragmentComponent(
    parent: ViewGroup,
    owner: LifecycleOwner
  ): MainFragmentComponent = MainFragmentComponentImpl(
      parent, owner, loaderModule, serviceModule, actionViewBus, settingsStateBus, mainStateBus
  )

  override fun plusSettingsComponent(
    owner: LifecycleOwner,
    recyclerView: RecyclerView,
    preferenceScreen: PreferenceScreen
  ): SettingsComponent = SettingsComponentImpl(
      owner, recyclerView, preferenceScreen, settingsModule,
      settingsViewBus, settingsStateBus, serviceFinishBus
  )
}
