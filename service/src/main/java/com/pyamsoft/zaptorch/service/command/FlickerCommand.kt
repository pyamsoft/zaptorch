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
import com.pyamsoft.zaptorch.core.CameraPreferences
import com.pyamsoft.zaptorch.core.TorchState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class FlickerCommand @Inject internal constructor(
    preferences: CameraPreferences
) : BaseCommand<TorchState.Flicker>(preferences) {

    override suspend fun CoroutineScope.onClaimTorch(handler: Command.Handler) {
        handler.onCommandStart(TorchState.Pulse)
        while (isActive) {
            // In case something is planning on killing the torch
            yield()

            handler.forceTorchOn(TorchState.Pulse)
            delay(FLICKER_TIMEOUT)
            handler.forceTorchOff()
            delay(FLICKER_TIMEOUT)
        }
    }

    override fun isKeyCodeHandled(keyCode: Int, isFirstPressComplete: Boolean): Boolean {
        return if (isFirstPressComplete) {
            keyCode == KeyEvent.KEYCODE_VOLUME_UP
        } else {
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
        }
    }

    companion object {
        private const val FLICKER_TIMEOUT = 60L
    }
}