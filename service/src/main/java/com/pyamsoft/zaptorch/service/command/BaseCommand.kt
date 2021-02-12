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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.core.TorchPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

internal abstract class BaseCommand protected constructor(
    private val preferences: TorchPreferences
) : Command {

    private var commandScope: CoroutineScope? = null

    private val mutex = Mutex()
    private var commandReady = false

    private var timerJob: Job? = null
    private var job: Job? = null

    @CheckResult
    private suspend fun ensureScope(): CoroutineScope = mutex.withLock {
        commandScope ?: newScope().also { commandScope = it }
    }

    @CheckResult
    private fun newScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    @PublishedApi
    internal suspend inline fun handleTorchOnDoublePressed(
        handler: Command.Handler
    ) = coroutineScope {
        mutex.withLock {
            commandReady = false

            cancelTimer()
        }

        mutex.withLock {
            job = job.let { j ->
                val prepError = handler.forceTorchOff()
                if (j == null && prepError == null) {
                    launch {
                        val torchError = onClaimTorch(handler)
                        if (torchError != null) {
                            Timber.e(torchError, "Error running torch command")
                            cancel()
                            handler.onCommandStop()
                        }
                    }
                } else {
                    prepError?.also { Timber.e(it, "Error preparing torch for command") }
                    j?.cancelAndJoin()
                    handler.onCommandStop()
                    null
                }
            }
        }
    }

    @PublishedApi
    internal suspend fun handleTorchOnFirstPress() = coroutineScope {
        mutex.withLock {
            commandReady = true
        }

        // Launch a new coroutine here so that this happens in parallel with other operations
        cancelTimer()
        timerJob = launch {
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
        isReady: Boolean,
        handler: Command.Handler
    ): Boolean {
        return isReady.also { ready ->
            ensureScope().launch {
                if (ready) {
                    handleTorchOnDoublePressed(handler)
                } else {
                    handleTorchOnFirstPress()
                }
            }
        }
    }

    private fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    final override fun id(): String {
        return this::class.java.name
    }

    final override fun reset() {
        commandReady = false

        cancelTimer()

        job?.cancel()
        job = null
    }

    final override fun destroy() {
        reset()

        commandScope?.cancel()
        commandScope = null
    }

    final override suspend fun handle(keyCode: Int, handler: Command.Handler): Boolean {
        Enforcer.assertOffMainThread()

        if (!isCommandEnabled(preferences)) {
            reset()
            return false
        }

        val isReady = mutex.withLock { commandReady }
        return if (!isKeyCodeHandled(keyCode, isReady)) false else {
            handleTorchOnCommand(isReady, handler)
        }
    }

    @CheckResult
    protected abstract suspend fun CoroutineScope.onClaimTorch(handler: Command.Handler): Throwable?

    @CheckResult
    protected abstract suspend fun isCommandEnabled(preferences: TorchPreferences): Boolean

    @CheckResult
    protected abstract suspend fun isKeyCodeHandled(
        keyCode: Int,
        isFirstPressComplete: Boolean
    ): Boolean
}