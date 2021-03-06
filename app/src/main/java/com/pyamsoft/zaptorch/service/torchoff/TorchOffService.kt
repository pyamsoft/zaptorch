/*
 * Copyright 2020 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.service.torchoff

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.zaptorch.ZapTorchComponent
import javax.inject.Inject
import timber.log.Timber

class TorchOffService : JobIntentService(), LifecycleOwner {

  @JvmField @Inject internal var binder: TorchBinder? = null

  private val registry by lazy(LazyThreadSafetyMode.NONE) { LifecycleRegistry(this) }

  override fun getLifecycle(): Lifecycle {
    return registry
  }

  override fun onCreate() {
    super.onCreate()
    Injector.obtainFromApplication<ZapTorchComponent>(this)
        .plusServiceComponent()
        .create()
        .inject(this)

    requireNotNull(binder).bind(lifecycleScope) {
      // This should not handle any events
    }

    registry.currentState = Lifecycle.State.RESUMED
  }

  override fun onDestroy() {
    super.onDestroy()

    binder?.unbind()
    binder = null

    registry.currentState = Lifecycle.State.DESTROYED
  }

  override fun onHandleWork(intent: Intent) {
    try {
      requireNotNull(binder).handleToggle(lifecycleScope)
    } catch (e: IllegalStateException) {
      Timber.e(e, "Error toggling torch")
    }
  }

  companion object {

    private const val JOB_ID = 42069

    @JvmStatic
    fun enqueue(context: Context) {
      val intent = Intent(context.applicationContext, TorchOffService::class.java)
      enqueueWork(context, TorchOffService::class.java, JOB_ID, intent)
    }
  }
}
