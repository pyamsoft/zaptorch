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
import com.pyamsoft.zaptorch.core.*
import com.pyamsoft.zaptorch.service.command.Command
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
internal class CameraInteractorImpl @Inject internal constructor(
    private val cameraProvider: Provider<CameraInterface>,
    private val notificationHandler: NotificationHandler,
    private val toggleCommand: Command<TorchState.Toggle>,
    private val pulseCommand: Command<TorchState.Pulse>,
) : CameraInteractor, TorchOffInteractor, Command.Handler {

    private var cameraInterface: CameraInterface? = null

    override suspend fun handleKeyPress(action: Int, keyCode: Int) {
        val self = this
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()

            if (action != KeyEvent.ACTION_UP) {
                return@withContext
            }

            if (toggleCommand.handle(keyCode, self)) {
                pulseCommand.reset()
                Timber.d("Torch handled by ${TorchState.Toggle}")
            }

            if (pulseCommand.handle(keyCode, self)) {
                toggleCommand.reset()
                Timber.d("Torch handled by ${TorchState.Pulse}")
            }
        }
    }

    override fun onCommandStart(state: TorchState) {
        Timber.d("Start command: $state")
        notificationHandler.start()
    }

    override fun onCommandStop(state: TorchState) {
        Timber.d("Stop command: $state")
        notificationHandler.stop()
        clearCommands()
    }

    override fun initialize() {
        Enforcer.assertOnMainThread()

        destroy()
        cameraInterface = cameraProvider.get().apply {
            setOnUnavailableCallback { state ->
                Timber.w("Torch unavailable: $state")
                clearCommands()
                notificationHandler.stop()
            }
        }
    }

    override suspend fun toggleTorch(state: TorchState) {
        withContext(context = Dispatchers.Default) {
            cameraInterface?.toggleTorch(state)
        }
    }

    override suspend fun forceTorchOff() {
        withContext(context = Dispatchers.Default) {
            clearCommands()
            cameraInterface?.forceTorchOff()
        }
    }

    private fun clearCommands() {
        toggleCommand.destroy()
        pulseCommand.destroy()
    }

    override fun destroy() {
        clearCommands()
        notificationHandler.stop()

        cameraInterface?.destroy()
        cameraInterface = null
    }

}
