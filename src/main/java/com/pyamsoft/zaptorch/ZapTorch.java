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

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import com.pyamsoft.pydroid.base.ApplicationBase;
import com.pyamsoft.pydroid.crash.CrashHandler;
import com.pyamsoft.zaptorch.dagger.DaggerZapTorchComponent;
import com.pyamsoft.zaptorch.dagger.ZapTorchComponent;
import com.pyamsoft.zaptorch.dagger.ZapTorchModule;

public class ZapTorch extends ApplicationBase implements CrashHandler.Provider {

  private ZapTorchComponent zapTorchComponent;

  private static ZapTorchComponent zapTorchComponent(final Application application) {
    if (application instanceof ZapTorch) {
      final ZapTorch zapTorch = (ZapTorch) application;
      return zapTorch.zapTorchComponent;
    } else {
      throw new ClassCastException("Cannot cast Application to ZapTorch");
    }
  }

  public static ZapTorchComponent zapTorchComponent(final Activity activity) {
    return zapTorchComponent(activity.getApplication());
  }

  public static ZapTorchComponent zapTorchComponent(final Fragment fragment) {
    return zapTorchComponent(fragment.getActivity());
  }

  public static ZapTorchComponent zapTorchComponent(final Service service) {
    return zapTorchComponent(service.getApplication());
  }

  @Override protected boolean buildConfigDebug() {
    return BuildConfig.DEBUG;
  }

  @NonNull @Override public String appName() {
    return getString(R.string.app_name);
  }

  @NonNull @Override public String buildConfigApplicationId() {
    return BuildConfig.APPLICATION_ID;
  }

  @NonNull @Override public String buildConfigVersionName() {
    return BuildConfig.VERSION_NAME;
  }

  @Override public int buildConfigVersionCode() {
    return BuildConfig.VERSION_CODE;
  }

  @NonNull @Override public String getApplicationPackageName() {
    return getPackageName();
  }

  @Override public void onCreate() {
    super.onCreate();
    new CrashHandler(getApplicationContext(), this).register();

    if (buildConfigDebug()) {
      StrictMode.setThreadPolicy(
          new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build());
      StrictMode.setVmPolicy(
          new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build());
    }

    zapTorchComponent =
        DaggerZapTorchComponent.builder().zapTorchModule(new ZapTorchModule(this)).build();
  }
}
