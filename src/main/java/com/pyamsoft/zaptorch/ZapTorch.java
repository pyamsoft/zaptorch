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
import com.pyamsoft.pydroid.PYDroidApplication;
import com.pyamsoft.zaptorch.dagger.DaggerZapTorchComponent;
import com.pyamsoft.zaptorch.dagger.ZapTorchComponent;
import com.pyamsoft.zaptorch.dagger.ZapTorchModule;

public class ZapTorch extends PYDroidApplication implements IZapTorch<ZapTorchComponent> {

  private ZapTorchComponent component;

  @NonNull @CheckResult public static IZapTorch get(@NonNull Context context) {
    final Context appContext = context.getApplicationContext();
    if (appContext instanceof IZapTorch) {
      return ZapTorch.class.cast(appContext);
    } else {
      throw new ClassCastException("Cannot cast Application Context to IZapTorch");
    }
  }

  @Override protected void createApplicationComponents() {
    super.createApplicationComponents();
    component = DaggerZapTorchComponent.builder()
        .zapTorchModule(new ZapTorchModule(getApplicationContext()))
        .build();
  }

  @NonNull @Override public ZapTorchComponent provideComponent() {
    if (component == null) {
      throw new NullPointerException("ZapTorchComponent is NULL");
    }
    return component;
  }

  @Nullable @Override public String provideGoogleOpenSourceLicenses() {
    return null;
  }

  @Override public void insertCustomLicensesIntoMap() {

  }
}
