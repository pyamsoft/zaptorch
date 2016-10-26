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
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.IPYDroidApp;
import com.pyamsoft.pydroid.SingleInitContentProvider;
import com.pyamsoft.zaptorch.dagger.ZapTorchModule;

public class ZapTorchSingleInitProvider extends SingleInitContentProvider
    implements IPYDroidApp<ZapTorchModule> {

  @Nullable private static volatile ZapTorchSingleInitProvider instance = null;
  @Nullable private ZapTorchModule module;

  @NonNull @CheckResult public static IPYDroidApp<ZapTorchModule> get() {
    if (instance == null) {
      throw new NullPointerException("Instance is NULL");
    }

    //noinspection ConstantConditions
    return instance;
  }

  private static void setInstance(@NonNull ZapTorchSingleInitProvider instance) {
    ZapTorchSingleInitProvider.instance = instance;
  }

  @Override protected void onInstanceCreated(@NonNull Context context) {
    setInstance(this);
  }

  @Override protected void onFirstCreate(@NonNull Context context) {
    super.onFirstCreate(context);
    module = new ZapTorchModule(context);
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

  }
}
