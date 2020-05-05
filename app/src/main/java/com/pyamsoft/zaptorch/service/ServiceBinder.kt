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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class ServiceBinder @Inject internal constructor(
    private val finishBus: EventBus<ServiceFinishEvent>,
    private val interactor: VolumeServiceInteractor
) : Binder<ServiceControllerEvent>() {

    override fun onBind(onEvent: (event: ServiceControllerEvent) -> Unit) {
        interactor.setupCamera()
        binderScope.setupCamera(onEvent)
        binderScope.listenFinish(onEvent)
    }

    override fun onUnbind() {
        interactor.releaseCamera()
    }

    private inline fun CoroutineScope.setupCamera(crossinline onEvent: (event: ServiceControllerEvent) -> Unit) =
        launch {
            interactor.observeCameraState()
                .onEvent { withContext(context = Dispatchers.Main) { onEvent(RenderError(it)) } }
        }

    private inline fun CoroutineScope.listenFinish(crossinline onEvent: (event: ServiceControllerEvent) -> Unit) =
        launch(context = Dispatchers.Default) {
            finishBus.onEvent { withContext(context = Dispatchers.Main) { onEvent(Finish) } }
        }

    fun handleKeyEvent(action: Int, keyCode: Int) {
        binderScope.launch {
            interactor.handleKeyPress(action, keyCode) { error ->
                withContext(context = Dispatchers.Main) { interactor.showError(error) }
            }
        }
    }

    fun start() {
        interactor.setServiceState(true)
    }

    fun stop() {
        interactor.setServiceState(false)
    }
}
