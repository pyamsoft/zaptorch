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

package com.pyamsoft.zaptorch

import android.app.Application
import android.support.annotation.CheckResult
import android.support.v4.app.Fragment
import com.pyamsoft.pydroid.about.Licenses
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.ui.app.fragment.ActionBarSettingsPreferenceFragment
import com.pyamsoft.zaptorch.base.ZapTorchModule
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.uicode.WatchedDialog
import com.pyamsoft.zaptorch.uicode.WatchedFragment
import com.pyamsoft.zaptorch.uicode.WatchedPreferenceFragment
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

class ZapTorch : Application(), ComponentProvider {

  private lateinit var refWatcher: RefWatcher
  private var component: ZapTorchComponent? = null

  @CheckResult override fun getComponent(): ZapTorchComponent {
    val obj = component
    if (obj == null) {
      throw IllegalStateException("ZapTorchComponent must be initialized before use")
    } else {
      return obj
    }
  }

  override fun onCreate() {
    super.onCreate()
    if (LeakCanary.isInAnalyzerProcess(this)) {
      return
    }

    PYDroid.initialize(this, BuildConfig.DEBUG)
    Licenses.create("Firebase", "https://firebase.google.com", "licenses/firebase")
    component = ZapTorchComponentImpl(ZapTorchModule(this, TorchOffService::class.java))

    refWatcher = if (BuildConfig.DEBUG) {
      // Assign
      LeakCanary.install(this)
    } else {
      // Assign
      RefWatcher.DISABLED
    }
  }

  private val watcher: RefWatcher
    @CheckResult get() = refWatcher

  companion object {
    @JvmStatic
    @CheckResult
    fun getRefWatcher(fragment: WatchedDialog): RefWatcher =
        getRefWatcherInternal(fragment)

    @JvmStatic
    @CheckResult
    fun getRefWatcher(fragment: WatchedPreferenceFragment): RefWatcher =
        getRefWatcherInternal(fragment)

    @JvmStatic
    @CheckResult
    fun getRefWatcher(fragment: WatchedFragment): RefWatcher =
        getRefWatcherInternal(fragment)

    @JvmStatic
    @CheckResult
    fun getRefWatcher(
        fragment: ActionBarSettingsPreferenceFragment): RefWatcher = getRefWatcherInternal(fragment)

    @JvmStatic
    @CheckResult private fun getRefWatcherInternal(fragment: Fragment): RefWatcher {
      val application = fragment.activity.application
      if (application is ZapTorch) {
        return application.watcher
      } else {
        throw IllegalStateException("Application is not ZapTorch")
      }
    }
  }
}
