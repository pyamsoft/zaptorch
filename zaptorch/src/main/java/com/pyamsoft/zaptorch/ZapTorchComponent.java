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

package com.pyamsoft.zaptorch;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.pyamsoft.zaptorch.base.ZapTorchModule;
import com.pyamsoft.zaptorch.main.MainComponent;
import com.pyamsoft.zaptorch.main.MainModule;
import com.pyamsoft.zaptorch.service.VolumeServiceComponent;
import com.pyamsoft.zaptorch.service.VolumeServiceModule;
import com.pyamsoft.zaptorch.settings.SettingsComponent;
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragmentModule;

public class ZapTorchComponent {

  @NonNull private final MainComponent mainComponent;
  @NonNull private final VolumeServiceComponent volumeComponent;
  @NonNull private final SettingsComponent settingsComponent;

  private ZapTorchComponent(@NonNull ZapTorchModule zapTorchModule) {
    MainModule mainModule = new MainModule(zapTorchModule);
    VolumeServiceModule volumeServiceModule = new VolumeServiceModule(zapTorchModule);
    SettingsPreferenceFragmentModule settingsPreferenceFragmentModule =
        new SettingsPreferenceFragmentModule(zapTorchModule);
    mainComponent = new MainComponent(mainModule);
    volumeComponent = new VolumeServiceComponent(volumeServiceModule);
    settingsComponent = new SettingsComponent(settingsPreferenceFragmentModule);
  }

  @CheckResult @NonNull static ZapTorchComponent withModule(@NonNull ZapTorchModule module) {
    return new ZapTorchComponent(module);
  }

  @CheckResult @NonNull public MainComponent plusMainComponent() {
    return mainComponent;
  }

  @CheckResult @NonNull public VolumeServiceComponent plusVolumeServiceComponent() {
    return volumeComponent;
  }

  @CheckResult @NonNull public SettingsComponent plusSettingsComponent() {
    return settingsComponent;
  }
}
