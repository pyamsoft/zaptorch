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
import com.pyamsoft.zaptorch.core.TorchPreferences
import com.pyamsoft.zaptorch.core.TorchState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.yield
import timber.log.Timber

@Singleton
internal class ToggleCommand @Inject internal constructor(preferences: TorchPreferences) :
    BaseCommand(preferences) {

  override suspend fun CoroutineScope.onClaimTorch(handler: Command.Handler): Throwable? {
    handler.onCommandStart(TorchState.Toggle)

    // In case something is planning on killing the torch
    yield()

    return handler.forceTorchOn(TorchState.Toggle)?.also { error ->
      Timber.e(error, "Force torch ON failed. Stop Toggle")
    }
  }

  override suspend fun isKeyCodeHandled(keyCode: Int, isFirstPressComplete: Boolean): Boolean {
    return keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
  }

  override suspend fun isCommandEnabled(preferences: TorchPreferences): Boolean {
    return preferences.isTorchEnabled()
  }
}
