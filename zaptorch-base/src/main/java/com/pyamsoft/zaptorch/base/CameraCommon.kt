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
import timber.log.Timber

internal abstract class CameraCommon protected constructor(
  protected val enforcer: Enforcer,
  private val preferences: CameraPreferences
) : CameraInterface, OnStateChangedCallback {

  private val errorExplain = Intent()
  private var callback: OnStateChangedCallback? = null

  init {
    errorExplain.apply {
      putExtra(CameraInterface.DIALOG_WHICH, CameraInterface.TYPE_ERROR)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
  }

  override fun setOnStateChangedCallback(callback: OnStateChangedCallback?) {
    this.callback = callback
  }

  override fun onOpened() {
    callback?.also {
      Timber.d("Notify callback: opened")
      it.onOpened()
    }
  }

  override fun onClosed() {
    callback?.also {
      Timber.d("Notify callback: closed")
      it.onClosed()
    }
  }

  private fun notifyCallbackOnError(
    exception: CameraAccessException?,
    intent: Intent
  ) {
    val error = CameraError(exception, intent)
    onError(error)
  }

  override fun onError(error: CameraError) {
    callback?.also {
      Timber.w("Notify callback: error")
      it.onError(error)
    }
  }

  override fun destroy() {
    release()
  }

  @CheckResult
  protected suspend fun shouldShowError(): Boolean {
    enforcer.assertNotOnMainThread()
    return preferences.shouldShowErrorDialog()
  }

  override fun showError(exception: CameraAccessException?) {
    notifyCallbackOnError(exception, errorExplain)
  }

  protected abstract fun release()
}
