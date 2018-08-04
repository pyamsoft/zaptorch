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
import androidx.annotation.CheckResult
import androidx.fragment.app.Fragment
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.zaptorch.base.ZapTorchModuleImpl
import com.pyamsoft.zaptorch.service.TorchOffService
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

class ZapTorch : Application(), PYDroid.Instance {

  private var pyDroid: PYDroid? = null
  private lateinit var component: ZapTorchComponent
  private lateinit var refWatcher: RefWatcher

  override fun onCreate() {
    super.onCreate()
    if (LeakCanary.isInAnalyzerProcess(this)) {
      return
    }

    if (BuildConfig.DEBUG) {
      refWatcher = LeakCanary.install(this)
    } else {
      refWatcher = RefWatcher.DISABLED
    }

    PYDroid.init(this, this, BuildConfig.DEBUG)
  }

  override fun getPydroid(): PYDroid? = pyDroid

  override fun setPydroid(instance: PYDroid) {
    pyDroid = instance.also {
      component = ZapTorchComponentImpl(
          it.enforcer(),
          ZapTorchModuleImpl(
              this,
              it.modules().loaderModule(),
              TorchOffService::class.java,
              R.color.purple500
          )
      )
    }
  }

  override fun getSystemService(name: String?): Any {
    if (Injector.name == name) {
      return component
    } else {
      return super.getSystemService(name)
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun getRefWatcher(fragment: Fragment): RefWatcher =
      getRefWatcherInternal(fragment.requireActivity().application)

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
