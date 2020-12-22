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

package com.pyamsoft.zaptorch.service

import com.pyamsoft.pydroid.arch.EventBus
import com.pyamsoft.pydroid.arch.EventConsumer
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.core.NotificationHandler
import com.pyamsoft.zaptorch.core.TorchError
import com.pyamsoft.zaptorch.core.VolumeServiceInteractor
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class VolumeServiceInteractorImpl @Inject internal constructor(
    private val notificationHandler: NotificationHandler,
    private val errorBus: EventBus<TorchError>
) : VolumeServiceInteractor {

    private val runningStateBus = EventBus.create<Boolean>(
        emitOnlyWhenActive = true,
        replayCount = 1
    )

    override suspend fun setServiceState(changed: Boolean) {
        Enforcer.assertOffMainThread()
        runningStateBus.send(changed)
    }

    override suspend fun observeServiceState(): EventConsumer<Boolean> =
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()
            return@withContext runningStateBus
        }

    override suspend fun observeCameraState(): EventConsumer<TorchError> =
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()
            return@withContext object : EventConsumer<TorchError> {
                override suspend fun onEvent(emitter: suspend (event: TorchError) -> Unit) {
                    errorBus.onEvent { event ->
                        Timber.w("Stop notification on camera error")
                        notificationHandler.stop()

                        emitter(event)
                    }
                }

            }
        }
}
