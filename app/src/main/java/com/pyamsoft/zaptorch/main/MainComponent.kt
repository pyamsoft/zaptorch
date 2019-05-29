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
import androidx.lifecycle.ViewModelProvider
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.zaptorch.ViewModelKey
import com.pyamsoft.zaptorch.ZapTorchViewModelFactory
import com.pyamsoft.zaptorch.main.MainComponent.MainModule
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.multibindings.IntoMap

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
    internal abstract fun bindViewModelFactory(factory: ZapTorchViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainToolbarViewModel::class)
    internal abstract fun toolbarViewModel(viewModel: MainToolbarViewModel): UiViewModel<*, *, *>

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

