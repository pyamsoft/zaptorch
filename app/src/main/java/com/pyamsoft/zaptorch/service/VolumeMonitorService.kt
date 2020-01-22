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
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.api.CameraInterface.CameraError
import com.pyamsoft.zaptorch.service.ServiceControllerEvent.Finish
import com.pyamsoft.zaptorch.service.ServiceControllerEvent.RenderError
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation
import javax.inject.Inject
import timber.log.Timber

class VolumeMonitorService : AccessibilityService() {

    @JvmField
    @Inject
    internal var binder: ServiceBinder? = null

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        binder?.handleKeyEvent(action, keyCode)

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
            .plusServiceComponent()
            .create()
            .inject(this)

        requireNotNull(binder).bind {
            return@bind when (it) {
                is RenderError -> renderError(it.error)
                is Finish -> finish()
            }
        }
    }

    private fun renderError(cameraError: CameraError) {
        applicationContext.also {
            val intent = cameraError.intent
            intent.setClass(it, CameraErrorExplanation::class.java)
            it.startActivity(intent)
        }
    }

    private fun finish() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            disableSelf()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        requireNotNull(binder).start()
    }

    override fun onUnbind(intent: Intent): Boolean {
        binder?.stop()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        binder?.unbind()
        binder = null
    }
}
