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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.BuildConfigChecker;
import com.pyamsoft.pydroid.IPYDroidApp;
import com.pyamsoft.pydroid.SingleInitContentProvider;
import com.pyamsoft.pydroid.ui.UiLicenses;
import com.pyamsoft.zaptorch.service.TorchOffService;
import com.pyamsoft.zaptorch.presenter.Injector;
import com.pyamsoft.zaptorch.presenter.ZapTorchModule;

public class ZapTorchSingleInitProvider extends SingleInitContentProvider
    implements IPYDroidApp<ZapTorchModule> {

  @Nullable private ZapTorchModule module;

  @NonNull @Override protected BuildConfigChecker initializeBuildConfigChecker() {
    return new BuildConfigChecker() {
      @Override public boolean isDebugMode() {
        return BuildConfig.DEBUG;
      }
    };
  }

  @Override protected void onInstanceCreated(@NonNull Context context) {
    Injector.set(module);
  }

  @Override protected void onFirstCreate(@NonNull Context context) {
    super.onFirstCreate(context);
    module = new ZapTorchModule(context, TorchOffService.class);
  }

  @NonNull @Override public ZapTorchModule provideComponent() {
    if (module == null) {
      throw new NullPointerException("ZapTorchComponent is NULL");
    }
    return module;
  }

  @Nullable @Override public String provideGoogleOpenSourceLicenses(@NonNull Context context) {
    return null;
  }

  @Override public void insertCustomLicensesIntoMap() {
    UiLicenses.addLicenses();
  }
}
