/*
 *     Copyright (C) 2017 Peter Kenji Yamanaka
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
        super.onUnbind()
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
