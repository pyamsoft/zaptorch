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

package com.pyamsoft.zaptorch.base;

import android.app.IntentService;
import android.content.Context;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ZapTorchModule {

  @NonNull private final Context appContext;
  @NonNull private final ZapTorchPreferences preferences;
  @NonNull private final Class<? extends IntentService> torchOffServiceClass;

  public ZapTorchModule(@NonNull Context context,
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

  @CheckResult @NonNull public Scheduler provideObsScheduler() {
    return AndroidSchedulers.mainThread();
  }

  @CheckResult @NonNull public Scheduler provideSubScheduler() {
    return Schedulers.io();
  }
}
