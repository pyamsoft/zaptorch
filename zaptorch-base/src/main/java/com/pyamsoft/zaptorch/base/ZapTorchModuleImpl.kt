/*
 * Copyright (C) 2018 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import com.pyamsoft.pydroid.PYDroidModule
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.LoaderModule
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.ClearPreferences
import com.pyamsoft.zaptorch.api.UIPreferences
import com.pyamsoft.zaptorch.api.ZapTorchModule
import io.reactivex.Scheduler

class ZapTorchModuleImpl(
  private val pyDroidModule: PYDroidModule,
  private val loaderModule: LoaderModule,
  private val torchOffServiceClass: Class<out IntentService>
) : ZapTorchModule {

  private val preferences: ZapTorchPreferencesImpl = ZapTorchPreferencesImpl(
      pyDroidModule.provideContext()
  )

  @CheckResult
  override fun provideContext(): Context = pyDroidModule.provideContext()

  @CheckResult
  override fun provideCameraPreferences(): CameraPreferences = preferences

  @CheckResult
  override fun provideClearPreferences(): ClearPreferences = preferences

  @CheckResult
  override fun provideUiPreferences(): UIPreferences = preferences

  @CheckResult
  override fun provideTorchOffServiceClass(): Class<out IntentService> = torchOffServiceClass

  @CheckResult
  override fun provideMainThreadScheduler(): Scheduler = pyDroidModule.provideMainThreadScheduler()

  @CheckResult
  override fun provideIoScheduler(): Scheduler = pyDroidModule.provideIoScheduler()

  @CheckResult
  override fun provideComputationScheduler(): Scheduler =
    pyDroidModule.provideComputationScheduler()

  @CheckResult
  override fun provideImageLoader(): ImageLoader = loaderModule.provideImageLoader()
}
