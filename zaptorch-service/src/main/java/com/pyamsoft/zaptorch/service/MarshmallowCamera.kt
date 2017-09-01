/*
 * Copyright 2017 Peter Kenji Yamanaka
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

import android.annotation.TargetApi
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.os.Build
import android.support.annotation.CheckResult
import io.reactivex.Scheduler
import timber.log.Timber

@TargetApi(Build.VERSION_CODES.M) internal class MarshmallowCamera internal constructor(
    context: Context, interactor: VolumeServiceInteractor, computationScheduler: Scheduler,
    ioScheduler: Scheduler, mainThreadScheduler: Scheduler) : CameraCommon(context, interactor,
    computationScheduler, ioScheduler, mainThreadScheduler) {

  private val cameraManager: CameraManager = context.applicationContext.getSystemService(
      Context.CAMERA_SERVICE) as CameraManager
  private val torchCallback = TorchCallback(this)

  init {
    setupCamera()
  }

  override fun toggleTorch() {
    setTorch(torchCallback.isEnabled.not())
  }

  private fun setTorch(enable: Boolean) {
    val cameraId = torchCallback.cameraId
    if (cameraId != null) {
      try {
        Timber.d("Set torch: %s", enable)
        cameraManager.setTorchMode(cameraId, enable)
      } catch (e: CameraAccessException) {
        Timber.e(e, "toggleTorch ERROR")
        startErrorExplanationActivity()
      }

    } else {
      Timber.e("Torch unavailable")
      startErrorExplanationActivity()
    }
  }

  override fun release() {
    if (torchCallback.isEnabled) {
      setTorch(false)
    }

    Timber.d("Unregister torch callback")
    cameraManager.unregisterTorchCallback(torchCallback)
  }

  override fun onUnbind() {
    release()
  }

  private fun setupCamera() {
    Timber.d("Register torch callback")
    cameraManager.registerTorchCallback(torchCallback, null)
  }

  internal class TorchCallback internal constructor(
      private val cameraCommon: CameraCommon) : CameraManager.TorchCallback() {

    var cameraId: String? = null
      @CheckResult get
    var isEnabled: Boolean = false
      @CheckResult get

    override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
      super.onTorchModeChanged(cameraId, enabled)
      Timber.d("Torch changed: %s", enabled)
      this.cameraId = cameraId
      this.isEnabled = enabled

      if (enabled) {
        cameraCommon.notifyCallbackOnOpened()
      } else {
        cameraCommon.notifyCallbackOnClosed()
      }
    }

    override fun onTorchModeUnavailable(cameraId: String) {
      super.onTorchModeUnavailable(cameraId)
      Timber.e("Torch unavailable")
      this.cameraId = null
      this.isEnabled = false

      cameraCommon.notifyCallbackOnClosed()
    }
  }
}
