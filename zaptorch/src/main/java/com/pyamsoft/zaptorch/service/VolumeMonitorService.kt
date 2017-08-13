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

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.support.annotation.CheckResult
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.service.VolumeServicePresenter.Callback
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import timber.log.Timber

class VolumeMonitorService : AccessibilityService(), Callback {

  internal lateinit var presenter: VolumeServicePresenter

  override fun onKeyEvent(event: KeyEvent): Boolean {
    val action = event.action
    val keyCode = event.keyCode
    presenter.handleKeyEvent(action, keyCode)

    // Never consume events
    return false
  }

  override fun onAccessibilityEvent(accessibilityEvent: AccessibilityEvent) {
    Timber.d("onAccessibilityEvent")
  }

  override fun onInterrupt() {
    Timber.e("onInterrupt")
  }

  override fun onCreate() {
    super.onCreate()
    Injector.with(this) {
      it.inject(this)
    }
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    presenter.start(this)
    setupCamera()
    isRunning = true
  }

  override fun onToggleTorch() {
    Timber.d("Toggle Torch")
    presenter.toggleTorch()
  }

  override fun onChangeCameraApi() {
    // Simulate the lifecycle for destroying and re-creating the publisher
    Timber.d("Change setupCamera API")
    presenter.stop()

    try {
      Thread.sleep(200)
    } catch (e: InterruptedException) {
      Timber.e(e, "Sleep interrupt")
    }

    setupCamera()
  }

  override fun onFinishService() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      disableSelf()
    }
  }

  internal fun setupCamera() {
    presenter.setupCamera { errorIntent ->
      errorIntent.setClass(applicationContext, CameraErrorExplanation::class.java)
      application.startActivity(errorIntent)
    }
  }

  override fun onUnbind(intent: Intent): Boolean {
    presenter.stop()
    isRunning = false
    return super.onUnbind(intent)
  }

  companion object {

    var isRunning: Boolean = false
      @CheckResult get
      private set

  }
}
