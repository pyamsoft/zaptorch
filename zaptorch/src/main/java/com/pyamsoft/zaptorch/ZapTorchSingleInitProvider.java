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
import com.pyamsoft.pydroid.ui.UiLicenses;
import com.pyamsoft.zaptorch.base.BaseInitContentProvider;
import com.pyamsoft.zaptorch.base.ZapTorchModule;
import com.pyamsoft.zaptorch.service.TorchOffService;

public class ZapTorchSingleInitProvider extends BaseInitContentProvider
    implements IPYDroidApp<ZapTorchModule> {

  @NonNull @Override protected BuildConfigChecker initializeBuildConfigChecker() {
    return new BuildConfigChecker() {
      @Override public boolean isDebugMode() {
        return BuildConfig.DEBUG;
      }
    };
  }

  @NonNull @Override protected ZapTorchModule createModule(@NonNull Context context) {
    return new ZapTorchModule(context, TorchOffService.class);
  }

  @Nullable @Override public String provideGoogleOpenSourceLicenses(@NonNull Context context) {
    return null;
  }

  @Override public void insertCustomLicensesIntoMap() {
    UiLicenses.addLicenses();
  }
}
