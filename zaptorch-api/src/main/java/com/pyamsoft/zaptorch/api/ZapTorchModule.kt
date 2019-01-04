/*
 * Copyright 2019 Peter Kenji Yamanaka
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
 *
 */

package com.pyamsoft.zaptorch.api

import android.app.Application
import android.app.IntentService
import android.content.Context
import androidx.annotation.CheckResult
import androidx.annotation.ColorRes
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.zaptorch.model.FabScrollListenerRequestEvent

interface ZapTorchModule {

  @CheckResult
  fun provideApplication(): Application

  @CheckResult
  fun provideContext(): Context

  @CheckResult
  fun provideCameraPreferences(): CameraPreferences

  @CheckResult
  fun provideClearPreferences(): ClearPreferences

  @CheckResult
  fun provideUiPreferences(): UIPreferences

  @CheckResult
  fun provideTorchOffServiceClass(): Class<out IntentService>

  @ColorRes
  @CheckResult
  fun provideNotificationColor(): Int

  @CheckResult
  fun provideFabScrollRequestBus(): EventBus<FabScrollListenerRequestEvent>
}
