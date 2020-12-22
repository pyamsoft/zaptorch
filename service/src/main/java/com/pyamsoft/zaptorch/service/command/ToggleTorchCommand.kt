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

package com.pyamsoft.zaptorch.service.command

import android.view.KeyEvent
import androidx.annotation.CheckResult
import com.pyamsoft.zaptorch.core.CameraPreferences
import com.pyamsoft.zaptorch.core.TorchState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ToggleTorchCommand @Inject internal constructor(
    private val preferences: CameraPreferences
) : Command<TorchState.Toggle> {

    private val mutex = Mutex()
    private var commandReady = false

    @PublishedApi
    internal suspend inline fun handleTorchOnDoublePressed(onAction: () -> Unit) {
        mutex.withLock {
            commandReady = false
        }


        Timber.d("Key has been double pressed, toggle torch")
        onAction()
    }

    @PublishedApi
    internal suspend fun handleTorchOnFirstPress() = coroutineScope {
        mutex.withLock {
            commandReady = true
        }

        // Launch a new coroutine here so that this happens in parallel with other operations
        launch(context = Dispatchers.Default) {
            delay(preferences.getButtonDelayTime())
            if (commandReady) {
                mutex.withLock {
                    if (commandReady) {
                        Timber.d("Set pressed back to false")
                        commandReady = false
                    }
                }
            }
        }
    }

    @CheckResult
    @PublishedApi
    internal suspend inline fun handleTorchOnCommand(
        keyCode: Int,
        onAction: () -> Unit
    ): Boolean {
        return if (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) false else {
            commandReady.also { ready ->
                if (ready) {
                    handleTorchOnDoublePressed(onAction)
                } else {
                    handleTorchOnFirstPress()
                }
            }
        }
    }

    override suspend fun handle(
        keyCode: Int,
        onAction: suspend (TorchState.Toggle) -> Unit
    ): Boolean {
        return handleTorchOnCommand(keyCode) { onAction(TorchState.Toggle) }
    }
}