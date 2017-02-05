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

package com.pyamsoft.zaptorch.settings;

import android.support.annotation.NonNull;
import com.pyamsoft.zaptorch.base.ZapTorchModule;

public class SettingsComponent {

  @NonNull private final SettingsPreferenceFragmentModule serviceModule;

  public SettingsComponent(@NonNull ZapTorchModule module) {
    serviceModule = new SettingsPreferenceFragmentModule(module);
  }

  void inject(@NonNull SettingsFragment fragment) {
    fragment.presenter = serviceModule.getSettingsFragmentPresenter();
  }

  void inject(@NonNull SettingsPreferenceFragment fragment) {
    fragment.presenter = serviceModule.getPreferenceFragmentPresenter();
  }
}