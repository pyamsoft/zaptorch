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
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PulseTorchCommand @Inject internal constructor(
    private val preferences: CameraPreferences
) : Command<TorchState.Pulse> {

    private val commandScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val mutex = Mutex()
    private var commandReady = false

    private var timerJob: Job? = null
    private var job: Job? = null

    @PublishedApi
    internal suspend inline fun handleTorchOnDoublePressed(
        scope: CoroutineScope,
        handler: Command.Handler
    ) {
        mutex.withLock {
            commandReady = false

            timerJob?.cancelAndJoin()
            timerJob = null
        }

        mutex.withLock {
            job = job.let { j ->
                handler.forceTorchOff()
                if (j == null) {
                    scope.launch {
                        handler.onCommandStart(TorchState.Pulse)
                        while (isActive) {
                            handler.forceTorchOn(TorchState.Pulse)
                            delay(PULSE_TIMEOUT)
                            handler.forceTorchOff()
                            delay(PULSE_TIMEOUT)
                        }
                    }
                } else {
                    j.cancelAndJoin()
                    handler.onCommandStop(TorchState.Pulse)
                    null
                }
            }
        }
    }

    @PublishedApi
    internal suspend fun handleTorchOnFirstPress(scope: CoroutineScope) {
        mutex.withLock {
            commandReady = true
        }

        // Launch a new coroutine here so that this happens in parallel with other operations
        timerJob?.cancelAndJoin()
        timerJob = scope.launch {
            delay(preferences.getButtonDelayTime())
            if (commandReady) {
                mutex.withLock {
                    if (commandReady) {
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
        handler: Command.Handler
    ): Boolean {
        return if (keyCode != KeyEvent.KEYCODE_VOLUME_UP) false else {
            commandReady.also { ready ->
                commandScope.launch {
                    if (ready) {
                        handleTorchOnDoublePressed(this, handler)
                    } else {
                        handleTorchOnFirstPress(this)
                    }
                }
            }
        }
    }

    override fun reset() {
        commandReady = false

        timerJob?.cancel()
        timerJob = null

        job?.cancel()
        job = null
    }

    override fun destroy() {
        reset()
        commandScope.cancel()
    }

    override suspend fun handle(keyCode: Int, handler: Command.Handler): Boolean {
        return handleTorchOnCommand(keyCode, handler)
    }

    companion object {
        private const val PULSE_TIMEOUT = 600L
    }
}