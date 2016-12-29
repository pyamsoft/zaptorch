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

package com.pyamsoft.zaptorch.presenter.settings;

import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.app.PersistLoader;
import com.pyamsoft.zaptorch.presenter.Injector;

public class SettingsPreferenceFragmentPresenterLoader
    extends PersistLoader<SettingsPreferenceFragmentPresenter> {

  public SettingsPreferenceFragmentPresenterLoader() {
  }

  @NonNull @Override public SettingsPreferenceFragmentPresenter loadPersistent() {
    return Injector.get()
        .provideComponent()
        .provideSettingsPreferenceFragmentModule()
        .getPreferenceFragmentPresenter();
  }
}