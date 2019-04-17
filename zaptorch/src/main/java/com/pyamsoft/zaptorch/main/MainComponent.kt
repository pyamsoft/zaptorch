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

package com.pyamsoft.zaptorch.main

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.UiEventHandler
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.zaptorch.main.MainComponent.MainModule
import com.pyamsoft.zaptorch.main.MainToolbarHandler.ToolbarEvent
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent

@Subcomponent(modules = [MainModule::class])
interface MainComponent {

  fun inject(activity: MainActivity)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
      @BindsInstance owner: LifecycleOwner,
      @BindsInstance parent: ViewGroup,
      @BindsInstance toolbarActivityProvider: ToolbarActivityProvider
    ): MainComponent

  }

  @Module
  abstract class MainModule {

    @Binds
    @CheckResult
    internal abstract fun bindUiComponent(impl: MainUiComponentImpl): MainUiComponent

    @Binds
    @CheckResult
    internal abstract fun bindToolbarComponent(impl: MainToolbarUiComponentImpl): MainToolbarUiComponent

    @Binds
    @CheckResult
    internal abstract fun bindCallback(impl: MainToolbarHandler): MainToolbarView.Callback

    @Binds
    @CheckResult
    internal abstract fun bindHandler(impl: MainToolbarHandler): UiEventHandler<ToolbarEvent, MainToolbarView.Callback>

    @Module
    companion object {

      @Provides
      @JvmStatic
      @CheckResult
      fun provideDropshadow(parent: ViewGroup): DropshadowView {
        return DropshadowView(parent)
      }
    }
  }

}

