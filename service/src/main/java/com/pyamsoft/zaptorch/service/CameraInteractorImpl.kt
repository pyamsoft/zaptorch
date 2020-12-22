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

import android.view.KeyEvent
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.core.CameraInteractor
import com.pyamsoft.zaptorch.core.CameraInterface
import com.pyamsoft.zaptorch.core.TorchOffInteractor
import com.pyamsoft.zaptorch.service.command.ToggleTorchCommand
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
internal class CameraInteractorImpl @Inject internal constructor(
    private val cameraProvider: Provider<CameraInterface>,
    private val toggleCommand: ToggleTorchCommand,
) : CameraInteractor, TorchOffInteractor {

    private var cameraInterface: CameraInterface? = null

    override suspend fun handleKeyPress(action: Int, keyCode: Int) =
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()

            if (action != KeyEvent.ACTION_UP) {
                return@withContext
            }

            toggleCommand.handleCommand(keyCode) { toggleTorch() }
        }

    override fun setupCamera(onOpened: (String) -> Unit, onClosed: (String) -> Unit) {
        Enforcer.assertOnMainThread()

        releaseCamera()
        cameraInterface = cameraProvider.get().apply {
            setOnOpenedCallback(onOpened)
            setOnClosedCallback(onClosed)
        }
    }

    override suspend fun toggleTorch() {
        withContext(context = Dispatchers.Default) {
            cameraInterface?.toggleTorchState()
        }
    }

    override suspend fun torchOff() {
        withContext(context = Dispatchers.Default) {
            cameraInterface?.setTorchState(false)
        }
    }

    override fun releaseCamera() {
        cameraInterface?.destroy()
        cameraInterface = null
    }

}
