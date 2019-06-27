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

package com.pyamsoft.zaptorch

import android.app.IntentService
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.EventBus
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.zaptorch.ZapTorchComponent.ZaptorchModule
import com.pyamsoft.zaptorch.base.BaseModule
import com.pyamsoft.zaptorch.main.MainComponent
import com.pyamsoft.zaptorch.main.MainFragmentComponent
import com.pyamsoft.zaptorch.service.ServiceComponent
import com.pyamsoft.zaptorch.service.ServiceFinishEvent
import com.pyamsoft.zaptorch.settings.ClearAllEvent
import com.pyamsoft.zaptorch.settings.ConfirmationDialog
import com.pyamsoft.zaptorch.settings.SettingsComponent
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Singleton
@Component(modules = [ZaptorchModule::class, BaseModule::class])
interface ZapTorchComponent {

  fun inject(dialog: ConfirmationDialog)

  @CheckResult
  fun plusMainComponent(): MainComponent.Factory

  @CheckResult
  fun plusMainFragmentComponent(): MainFragmentComponent.Factory

  @CheckResult
  fun plusSettingsComponent(): SettingsComponent.Factory

  @CheckResult
  fun plusServiceComponent(): ServiceComponent.Factory

  @Component.Factory
  interface Factory {

    @CheckResult
    fun create(
      @BindsInstance context: Context,
      @BindsInstance theming: Theming,
      @BindsInstance enforcer: Enforcer,
      @BindsInstance imageLoader: ImageLoader,
      @BindsInstance serviceClass: Class<out IntentService>,
      @BindsInstance notificationColor: Int,
      @BindsInstance handleKeyPressKey: String
    ): ZapTorchComponent
  }

  @Module
  abstract class ZaptorchModule {

    @Module
    companion object {

      @Provides
      @Singleton
      @JvmStatic
      @CheckResult
      internal fun provideClearBus(): EventBus<ClearAllEvent> {
        return EventBus.create()
      }

      @Provides
      @Singleton
      @JvmStatic
      @CheckResult
      internal fun provideServiceBus(): EventBus<ServiceFinishEvent> {
        return EventBus.create()
      }

      @Provides
      @Singleton
      @JvmStatic
      @CheckResult
      internal fun provideScrollBus(): EventBus<SignificantScrollEvent> {
        return EventBus.create()
      }

    }
  }
}

