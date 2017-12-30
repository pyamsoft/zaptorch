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
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Lifecycle.Event.ON_CREATE
import android.arch.lifecycle.Lifecycle.Event.ON_DESTROY
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LifecycleRegistry
import android.content.Intent
import android.os.Build
import android.support.annotation.CheckResult
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.pyamsoft.zaptorch.Injector
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
        Injector.obtain<ZapTorchComponent>(applicationContext).inject(this)
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
    }

    companion object {

        var isRunning: Boolean = false
            @CheckResult get
            private set
    }
}
