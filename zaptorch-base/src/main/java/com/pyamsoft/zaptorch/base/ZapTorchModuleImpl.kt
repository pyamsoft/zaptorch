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

import android.app.Application
import android.app.IntentService
import android.content.Context
import com.pyamsoft.pydroid.PYDroidModule
import com.pyamsoft.pydroid.data.Cache
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

  override fun provideApplication(): Application = pyDroidModule.provideApplication()

  override fun provideContext(): Context = pyDroidModule.provideContext()

  override fun provideMainThreadScheduler(): Scheduler = pyDroidModule.provideMainThreadScheduler()

  override fun provideIoScheduler(): Scheduler = pyDroidModule.provideIoScheduler()

  override fun provideComputationScheduler(): Scheduler =
    pyDroidModule.provideComputationScheduler()

  override fun provideImageLoader(): ImageLoader = loaderModule.provideImageLoader()

  override fun provideImageLoaderCache(): Cache = loaderModule.provideImageLoaderCache()

  override fun provideCameraPreferences(): CameraPreferences = preferences

  override fun provideClearPreferences(): ClearPreferences = preferences

  override fun provideUiPreferences(): UIPreferences = preferences

  override fun provideTorchOffServiceClass(): Class<out IntentService> = torchOffServiceClass
}
