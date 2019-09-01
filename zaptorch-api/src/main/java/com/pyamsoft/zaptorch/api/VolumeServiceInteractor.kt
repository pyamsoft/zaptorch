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

import android.hardware.camera2.CameraAccessException
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.EventConsumer
import com.pyamsoft.zaptorch.api.CameraInterface.CameraError

interface VolumeServiceInteractor {

    suspend fun handleKeyPress(
        action: Int,
        keyCode: Int,
        onError: suspend (error: CameraAccessException?) -> Unit
    )

    fun setupCamera()

    suspend fun toggleTorch(onError: suspend (error: CameraAccessException?) -> Unit)

    fun releaseCamera()

    @CheckResult
    fun observeServiceState(): EventConsumer<Boolean>

    @CheckResult
    fun observeCameraState(): EventConsumer<CameraError>

    fun setServiceState(changed: Boolean)

    fun showError(error: CameraAccessException?)
}
