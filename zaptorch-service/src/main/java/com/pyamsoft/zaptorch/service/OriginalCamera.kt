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

@file:Suppress("DEPRECATION")

package com.pyamsoft.zaptorch.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.hardware.Camera
import android.support.annotation.CheckResult
import android.support.v4.app.ActivityCompat
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import com.pyamsoft.pydroid.helper.Optional
import io.reactivex.Scheduler
import io.reactivex.Single
import timber.log.Timber
import java.io.IOException

internal class OriginalCamera internal constructor(context: Context,
    interactor: VolumeServiceInteractor,
    obsScheduler: Scheduler, subScheduler: Scheduler) : CameraCommon(context, interactor,
    obsScheduler, subScheduler), SurfaceHolder.Callback {

  val windowManager = context.applicationContext.getSystemService(
      Context.WINDOW_SERVICE) as WindowManager
  val surfaceView = SurfaceView(context.applicationContext)
  val params = WindowManager.LayoutParams()
  var camera: Camera? = null
  var opened: Boolean = false

  init {
    params.width = 1
    params.height = 1
    params.gravity = Gravity.TOP or Gravity.START
    params.format = PixelFormat.TRANSLUCENT
    params.type = WindowManager.LayoutParams.TYPE_TOAST
    params.flags = (WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
  }

  @CheckResult fun getInitializedHolder(): SurfaceHolder {
    val holder = surfaceView.holder
    holder.addCallback(this)
    return holder
  }

  override fun toggleTorch() {
    if (ActivityCompat.checkSelfPermission(appContext,
        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
      if (opened) {
        Timber.d("Camera is open, close it")
        release()
      } else {
        connectToCameraService()
      }
    } else {
      Timber.e("Missing setupCamera permission")
      startPermissionExplanationActivity()
    }
  }

  private fun connectToCameraService() {
    Timber.d("Camera is closed, open it")
    disposeOnStop {
      Single.fromCallable { Optional.ofNullable(Camera.open()) }
          .map {
            if (it.isPresent()) {
              return@map it.item()
            } else {
              throw IllegalStateException("Camera failed to open")
            }
          }
          .subscribeOn(backgroundScheduler)
          .observeOn(foregroundScheduler)
          .subscribe({ cameraOpened(it) }, {
            Timber.e(it, "onError connectToCameraService")
            clearCamera(it)
            startErrorExplanationActivity()
          })
    }
  }

  fun cameraOpened(camera: Camera) {
    val parameters = camera.parameters
    if (parameters.flashMode == null) {
      Timber.e("Null flash mode")
      camera.release()
      startErrorExplanationActivity()
      return
    }

    val supportedFlashModes = parameters.supportedFlashModes
    if (supportedFlashModes == null
        || supportedFlashModes.isEmpty()
        || !supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
      Timber.e("Camera parameters do not include Torch")
      camera.release()
      startErrorExplanationActivity()
      return
    }

    Timber.d("Camera should have torch mode")
    try {
      Timber.d("Camera has flash")
      this.camera = camera
      windowManager.addView(surfaceView, params)
      assert(this.camera != null)
      Timber.d("set preview")
      val holder = getInitializedHolder()
      camera.setPreviewDisplay(holder)
      val cameraParameters = camera.parameters
      cameraParameters.flashMode = Camera.Parameters.FLASH_MODE_TORCH
      camera.parameters = cameraParameters

      Timber.d("start setupCamera")
      camera.startPreview()
      opened = true
      notifyCallbackOnOpened()
    } catch (e: IOException) {
      clearCamera(e)
      startErrorExplanationActivity()
    }
  }

  fun clearCamera(throwable: Throwable) {
    Timber.e(throwable, "Error opening setupCamera")
    release()
  }

  private fun releaseCamera() {
    Timber.d("release setupCamera")
    val obj = camera
    if (obj != null) {
      obj.release()
      camera = null
    }
  }

  override fun onStop() {
    release()
  }

  override fun release() {
    if (opened) {
      val obj = camera
      if (obj != null) {
        val params = obj.parameters
        params.flashMode = Camera.Parameters.FLASH_MODE_OFF
        obj.parameters = params
        obj.stopPreview()
        releaseCamera()
        windowManager.removeView(surfaceView)
      }
      opened = false
      notifyCallbackOnClosed()
    }
  }

  override fun surfaceCreated(surfaceHolder: SurfaceHolder?) {
    val obj = camera
    if (surfaceHolder != null && obj != null) {
      try {
        Timber.d("Surface created")
        obj.setPreviewDisplay(surfaceHolder)
      } catch (e: IOException) {
        Timber.e(e, "surfaceCreated ERROR")
      }

    }
  }

  override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {

  }

  override fun surfaceDestroyed(surfaceHolder: SurfaceHolder?) {
    val obj = camera
    if (surfaceHolder != null && obj != null) {
      Timber.d("Surface destroyed")
      obj.stopPreview()
    }
  }
}
