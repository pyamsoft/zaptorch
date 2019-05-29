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

import androidx.lifecycle.ViewModelProvider
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.UiViewModelFactory
import com.pyamsoft.zaptorch.main.MainToolbarViewModel
import com.pyamsoft.zaptorch.main.MainViewModel
import com.pyamsoft.zaptorch.settings.SettingsViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
internal class ZapTorchViewModelFactory @Inject internal constructor(
  private val viewModels: MutableMap<Class<out UiViewModel<*, *, *>>, Provider<UiViewModel<*, *, *>>>
) : UiViewModelFactory() {

  override fun <T : UiViewModel<*, *, *>> viewModel(modelClass: Class<T>): UiViewModel<*, *, *> {
    @Suppress("UNCHECKED_CAST")
    return requireNotNull(viewModels[modelClass]).get() as T
  }

}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
private annotation class ViewModelKey(val value: KClass<out UiViewModel<*, *, *>>)

@Module
abstract class ViewModelModule {

  @Binds
  internal abstract fun bindViewModelFactory(factory: ZapTorchViewModelFactory): ViewModelProvider.Factory

  @Binds
  @IntoMap
  @ViewModelKey(MainToolbarViewModel::class)
  internal abstract fun toolbarViewModel(viewModel: MainToolbarViewModel): UiViewModel<*, *, *>

  @Binds
  @IntoMap
  @ViewModelKey(MainViewModel::class)
  internal abstract fun mainViewModel(viewModel: MainViewModel): UiViewModel<*, *, *>

  @Binds
  @IntoMap
  @ViewModelKey(SettingsViewModel::class)
  internal abstract fun settingsViewModel(viewModel: SettingsViewModel): UiViewModel<*, *, *>
}
