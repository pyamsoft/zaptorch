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

package com.pyamsoft.zaptorch.main

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiComponent
import com.pyamsoft.pydroid.arch.doOnDestroy
import com.pyamsoft.pydroid.ui.navigation.FailedNavigationBinder
import com.pyamsoft.zaptorch.main.MainUiComponent.Callback

internal class MainUiComponentImpl internal constructor(
  private val frameView: MainFrameView,
  private val binder: MainBinder,
  private val failedNavigation: FailedNavigationBinder
) : BaseUiComponent<MainUiComponent.Callback>(),
    MainUiComponent,
    MainBinder.Callback {

  override fun id(): Int {
    return frameView.id()
  }

  override fun onBind(
    owner: LifecycleOwner,
    savedInstanceState: Bundle?,
    callback: Callback
  ) {
    owner.doOnDestroy {
      frameView.teardown()
      binder.unbind()
    }

    frameView.inflate(savedInstanceState)
    binder.bind(this)
  }

  override fun onSaveState(outState: Bundle) {
    frameView.saveState(outState)
  }

  override fun failedNavigation(error: ActivityNotFoundException) {
    failedNavigation.failedNavigation(error)
  }

  override fun handleKeypressChanged(handle: Boolean) {
    callback.onHandleKeyPressChanged(handle)
  }

}