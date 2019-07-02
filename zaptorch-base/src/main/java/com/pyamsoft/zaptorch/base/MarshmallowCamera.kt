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

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.api.CameraInterface
import com.pyamsoft.zaptorch.api.CameraPreferences
import kotlinx.coroutines.coroutineScope
import timber.log.Timber

internal class MarshmallowCamera internal constructor(
  context: Context,
  enforcer: Enforcer,
  preferences: CameraPreferences
) : CameraCommon(enforcer, preferences) {

  private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
  private val torchCallback = TorchCallback(this)

  init {
    setupCamera()
  }

  override suspend fun toggleTorch(onError: suspend (error: CameraAccessException?) -> Unit) {
    coroutineScope {
      enforcer.assertNotOnMainThread()

      val toggle = !torchCallback.isEnabled
      Timber.d("Toggle torch: $toggle")
      setTorch(toggle, onError)
    }
  }

  private suspend inline fun setTorch(
    enable: Boolean,
    crossinline onError: suspend (error: CameraAccessException?) -> Unit
  ) {
    coroutineScope {
      enforcer.assertNotOnMainThread()

      val error = setTorchState(enable)
      if (error != null) {
        if (shouldShowError()) {
          onError(error.exception)
        }
      }
    }
  }

  @CheckResult
  private fun setTorchState(enabled: Boolean): TorchError? {
    val cameraId = torchCallback.cameraId
    if (cameraId != null) {
      try {
        Timber.d("Set torch: $enabled")
        cameraManager.setTorchMode(cameraId, enabled)
        return null
      } catch (e: CameraAccessException) {
        Timber.e(e, "Error during torchOff")
        return TorchError(e)
      }
    } else {
      Timber.e("Torch unavailable")
      return TorchError(null)
    }
  }

  private data class TorchError(val exception: CameraAccessException?)

  override fun release() {
    if (torchCallback.isEnabled) {
      if (setTorchState(false) == null) {
        Timber.d("Torch turned off")
      }
    }

    Timber.d("Unregister torch callback")
    cameraManager.unregisterTorchCallback(torchCallback)
  }

  private fun setupCamera() {
    Timber.d("Register torch callback")
    cameraManager.registerTorchCallback(torchCallback, null)
  }

  internal class TorchCallback internal constructor(
    private val callback: CameraInterface.OnStateChangedCallback
  ) : CameraManager.TorchCallback() {

    internal var cameraId: String? = null
    internal var isEnabled = false

    override fun onTorchModeChanged(
      cameraId: String,
      enabled: Boolean
    ) {
      super.onTorchModeChanged(cameraId, enabled)
      Timber.d("Torch changed: %s", enabled)
      this.cameraId = cameraId
      this.isEnabled = enabled

      if (enabled) {
        callback.onOpened()
      } else {
        callback.onClosed()
      }
    }

    override fun onTorchModeUnavailable(cameraId: String) {
      super.onTorchModeUnavailable(cameraId)
      Timber.e("Torch unavailable")
      this.cameraId = null
      this.isEnabled = false
      callback.onClosed()
    }
  }
}
