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

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.support.annotation.CheckResult
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import timber.log.Timber

class VolumeMonitorService : AccessibilityService(), VolumeServicePresenter.View {

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
    Injector.obtain<ZapTorchComponent>(applicationContext).inject(this)
    presenter.bind(this)
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
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
    presenter.unbind()
    presenter.bind(this)

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

  private fun setupCamera() {
    presenter.setupCamera { errorIntent ->
      errorIntent.setClass(applicationContext, CameraErrorExplanation::class.java)
      application.startActivity(errorIntent)
    }
  }

  override fun onUnbind(intent: Intent): Boolean {
    isRunning = false
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    presenter.unbind()
  }

  companion object {

    var isRunning: Boolean = false
      @CheckResult get
      private set

  }
}
