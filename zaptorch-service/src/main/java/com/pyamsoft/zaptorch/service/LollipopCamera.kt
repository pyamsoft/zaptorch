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

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.os.Build
import android.support.annotation.CheckResult
import android.support.v4.content.ContextCompat
import android.util.Size
import android.view.Surface
import com.pyamsoft.zaptorch.api.CameraInterface
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import io.reactivex.Scheduler
import timber.log.Timber
import java.util.ArrayList

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
internal class LollipopCamera internal constructor(
    context: Context, interactor: VolumeServiceInteractor, computationScheduler: Scheduler,
    mainScheduler: Scheduler
) :
    CameraCommon(context, interactor, computationScheduler, mainScheduler) {

    private val cameraManager: CameraManager = appContext.getSystemService(
        Context.CAMERA_SERVICE
    ) as CameraManager
    private val flashCameraId = setupCamera()
    private val cameraCallback = CameraCallback(this, cameraManager)

    @CheckResult
    private fun setupCamera(): String? {
        try {
            val cameraList = cameraManager.cameraIdList
            for (camera in cameraList) {
                val hasFlash = cameraManager.getCameraCharacteristics(camera)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
                if (hasFlash != null && hasFlash) {
                    return camera
                }
            }
        } catch (e: CameraAccessException) {
            Timber.e(e, "setupCamera ERROR")
        }

        return null
    }

    override fun release() {
        cameraCallback.close()
    }

    override fun toggleTorch() {
        if (flashCameraId == null) {
            Timber.e("No setupCamera with Flash")
            startErrorExplanationActivity()
        } else {
            Timber.d("Open setupCamera")
            val result = cameraCallback.accessCamera(appContext, flashCameraId)
            when (result) {
                CameraInterface.TYPE_ERROR -> startErrorExplanationActivity()
                CameraInterface.TYPE_PERMISSION -> startPermissionExplanationActivity()
                else -> Timber.d("Do nothing")
            }
        }
    }

    internal class CameraCallback internal constructor(
        private val cameraInterface: CameraCommon,
        private val manager: CameraManager
    ) : CameraDevice.StateCallback() {
        private val list: MutableList<Surface>

        private var cameraDevice: CameraDevice? = null
        private var session: SessionCallback? = null

        private var size: Size? = null
        private var opened: Boolean = false

        init {
            opened = false
            list = ArrayList(1)
        }

        fun close() {
            // Surface texture is released by CameraManager so we don't have to
            if (opened) {
                val obj = session
                if (obj != null) {
                    Timber.d("close SessionCallback")
                    obj.close()
                    session = null
                }

                val camera = cameraDevice
                if (camera != null) {
                    Timber.d("close setupCamera device")
                    camera.close()
                    cameraDevice = null
                }

                Timber.d("Release surfaces")
                for (surface in list) {
                    surface.release()
                }
                list.clear()
                cameraInterface.notifyCallbackOnClosed()
            }
            opened = false
        }

        @CheckResult
        fun accessCamera(context: Context, id: String): Int {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED) {
                return try {
                    Timber.d("Has setupCamera permission, attempt to access")
                    if (opened) {
                        Timber.d("Close opened setupCamera")
                        close()
                    } else {
                        Timber.d("Open closed setupCamera")
                        manager.openCamera(id, this, null)
                    }

                    // Return
                    CameraInterface.TYPE_NONE
                } catch (e: CameraAccessException) {
                    Timber.e(e, "toggleTorch ERROR")

                    // Return
                    CameraInterface.TYPE_ERROR
                }
            } else {
                Timber.e("Missing setupCamera permission")
                return CameraInterface.TYPE_PERMISSION
            }
        }

        @SuppressLint("Recycle")
        override fun onOpened(camera: CameraDevice) {
            Timber.d("onOpened")
            opened = true
            this.cameraDevice = camera

            try {
                Timber.d("create capture builder")
                val captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL)
                captureBuilder.set(
                    CaptureRequest.CONTROL_AE_MODE,
                    CameraMetadata.CONTROL_AF_MODE_AUTO
                )
                captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH)

                if (size == null) {
                    Timber.d("get smallest size for texture")
                    size = getSmallestSize(manager, camera.id)
                } else {
                    Timber.d("using cached smallest size")
                }

                // The setupCamera session recycles the surface texture, so we should not have to
                val surfaceTexture = SurfaceTexture(1)
                val smallestSize = size ?: throw RuntimeException(
                    "Size is NULL, This should not happen!"
                )
                surfaceTexture.setDefaultBufferSize(smallestSize.width, smallestSize.height)

                Timber.d("add surface to texture")
                val surface = Surface(surfaceTexture)
                if (list.isEmpty()) {
                    list.add(0, surface)
                } else {
                    list[0] = surface
                }

                Timber.d("add capture target")
                captureBuilder.addTarget(surface)

                Timber.d("create new session callback")
                val callback = SessionCallback(captureBuilder.build())
                session = callback

                Timber.d("register capture session")
                camera.createCaptureSession(list, callback, null)

                cameraInterface.notifyCallbackOnOpened()
            } catch (e: CameraAccessException) {
                Timber.e(e, "onOpened")
            }
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            this.cameraDevice = null
            Timber.d("onDisconnected")
            opened = false
        }

        override fun onError(cameraDevice: CameraDevice, i: Int) {
            this.cameraDevice = null
            Timber.e("onError")
            opened = false
        }

        companion object {

            @CheckResult
            @JvmStatic
            @Throws(CameraAccessException::class)
            fun getSmallestSize(
                manager: CameraManager, id: String
            ): Size {
                Timber.d("Get stream config map")
                val map = manager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        ?: throw IllegalStateException("Camera $id doesn't support any Stream Maps.")

                Timber.d("Get possible output sizes")
                val outputSizes = map.getOutputSizes(SurfaceTexture::class.java)
                if (outputSizes == null || outputSizes.isEmpty()) {
                    throw IllegalStateException("Camera " + id + "doesn't support any outputSize.")
                }

                Timber.d("Select a size")
                var chosen = outputSizes[0]
                outputSizes
                    .asSequence()
                    .filter { chosen.width >= it.width && chosen.height >= it.height }
                    .forEach { chosen = it }
                return chosen
            }
        }
    }

    internal class SessionCallback internal constructor(
        private val request: CaptureRequest
    ) : CameraCaptureSession.StateCallback() {

        private var session: CameraCaptureSession? = null

        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
            Timber.d("Camera configured")
            session = cameraCaptureSession
            try {
                Timber.d("set repeating")
                cameraCaptureSession.setRepeatingRequest(request, null, null)
            } catch (e: CameraAccessException) {
                Timber.e(e, "onConfigured")
            }
        }

        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
            Timber.e("onConfigureFailed")
            session = cameraCaptureSession
            close()
        }

        fun close() {
            val obj = session
            if (obj != null) {
                Timber.d("close session")
                obj.close()
                session = null
            }
        }
    }
}
