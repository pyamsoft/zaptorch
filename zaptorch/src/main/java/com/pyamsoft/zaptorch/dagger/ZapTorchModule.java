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

import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import com.pyamsoft.zaptorch.dagger.main.MainModule;
import com.pyamsoft.zaptorch.dagger.service.VolumeServiceModule;
import com.pyamsoft.zaptorch.dagger.settings.SettingsPreferenceFragmentModule;

public class ZapTorchModule {

  @NonNull private final Provider provider;

  public ZapTorchModule(final @NonNull Context context) {
    this.provider = new Provider(context);
  }

  @CheckResult @NonNull
  public SettingsPreferenceFragmentModule provideSettingsPreferenceFragmentModule() {
    return new SettingsPreferenceFragmentModule(provider);
  }

  @CheckResult @NonNull public MainModule provideMainModule() {
    return new MainModule(provider);
  }

  @CheckResult @NonNull public VolumeServiceModule provideVolumeServiceModule() {
    return new VolumeServiceModule(provider);
  }

  public static class Provider {

    @NonNull private final Context appContext;
    @NonNull private final ZapTorchPreferences preferences;

    Provider(final @NonNull Context context) {
      appContext = context.getApplicationContext();
      preferences = new ZapTorchPreferencesImpl(context);
    }

    @CheckResult @NonNull public Context provideContext() {
      return appContext;
    }

    @CheckResult @NonNull public ZapTorchPreferences providePreferences() {
      return preferences;
    }
  }
}
