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

package com.pyamsoft.zaptorch

import android.app.Application
import android.app.Service
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.zaptorch.service.TorchOffService
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

class ZapTorch : Application() {

  private var component: ZapTorchComponent? = null
  private var refWatcher: RefWatcher? = null

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

    PYDroid.init(
        this,
        getString(R.string.app_name),
        "https://github.com/pyamsoft/zaptorch",
        "https://github.com/pyamsoft/zaptorch/issues",
        BuildConfig.VERSION_CODE,
        BuildConfig.DEBUG
    ) { provider ->
      component = DaggerZapTorchComponent.factory()
          .create(
              this,
              provider.theming(),
              provider.enforcer(),
              provider.imageLoader(),
              TorchOffService::class.java,
              R.color.primary,
              getString(R.string.handle_volume_keys_key)
          )
    }
  }

  override fun getSystemService(name: String): Any? {
    val service = PYDroid.getSystemService(name)
    if (service != null) {
      return service
    }

    if (ZapTorchComponent::class.java.name == name) {
      return requireNotNull(component)
    }

    return super.getSystemService(name)
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun getRefWatcher(service: Service): RefWatcher = getRefWatcherInternal(service.application)

    @JvmStatic
    @CheckResult
    private fun getRefWatcherInternal(application: Application): RefWatcher {
      if (application is ZapTorch) {
        return requireNotNull(application.refWatcher)
      } else {
        throw IllegalStateException("Application is not ZapTorch")
      }
    }
  }
}
