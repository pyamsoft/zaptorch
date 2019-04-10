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
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.api.CameraInterface.CameraError
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import timber.log.Timber

class VolumeMonitorService : AccessibilityService(),
    ServiceFinishBinder.Callback,
    ServiceBinder.Callback,
    TorchBinder.Callback,
    ServiceStateBinder.Callback {

  internal lateinit var stateBinder: ServiceStateBinder
  internal lateinit var finishBinder: ServiceFinishBinder
  internal lateinit var torchBinder: TorchBinder
  internal lateinit var serviceBinder: ServiceBinder

  override fun onKeyEvent(event: KeyEvent): Boolean {
    val action = event.action
    val keyCode = event.keyCode
    serviceBinder.handleKeyEvent(action, keyCode)

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

    stateBinder.bind(this)
    torchBinder.bind(this)
    serviceBinder.bind(this)
    finishBinder.bind(this)
  }

  override fun handleCameraError(error: CameraError) {
    applicationContext.also {
      val intent = error.intent
      intent.setClass(it, CameraErrorExplanation::class.java)
      it.startActivity(intent)
    }
  }

  override fun onServiceFinished() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      disableSelf()
    }
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    stateBinder.start()
  }

  override fun onUnbind(intent: Intent): Boolean {
    stateBinder.stop()
    return super.onUnbind(intent)
  }

  override fun onServiceStarted() {
  }

  override fun onServiceStopped() {
  }

  override fun onDestroy() {
    super.onDestroy()

    stateBinder.unbind()
    torchBinder.unbind()
    serviceBinder.unbind()
    finishBinder.unbind()

    ZapTorch.getRefWatcher(this)
        .watch(this)
  }
}
