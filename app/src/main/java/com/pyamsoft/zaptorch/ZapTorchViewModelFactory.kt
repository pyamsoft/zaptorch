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

import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.arch.UiViewModelFactory
import dagger.MapKey
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider
import kotlin.reflect.KClass

internal class ZapTorchViewModelFactory @Inject internal constructor(
    @Named("debug") debug: Boolean,
    private val viewModels: MutableMap<Class<out UiViewModel<*, *, *>>, Provider<UiViewModel<*, *, *>>>
) : UiViewModelFactory(debug) {

    override fun <T : UiViewModel<*, *, *>> viewModel(modelClass: KClass<T>): UiViewModel<*, *, *> {
        @Suppress("UNCHECKED_CAST")
        return viewModels[modelClass.java]?.get() as? T ?: fail()
    }
}

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out UiViewModel<*, *, *>>)
