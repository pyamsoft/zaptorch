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
import com.pyamsoft.zaptorch.core.TorchOff
import com.pyamsoft.zaptorch.core.TorchState
import com.pyamsoft.zaptorch.core.TorchToggle

internal interface Command<out S : TorchState> {

    fun destroy()

    suspend fun reset()

    @CheckResult
    suspend fun handle(keyCode: Int, handler: Handler): Boolean

    interface Handler : TorchOff, TorchToggle {

        fun onCommandStart(state: TorchState)

        fun onCommandStop(state: TorchState)
    }
}