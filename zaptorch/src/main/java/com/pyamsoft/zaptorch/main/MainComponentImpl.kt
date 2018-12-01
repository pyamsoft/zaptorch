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

package com.pyamsoft.zaptorch.main

import com.pyamsoft.pydroid.loader.LoaderModule
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.settings.SettingsModule

internal class MainComponentImpl internal constructor(
  private val theming: Theming,
  private val mainModule: MainModule,
  private val keyPressKey: String,
  private val settingsModule: SettingsModule,
  private val loaderModule: LoaderModule,
  private val serviceModule: VolumeServiceModule
) : MainComponent {

  override fun inject(activity: MainActivity) {
    activity.viewModel = mainModule.getViewModel(keyPressKey)
    activity.theming = theming
  }

  override fun inject(mainFragment: MainFragment) {
    mainFragment.publisher = settingsModule.getPublisher()
    mainFragment.imageLoader = loaderModule.provideImageLoader()
    mainFragment.serviceViewModel = serviceModule.getViewModel()
  }
}
