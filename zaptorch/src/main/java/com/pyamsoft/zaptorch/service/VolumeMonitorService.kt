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

package com.pyamsoft.zaptorch.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.pydroid.ui.arch.destroy
import com.pyamsoft.pydroid.util.fakeBind
import com.pyamsoft.pydroid.util.fakeUnbind
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import timber.log.Timber

class VolumeMonitorService : AccessibilityService(), LifecycleOwner {

  private val registry = LifecycleRegistry(this)

  override fun getLifecycle(): Lifecycle {
    return registry
  }

  internal lateinit var stateWorker: ServiceStateWorker
  internal lateinit var finishWorker: ServiceFinishWorker
  internal lateinit var torchWorker: TorchWorker
  internal lateinit var serviceWorker: ServiceWorker

  private var handleKeyDisposable by singleDisposable()

  override fun onKeyEvent(event: KeyEvent): Boolean {
    val action = event.action
    val keyCode = event.keyCode
    handleKeyDisposable = serviceWorker.handleKeyEvent(action, keyCode)

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

    serviceWorker.onCameraError { onCameraError(it.intent) }
        .destroy(this)

    torchWorker.onRequest {
      Timber.d("Toggling torch")
      serviceWorker.toggleTorch()
    }
        .destroy(this)

    finishWorker.onFinishEvent { onServiceFinishRequested() }
        .destroy(this)

    registry.fakeBind()
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    stateWorker.setServiceState(true)
  }

  private fun onServiceFinishRequested() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      disableSelf()
    }
  }

  private fun onCameraError(intent: Intent) {
    applicationContext.also {
      intent.setClass(it, CameraErrorExplanation::class.java)
      it.startActivity(intent)
    }
  }

  override fun onUnbind(intent: Intent): Boolean {
    stateWorker.setServiceState(false)
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    registry.fakeUnbind()
    handleKeyDisposable.tryDispose()

    ZapTorch.getRefWatcher(this)
        .watch(this)
  }
}
