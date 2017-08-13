/*
 * Copyright 2017 Peter Kenji Yamanaka
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

    if (BuildConfig.DEBUG) {
      refWatcher = LeakCanary.install(this)
    } else {
      refWatcher = RefWatcher.DISABLED
    }
  }

  private val watcher: RefWatcher
    @CheckResult get() {
      return refWatcher
    }

  companion object {
    @JvmStatic @CheckResult fun getRefWatcher(fragment: WatchedDialog): RefWatcher {
      return getRefWatcherInternal(fragment)
    }

    @JvmStatic @CheckResult fun getRefWatcher(fragment: WatchedPreferenceFragment): RefWatcher {
      return getRefWatcherInternal(fragment)
    }

    @JvmStatic @CheckResult fun getRefWatcher(fragment: WatchedFragment): RefWatcher {
      return getRefWatcherInternal(fragment)
    }

    @JvmStatic @CheckResult fun getRefWatcher(
        fragment: ActionBarSettingsPreferenceFragment): RefWatcher {
      return getRefWatcherInternal(fragment)
    }

    @JvmStatic @CheckResult private fun getRefWatcherInternal(fragment: Fragment): RefWatcher {
      val application = fragment.activity.application
      if (application is ZapTorch) {
        return application.watcher
      } else {
        throw IllegalStateException("Application is not ZapTorch")
      }
    }
  }
}
