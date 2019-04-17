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
import com.pyamsoft.pydroid.arch.renderOnChange
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.CameraViewModel.ServiceState
import com.pyamsoft.zaptorch.service.ServiceFinishViewModel.FinishState
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import timber.log.Timber
import javax.inject.Inject

class VolumeMonitorService : AccessibilityService() {

  @field:Inject internal lateinit var stateViewModel: ServiceStateViewModel
  @field:Inject internal lateinit var finishViewModel: ServiceFinishViewModel
  @field:Inject internal lateinit var cameraViewModel: CameraViewModel

  override fun onKeyEvent(event: KeyEvent): Boolean {
    val action = event.action
    val keyCode = event.keyCode
    cameraViewModel.handleKeyEvent(action, keyCode)

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

    cameraViewModel.bind { state, oldState ->
      renderError(state, oldState)
    }
    finishViewModel.bind { state, oldState ->
      renderFinish(state, oldState)
    }
  }

  private fun renderError(
    state: ServiceState,
    oldState: ServiceState?
  ) {
    state.renderOnChange(oldState, value = { it.throwable }) { throwable ->
      if (throwable != null) {
        applicationContext.also {
          val intent = throwable.intent
          intent.setClass(it, CameraErrorExplanation::class.java)
          it.startActivity(intent)
        }
      }
    }
  }

  private fun renderFinish(
    state: FinishState,
    oldState: FinishState?
  ) {
    state.renderOnChange(oldState, value = { it.isFinished }) { finished ->
      if (finished) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          disableSelf()
        }
      }
    }
  }

  override fun onServiceConnected() {
    super.onServiceConnected()
    stateViewModel.start()
  }

  override fun onUnbind(intent: Intent): Boolean {
    stateViewModel.stop()
    return super.onUnbind(intent)
  }

  override fun onDestroy() {
    super.onDestroy()

    finishViewModel.unbind()
    cameraViewModel.unbind()

    ZapTorch.getRefWatcher(this)
        .watch(this)
  }
}
