/*
 * Copyright (C) 2018 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.addTo
import com.pyamsoft.pydroid.core.disposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class VolumeMonitorService : AccessibilityService() {

  internal lateinit var viewModel: VolumeServiceViewModel
  private val compositeDisposable = CompositeDisposable()
  private var keyDisposable by disposable()

  override fun onKeyEvent(event: KeyEvent): Boolean {
    val action = event.action
    val keyCode = event.keyCode
    keyDisposable = viewModel.handleKeyEvent(action, keyCode)

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
    Injector.obtain<ZapTorchComponent>(applicationContext)
        .inject(this)

    viewModel.onCameraError { onCameraError(it) }
        .addTo(compositeDisposable)
    viewModel.onServiceFinishEvent { onFinishService() }
        .addTo(compositeDisposable)
    viewModel.onTorchEvent { Timber.d("Toggling torch") }
        .addTo(compositeDisposable)
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    isRunning = true
  }

  private fun onFinishService() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      disableSelf()
    }
  }

  private fun onCameraError(intent: Intent) {
    intent.setClass(applicationContext, CameraErrorExplanation::class.java)
    application.startActivity(intent)
  }

  override fun onUnbind(intent: Intent): Boolean {
    isRunning = false
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    ZapTorch.getRefWatcher(this)
        .watch(this)
    compositeDisposable.clear()
    keyDisposable.tryDispose()
  }

  companion object {

    var isRunning: Boolean = false
      @CheckResult get
      private set
  }
}
