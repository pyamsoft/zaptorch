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

package com.pyamsoft.zaptorchpresenter;

import android.app.IntentService;
import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.pyamsoft.zaptorchpresenter.main.MainModule;
import com.pyamsoft.zaptorchpresenter.service.VolumeServiceModule;
import com.pyamsoft.zaptorchpresenter.settings.SettingsPreferenceFragmentModule;

public class ZapTorchModule {

  @NonNull private final Provider provider;

  public ZapTorchModule(@NonNull Context context,
      @NonNull Class<? extends IntentService> torchOffServiceClass) {
    this.provider = new Provider(context, torchOffServiceClass);
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
    @NonNull private final Class<? extends IntentService> torchOffServiceClass;

    Provider(@NonNull Context context,
        @NonNull Class<? extends IntentService> torchOffServiceClass) {
      appContext = context.getApplicationContext();
      preferences = new ZapTorchPreferencesImpl(context);
      this.torchOffServiceClass = torchOffServiceClass;
    }

    @CheckResult @NonNull public Context provideContext() {
      return appContext;
    }

    @CheckResult @NonNull public ZapTorchPreferences providePreferences() {
      return preferences;
    }

    @CheckResult @NonNull public Class<? extends IntentService> provideTorchOffServiceClass() {
      return torchOffServiceClass;
    }
  }
}
