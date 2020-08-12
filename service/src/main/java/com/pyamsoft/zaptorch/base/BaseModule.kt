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

package com.pyamsoft.zaptorch.base

import androidx.annotation.CheckResult
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.ClearPreferences
import com.pyamsoft.zaptorch.api.MainInteractor
import com.pyamsoft.zaptorch.api.SettingsInteractor
import com.pyamsoft.zaptorch.api.UIPreferences
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import dagger.Binds
import dagger.Module

@Module
abstract class BaseModule {

    @Binds
    @CheckResult
    internal abstract fun bindServiceInteractor(impl: VolumeServiceInteractorImpl): VolumeServiceInteractor

    @Binds
    @CheckResult
    internal abstract fun bindSettingsInteractor(impl: SettingsInteractorImpl): SettingsInteractor

    @Binds
    @CheckResult
    internal abstract fun bindMainInteractor(impl: MainInteractorImpl): MainInteractor

    @Binds
    @CheckResult
    internal abstract fun bindUiPreferences(impl: ZapTorchPreferencesImpl): UIPreferences

    @Binds
    @CheckResult
    internal abstract fun bindCameraPreferences(impl: ZapTorchPreferencesImpl): CameraPreferences

    @Binds
    @CheckResult
    internal abstract fun bindClearPreferences(impl: ZapTorchPreferencesImpl): ClearPreferences
}
