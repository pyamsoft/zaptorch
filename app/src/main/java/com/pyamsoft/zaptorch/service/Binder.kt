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

import java.io.Closeable
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import timber.log.Timber

abstract class Binder<T : Any> {

    val binderScope: CoroutineScope
        get() = CloseableCoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun bind(onEvent: (event: T) -> Unit) {
        onBind(onEvent)
    }

    protected open fun onBind(onEvent: (event: T) -> Unit) {
    }

    fun unbind() {
        onUnbind()

        val scope = binderScope
        if (scope is Closeable) {
            try {
                scope.close()
            } catch (e: Throwable) {
                Timber.e(e, "Failed to close binder scope")
            }
        }
    }

    protected open fun onUnbind() {
    }

    private class CloseableCoroutineScope(context: CoroutineContext) : Closeable, CoroutineScope {
        override val coroutineContext: CoroutineContext = context

        override fun close() {
            coroutineContext.cancel()
        }
    }
}
