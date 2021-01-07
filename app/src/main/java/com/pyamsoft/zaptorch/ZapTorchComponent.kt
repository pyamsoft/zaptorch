/*
 * Copyright 2020 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch

import android.content.BroadcastReceiver
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.zaptorch.ZapTorchComponent.ZaptorchModule
import com.pyamsoft.zaptorch.service.ServiceModule
import com.pyamsoft.zaptorch.service.notification.NotificationModule
import com.pyamsoft.zaptorch.main.MainComponent
import com.pyamsoft.zaptorch.main.MainFragmentComponent
import com.pyamsoft.zaptorch.service.ServiceComponent
import com.pyamsoft.zaptorch.settings.SettingsComponent
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [ZaptorchModule::class, ServiceModule::class, NotificationModule::class])
interface ZapTorchComponent {

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
            @Named("debug") @BindsInstance debug: Boolean,
            @BindsInstance context: Context,
            @BindsInstance theming: Theming,
            @BindsInstance imageLoader: ImageLoader,
            @BindsInstance receiverClass: Class<out BroadcastReceiver>,
            @BindsInstance notificationColor: Int,
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
            internal fun provideScrollBus(): EventBus<SignificantScrollEvent> {
                return EventBus.create(emitOnlyWhenActive = true)
            }
        }
    }
}
