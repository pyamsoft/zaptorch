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

package com.pyamsoft.zaptorch.base

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.view.KeyEvent
import com.pyamsoft.pydroid.arch.EventBus
import com.pyamsoft.pydroid.arch.EventConsumer
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.api.CameraInterface
import com.pyamsoft.zaptorch.api.CameraInterface.CameraError
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.base.notification.NotificationHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class VolumeServiceInteractorImpl @Inject internal constructor(
    private val context: Context,
    private val preferences: CameraPreferences,
    private val notificationHandler: NotificationHandler
) : VolumeServiceInteractor {

    private val mutex = Mutex()

    private var pressed: Boolean = false

    private var cameraInterface: CameraInterface? = null

    private val cameraErrorBus = EventBus.create<CameraError>()

    private var running = false

    private val runningStateBus = EventBus.create<Boolean>()

    private var cameraScope: CoroutineScope? = null

    init {
        pressed = false
    }

    override suspend fun setServiceState(changed: Boolean) {
        Enforcer.assertOffMainThread()
        running = changed
        runningStateBus.send(changed)
    }

    override suspend fun observeServiceState(): EventConsumer<Boolean> =
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()
            return@withContext object : EventConsumer<Boolean> {
                override suspend fun onEvent(emitter: suspend (event: Boolean) -> Unit) {
                    emitter(running)
                    runningStateBus.onEvent(emitter)
                }
            }
        }

    override suspend fun observeCameraState(): EventConsumer<CameraError> =
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()
            return@withContext object : EventConsumer<CameraError> {
                override suspend fun onEvent(emitter: suspend (event: CameraError) -> Unit) {
                    cameraErrorBus.onEvent(emitter)
                }
            }
        }

    override suspend fun handleKeyPress(
        action: Int,
        keyCode: Int,
        onError: suspend (error: CameraAccessException) -> Unit
    ) = withContext(context = Dispatchers.Default) {
        if (action != KeyEvent.ACTION_UP) {
            return@withContext
        }
        if (keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return@withContext
        }

        Enforcer.assertOffMainThread()
        if (pressed) {
            mutex.withLock {
                Timber.d("Key has been double pressed")
                pressed = false
                toggleTorch(onError)
            }
        } else {
            mutex.withLock {
                pressed = true
            }

            launch(context = Dispatchers.Default) {
                delay(preferences.getButtonDelayTime())
                mutex.withLock {
                    if (pressed) {
                        Timber.d("Set pressed back to false")
                        pressed = false
                    }
                }
            }
        }
    }

    override fun setupCamera() {
        Enforcer.assertOnMainThread()
        val camera: CameraCommon = MarshmallowCamera(context, preferences).apply {
            setOnStateChangedCallback(object : CameraInterface.OnStateChangedCallback {
                override fun onOpened() {
                    notificationHandler.start()
                }

                override fun onClosed() {
                    notificationHandler.stop()
                }

                override fun onError(error: CameraError) {
                    requireNotNull(cameraScope).launch(context = Dispatchers.Default) {
                        cameraErrorBus.send(error)
                    }
                }
            })
        }

        releaseCamera()
        cameraInterface = camera
        cameraScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    override suspend fun toggleTorch(onError: suspend (error: CameraAccessException) -> Unit) =
        withContext(context = Dispatchers.Default) {
            cameraInterface?.toggleTorch(onError)
            return@withContext
        }

    override fun releaseCamera() {
        cameraInterface?.also {
            it.destroy()
            it.setOnStateChangedCallback(null)
        }
        cameraInterface = null

        cameraScope?.cancel()
        cameraScope = null
    }

    override suspend fun showError(error: CameraAccessException) {
        cameraInterface?.showError(error)
    }
}
