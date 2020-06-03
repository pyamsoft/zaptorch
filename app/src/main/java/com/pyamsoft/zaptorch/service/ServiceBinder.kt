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

import com.pyamsoft.pydroid.arch.EventBus
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.service.ServiceControllerEvent.Finish
import com.pyamsoft.zaptorch.service.ServiceControllerEvent.RenderError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class ServiceBinder @Inject internal constructor(
    private val finishBus: EventBus<ServiceFinishEvent>,
    private val interactor: VolumeServiceInteractor
) : Binder<ServiceControllerEvent>() {

    override fun onBind(onEvent: (event: ServiceControllerEvent) -> Unit) {
        binderScope.launch(context = Dispatchers.Main) {
            interactor.setupCamera()
        }

        setupCamera(onEvent)
        listenFinish(onEvent)
    }

    override fun onUnbind() {
        interactor.releaseCamera()
    }

    private inline fun setupCamera(crossinline onEvent: (event: ServiceControllerEvent) -> Unit) =
        binderScope.launch(context = Dispatchers.Default) {
            interactor.observeCameraState()
                .onEvent { withContext(context = Dispatchers.Main) { onEvent(RenderError(it)) } }
        }

    private inline fun listenFinish(crossinline onEvent: (event: ServiceControllerEvent) -> Unit) =
        binderScope.launch(context = Dispatchers.Default) {
            finishBus.onEvent { withContext(context = Dispatchers.Main) { onEvent(Finish) } }
        }

    fun handleKeyEvent(action: Int, keyCode: Int) {
        binderScope.launch(context = Dispatchers.Default) {
            interactor.handleKeyPress(action, keyCode) { error ->
                withContext(context = Dispatchers.Main) { interactor.showError(error) }
            }
        }
    }

    fun start() {
        binderScope.launch(context = Dispatchers.Default) {
            interactor.setServiceState(true)
        }
    }

    fun stop() {
        binderScope.launch(context = Dispatchers.Default) {
            interactor.setServiceState(false)
        }
    }
}
