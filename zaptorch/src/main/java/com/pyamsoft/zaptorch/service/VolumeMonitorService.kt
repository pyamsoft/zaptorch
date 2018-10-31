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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import com.pyamsoft.pydroid.util.fakeUnbind
import com.pyamsoft.pydroid.util.fakeBind
import timber.log.Timber

class VolumeMonitorService : AccessibilityService(), LifecycleOwner {

  private val registry = LifecycleRegistry(this)
  internal lateinit var viewModel: VolumeServiceViewModel

  override fun onKeyEvent(event: KeyEvent): Boolean {
    val action = event.action
    val keyCode = event.keyCode
    viewModel.handleKeyEvent(action, keyCode)

    // Never consume events
    return false
  }

  override fun getLifecycle(): Lifecycle {
    return registry
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
        .plusServiceComponent(this)
        .inject(this)

    viewModel.onCameraError { onCameraError(it) }
    viewModel.onServiceFinishEvent { onFinishService() }
    viewModel.onTorchEvent { Timber.d("Toggling torch") }

    registry.fakeBind()
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    viewModel.setServiceState(true)
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
    viewModel.setServiceState(false)
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    registry.fakeUnbind()
    ZapTorch.getRefWatcher(this)
        .watch(this)
  }
}
