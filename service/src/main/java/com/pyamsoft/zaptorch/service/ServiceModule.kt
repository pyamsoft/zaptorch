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

package com.pyamsoft.zaptorch.service

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.zaptorch.core.*
import com.pyamsoft.zaptorch.service.command.Command
import com.pyamsoft.zaptorch.service.command.FlickerCommand
import com.pyamsoft.zaptorch.service.command.PulseCommand
import com.pyamsoft.zaptorch.service.command.ToggleCommand
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
abstract class ServiceModule {

    @Binds
    @CheckResult
    internal abstract fun bindToggleCommand(impl: ToggleCommand): Command<TorchState.Toggle>

    @Binds
    @CheckResult
    internal abstract fun bindPulseCommand(impl: PulseCommand): Command<TorchState.Pulse>

    @Binds
    @CheckResult
    internal abstract fun bindFlickerCommand(impl: FlickerCommand): Command<TorchState.Flicker>

    @Binds
    @CheckResult
    internal abstract fun bindTorchOffInteractor(impl: CameraInteractorImpl): TorchOffInteractor

    @Binds
    @CheckResult
    internal abstract fun bindCameraInteractor(impl: CameraInteractorImpl): CameraInteractor

    @Binds
    @CheckResult
    internal abstract fun bindServiceInteractor(impl: VolumeServiceInteractorImpl): VolumeServiceInteractor

    @Binds
    @CheckResult
    internal abstract fun bindSettingsInteractor(impl: SettingsInteractorImpl): SettingsInteractor

    @Binds
    @CheckResult
    internal abstract fun bindCameraPreferences(impl: ZapTorchPreferencesImpl): CameraPreferences

    @Binds
    @CheckResult
    internal abstract fun bindClearPreferences(impl: ZapTorchPreferencesImpl): ClearPreferences

    @Binds
    @CheckResult
    internal abstract fun bindCamera(impl: MarshmallowCamera): CameraInterface

    @Module
    companion object {

        @JvmStatic
        @Provides
        @CheckResult
        @Singleton
        fun provideCameraErrorBus(): EventBus<TorchError> {
            return EventBus.create(emitOnlyWhenActive = false)
        }

    }
}
