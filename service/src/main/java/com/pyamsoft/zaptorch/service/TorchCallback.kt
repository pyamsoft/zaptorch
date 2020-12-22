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

import android.hardware.camera2.CameraManager
import androidx.annotation.CheckResult
import timber.log.Timber

internal class TorchCallback internal constructor(
    private val onOpened: (String) -> Unit,
    private val onClosed: (String) -> Unit,
) : CameraManager.TorchCallback() {

    private var cameraId: String? = null
    private var enabled = false

    @CheckResult
    fun id(): String? {
        return cameraId
    }

    @CheckResult
    fun isEnabled(): Boolean {
        return enabled
    }

    override fun onTorchModeChanged(
        cameraId: String,
        enabled: Boolean
    ) {
        super.onTorchModeChanged(cameraId, enabled)
        Timber.d("Torch changed $cameraId $enabled")
        this.cameraId = cameraId
        this.enabled = enabled

        if (enabled) {
            onOpened(cameraId)
        } else {
            onClosed(cameraId)
        }
    }

    override fun onTorchModeUnavailable(cameraId: String) {
        super.onTorchModeUnavailable(cameraId)
        Timber.e("Torch unavailable $cameraId")
        this.cameraId = null
        this.enabled = false
        onClosed(cameraId)
    }
}