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
import com.pyamsoft.zaptorch.service.VolumeServiceComponent;
import com.pyamsoft.zaptorch.settings.SettingsComponent;

public class ZapTorchComponent {

  @NonNull private final MainComponent mainComponent;
  @NonNull private final VolumeServiceComponent volumeComponent;
  @NonNull private final SettingsComponent settingsComponent;

  ZapTorchComponent(@NonNull ZapTorchModule zapTorchModule) {
    mainComponent = new MainComponent(zapTorchModule);
    volumeComponent = new VolumeServiceComponent(zapTorchModule);
    settingsComponent = new SettingsComponent(zapTorchModule);
  }

  @CheckResult @NonNull static Builder builder() {
    return new Builder();
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

  public static class Builder {

    private ZapTorchModule zapTorchModule;

    @CheckResult @NonNull Builder zapTorchModule(@NonNull ZapTorchModule module) {
      zapTorchModule = module;
      return this;
    }

    @CheckResult @NonNull ZapTorchComponent build() {
      if (zapTorchModule == null) {
        throw new IllegalStateException("ZapTorchModule cannot be NULL");
      }

      return new ZapTorchComponent(zapTorchModule);
    }
  }
}
