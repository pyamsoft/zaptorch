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

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.Looper
import androidx.annotation.CheckResult
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.arch.EventBus
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.core.TorchError
import com.pyamsoft.zaptorch.core.TorchState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class MarshmallowCamera @Inject internal constructor(
    context: Context,
    private val errorBus: EventBus<TorchError>,
) : CameraCommon() {

    private var activeState: TorchState = TorchState.None

    private val handler = Handler(Looper.getMainLooper())
    private val cameraManager = requireNotNull(context.getSystemService<CameraManager>())
    private val torchCallback = TorchCallback(
        onOpened = { id ->
            Timber.d("Opened camera $id")
            onOpened(activeState)
        },
        onClosed = { id ->
            Timber.d("Closed camera $id")
            onClosed(activeState)
        },
        onUnavailable = { id ->
            Timber.d("Camera unavailable $id")
            onUnavailable(activeState)
        }
    )

    init {
        Timber.d("Register torch callback on main thread")
        cameraManager.registerTorchCallback(torchCallback, handler)
    }

    private fun setTorchState(state: TorchState) {
        Timber.d("Setting torch state tracker to $state")
        this.activeState = state
    }

    override suspend fun toggleTorch(state: TorchState) {
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()
            applyTorchState(state, !torchCallback.isEnabled())?.let { errorBus.send(it) }
        }
    }

    override suspend fun forceTorchOff() {
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()
            applyTorchState(TorchState.None, false)?.let { errorBus.send(it) }
        }
    }

    @CheckResult
    private fun applyTorchState(state: TorchState, enabled: Boolean): TorchError? {
        val cameraId = torchCallback.id()
        if (cameraId == null) {
            Timber.e("Torch unavailable $cameraId")
            setTorchState(TorchState.None)
            return TorchError(CAMERA_UNAVAILABLE)
        }

        return try {
            Timber.d("Set torch: $cameraId $enabled")
            cameraManager.setTorchMode(cameraId, enabled)
            setTorchState(if (enabled) state else TorchState.None)
            null
        } catch (e: CameraAccessException) {
            Timber.e(e, "Error during setTorchState $cameraId $enabled")
            setTorchState(TorchState.None)
            TorchError(e)
        }
    }

    override fun release() {
        if (torchCallback.isEnabled()) {
            if (applyTorchState(TorchState.None, false) == null) {
                Timber.d("Torch turned off")
            }
        }

        Timber.d("Unregister torch callback")
        cameraManager.unregisterTorchCallback(torchCallback)

        Timber.d("Clear handler")
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private val CAMERA_UNAVAILABLE = CameraAccessException(
            CameraAccessException.CAMERA_ERROR,
            "Torch unavailable"
        )
    }
}
