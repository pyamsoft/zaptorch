/*
 * Copyright 2017 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.base

import android.app.IntentService
import android.content.Context
import android.support.annotation.CheckResult
import com.pyamsoft.zaptorch.base.preference.CameraPreferences
import com.pyamsoft.zaptorch.base.preference.ClearPreferences
import com.pyamsoft.zaptorch.base.preference.UIPreferences
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ZapTorchModule(context: Context,
    private val torchOffServiceClass: Class<out IntentService>) {

  private val appContext: Context = context.applicationContext
  private val preferences: ZapTorchPreferencesImpl = ZapTorchPreferencesImpl(context)

  @CheckResult fun provideContext(): Context {
    return appContext
  }

  @CheckResult fun provideCameraPreferences(): CameraPreferences {
    return preferences
  }

  @CheckResult fun provideClearPreferences(): ClearPreferences {
    return preferences
  }

  @CheckResult fun provideUiPreferences(): UIPreferences {
    return preferences
  }

  @CheckResult fun provideTorchOffServiceClass(): Class<out IntentService> {
    return torchOffServiceClass
  }

  @CheckResult fun provideObsScheduler(): Scheduler {
    return AndroidSchedulers.mainThread()
  }

  @CheckResult fun provideSubScheduler(): Scheduler {
    return Schedulers.io()
  }
}
