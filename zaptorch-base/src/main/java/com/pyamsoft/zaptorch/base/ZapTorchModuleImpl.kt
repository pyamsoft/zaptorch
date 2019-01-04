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

package com.pyamsoft.zaptorch.base

import android.app.Application
import android.app.IntentService
import android.content.Context
import androidx.annotation.ColorRes
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.ClearPreferences
import com.pyamsoft.zaptorch.api.UIPreferences
import com.pyamsoft.zaptorch.api.ZapTorchModule
import com.pyamsoft.zaptorch.model.FabScrollListenerRequestEvent

class ZapTorchModuleImpl(
  private val application: Application,
  private val torchOffServiceClass: Class<out IntentService>,
  @ColorRes private val notificationColor: Int
) : ZapTorchModule {

  private val fabScrollBus = RxBus.create<FabScrollListenerRequestEvent>()
  private val preferences = ZapTorchPreferencesImpl(application)

  override fun provideApplication(): Application = application

  override fun provideContext(): Context = provideApplication()

  override fun provideCameraPreferences(): CameraPreferences = preferences

  override fun provideClearPreferences(): ClearPreferences = preferences

  override fun provideUiPreferences(): UIPreferences = preferences

  override fun provideTorchOffServiceClass(): Class<out IntentService> = torchOffServiceClass

  override fun provideNotificationColor(): Int = notificationColor

  override fun provideFabScrollRequestBus(): EventBus<FabScrollListenerRequestEvent> = fabScrollBus
}
