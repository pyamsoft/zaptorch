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

package com.pyamsoft.zaptorch.base

import android.content.Intent
import android.hardware.camera2.CameraAccessException
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.api.CameraInterface
import com.pyamsoft.zaptorch.api.CameraInterface.CameraError
import com.pyamsoft.zaptorch.api.CameraInterface.OnStateChangedCallback
import com.pyamsoft.zaptorch.api.CameraPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal abstract class CameraCommon protected constructor(
    private val preferences: CameraPreferences
) : CameraInterface, OnStateChangedCallback {

    private var stateChangedCallback: OnStateChangedCallback? = null
    private val errorExplain = Intent().apply {
        putExtra(CameraInterface.DIALOG_WHICH, CameraInterface.TYPE_ERROR)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    final override fun setOnStateChangedCallback(callback: OnStateChangedCallback?) {
        stateChangedCallback = callback
    }

    final override fun onOpened() {
        stateChangedCallback?.also {
            Timber.d("Notify callback: opened")
            it.onOpened()
        }
    }

    final override fun onClosed() {
        stateChangedCallback?.also {
            Timber.d("Notify callback: closed")
            it.onClosed()
        }
    }

    final override fun onError(error: CameraError) {
        stateChangedCallback?.also {
            Timber.w("Notify callback: error")
            it.onError(error)
        }
    }

    final override fun destroy() {
        release()
    }

    @CheckResult
    protected suspend fun shouldShowError(): Boolean = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.shouldShowErrorDialog()
    }

    override suspend fun showError(exception: CameraAccessException?) =
        withContext(context = Dispatchers.Main) {
            Enforcer.assertOnMainThread()
            val error = CameraError(exception, errorExplain)
            onError(error)
        }

    protected abstract fun release()
}
