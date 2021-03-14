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

package com.pyamsoft.zaptorch.service.monitor

import com.pyamsoft.zaptorch.core.CameraInteractor
import com.pyamsoft.zaptorch.core.VolumeServiceInteractor
import com.pyamsoft.zaptorch.service.Binder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

internal class ServiceBinder @Inject internal constructor(
    private val cameraInteractor: CameraInteractor,
    private val serviceInteractor: VolumeServiceInteractor,
) : Binder<ServiceEvent>() {

    override fun CoroutineScope.onBind(onEvent: (event: ServiceEvent) -> Unit) {
        launch(context = Dispatchers.Main) {
            cameraInteractor.initialize()
        }

        watchCameraErrors(onEvent)
    }

    override fun onUnbind() {
        cameraInteractor.destroy()
    }

    private inline fun CoroutineScope.watchCameraErrors(crossinline onEvent: (ServiceEvent) -> Unit) =
        launch(context = Dispatchers.Default) {
            serviceInteractor.observeCameraState { error ->
                withContext(context = Dispatchers.Main) {
                    Timber.e(error.exception, "Camera error received")
                    onEvent(ServiceEvent.RenderError(error))
                }
            }
        }

    internal fun handleKeyEvent(scope: CoroutineScope, action: Int, keyCode: Int) {
        scope.launch(context = Dispatchers.Default) {
            cameraInteractor.handleKeyPress(action, keyCode)
        }
    }

    internal fun start(scope: CoroutineScope) {
        scope.launch(context = Dispatchers.Default) {
            serviceInteractor.setServiceState(true)
        }
    }

    internal fun handleStop(scope: CoroutineScope) {
        scope.launch(context = Dispatchers.Default) {
            serviceInteractor.setServiceState(false)
        }
    }
}
