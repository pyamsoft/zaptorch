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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import android.content.Intent
import android.os.Build
import androidx.annotation.CheckResult
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.lifecycle.fakePauseStop
import com.pyamsoft.zaptorch.lifecycle.fakeStartResume
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import timber.log.Timber

class VolumeMonitorService : AccessibilityService(), VolumeServicePresenter.View, LifecycleOwner {

  private val lifecycle = LifecycleRegistry(this)
  internal lateinit var presenter: VolumeServicePresenter

  override fun getLifecycle(): Lifecycle = lifecycle

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
    Injector.obtain<ZapTorchComponent>(applicationContext)
        .inject(this)
    presenter.bind(this, this)
    lifecycle.handleLifecycleEvent(ON_CREATE)
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    lifecycle.fakeStartResume()
    isRunning = true
  }

  override fun onFinishService() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      disableSelf()
    }
  }

  override fun onError(intent: Intent) {
    intent.setClass(applicationContext, CameraErrorExplanation::class.java)
    application.startActivity(intent)
  }

  override fun onUnbind(intent: Intent): Boolean {
    isRunning = false
    lifecycle.fakePauseStop()
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    super.onDestroy()
    lifecycle.handleLifecycleEvent(ON_DESTROY)
    ZapTorch.getRefWatcher(this)
        .watch(this)
  }

  companion object {

    var isRunning: Boolean = false
      @CheckResult get
      private set
  }
}
