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

package com.pyamsoft.zaptorch

import android.app.Application
import android.app.Service
import android.support.annotation.CheckResult
import com.pyamsoft.pydroid.PYDroidModule
import com.pyamsoft.pydroid.PYDroidModuleImpl
import com.pyamsoft.pydroid.base.about.Licenses
import com.pyamsoft.pydroid.loader.LoaderModule
import com.pyamsoft.pydroid.loader.LoaderModuleImpl
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.ui.app.fragment.SettingsPreferenceFragment
import com.pyamsoft.zaptorch.base.ZapTorchModuleImpl
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.uicode.WatchedDialog
import com.pyamsoft.zaptorch.uicode.WatchedFragment
import com.pyamsoft.zaptorch.uicode.WatchedPreferenceFragment
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

class ZapTorch : Application() {

  private lateinit var refWatcher: RefWatcher
  private var component: ZapTorchComponent? = null
  private lateinit var pydroidModule: PYDroidModule
  private lateinit var loaderModule: LoaderModule

  override fun onCreate() {
    super.onCreate()
    if (LeakCanary.isInAnalyzerProcess(this)) {
      return
    }

    pydroidModule = PYDroidModuleImpl(this, BuildConfig.DEBUG)
    loaderModule = LoaderModuleImpl(pydroidModule)
    PYDroid.init(pydroidModule, loaderModule)
    Licenses.create("Firebase", "https://firebase.google.com", "licenses/firebase")

    refWatcher = if (BuildConfig.DEBUG) {
      // Assign
      LeakCanary.install(this)
    } else {
      // Assign
      RefWatcher.DISABLED
    }
  }

  private fun buildComponent(): ZapTorchComponent =
    ZapTorchComponentImpl(
        ZapTorchModuleImpl(pydroidModule, loaderModule, TorchOffService::class.java)
    )

  override fun getSystemService(name: String?): Any {
    return if (Injector.name == name) {
      val zaptorch: ZapTorchComponent
      val obj = component
      if (obj == null) {
        zaptorch = buildComponent()
        component = zaptorch
      } else {
        zaptorch = obj
      }

      // Return
      zaptorch
    } else {
      // Return
      super.getSystemService(name)
    }
  }

  companion object {
    @JvmStatic
    @CheckResult
    fun getRefWatcher(fragment: WatchedDialog): RefWatcher =
      getRefWatcherInternal(fragment.activity!!.application)

    @JvmStatic
    @CheckResult
    fun getRefWatcher(fragment: WatchedPreferenceFragment): RefWatcher =
      getRefWatcherInternal(fragment.activity!!.application)

    @JvmStatic
    @CheckResult
    fun getRefWatcher(fragment: WatchedFragment): RefWatcher =
      getRefWatcherInternal(fragment.activity!!.application)

    @JvmStatic
    @CheckResult
    fun getRefWatcher(
      fragment: SettingsPreferenceFragment
    ): RefWatcher = getRefWatcherInternal(
        fragment.activity!!.application
    )

    @JvmStatic
    @CheckResult
    fun getRefWatcher(service: Service): RefWatcher = getRefWatcherInternal(service.application)

    @JvmStatic
    @CheckResult
    private fun getRefWatcherInternal(application: Application): RefWatcher {
      if (application is ZapTorch) {
        return application.refWatcher
      } else {
        throw IllegalStateException("Application is not ZapTorch")
      }
    }
  }
}
