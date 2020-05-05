/*
 * Copyright 2019 Peter Kenji Yamanaka
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
 *
 */

package com.pyamsoft.zaptorch.service

import com.pyamsoft.pydroid.arch.UnitControllerEvent
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

internal class TorchBinder @Inject internal constructor(
    private val interactor: VolumeServiceInteractor
) : Binder<UnitControllerEvent>() {

    fun toggle() {
        binderScope.launch {
            interactor.toggleTorch { error ->
                if (error == null) {
                    Timber.e("Torch unavailable, cannot toggle")
                } else {
                    Timber.e(error, "Error when toggling torch")
                }
            }
        }
    }
}
