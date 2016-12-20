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

package com.pyamsoft.zaptorchpresenter.settings;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.pyamsoft.zaptorchpresenter.ZapTorchModule;

public class SettingsPreferenceFragmentModule {

  @NonNull private final SettingsPreferenceFragmentInteractor interactor;
  @NonNull private final SettingsPreferenceFragmentPresenter preferenceFragmentPresenter;
  @NonNull private final SettingsFragmentPresenter fragmentPresenter;

  public SettingsPreferenceFragmentModule(@NonNull ZapTorchModule.Provider provider) {
    interactor = new SettingsPreferenceFragmentInteractorImpl(provider.provideContext(),
        provider.providePreferences());
    fragmentPresenter = new SettingsFragmentPresenterImpl();
    preferenceFragmentPresenter = new SettingsPreferenceFragmentPresenterImpl(interactor);
  }

  @CheckResult @NonNull public SettingsFragmentPresenter getSettingsFragmentPresenter() {
    return fragmentPresenter;
  }

  @CheckResult @NonNull
  public SettingsPreferenceFragmentPresenter getPreferenceFragmentPresenter() {
    return preferenceFragmentPresenter;
  }
}
