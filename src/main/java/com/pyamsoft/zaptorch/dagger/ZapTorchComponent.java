/*
 * Copyright 2016 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.dagger;

import com.pyamsoft.zaptorch.dagger.settings.SettingsPreferenceFragmentComponent;
import com.pyamsoft.zaptorch.dagger.main.MainComponent;
import com.pyamsoft.zaptorch.dagger.service.VolumeServiceComponent;
import dagger.Component;
import javax.inject.Singleton;

@Singleton @Component(modules = ZapTorchModule.class) public interface ZapTorchComponent {

  // Subcomponent VolumeService
  VolumeServiceComponent plusVolumeServiceComponent();

  // Subcomponent MainFragment
  SettingsPreferenceFragmentComponent plusSettingsComponent();

  // Subcomponent Main
  MainComponent plusMainComponent();
}
