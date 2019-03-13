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

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiComponent
import com.pyamsoft.pydroid.arch.doOnDestroy
import com.pyamsoft.zaptorch.main.MainFragmentUiComponent.Callback
import com.pyamsoft.zaptorch.service.ServiceStatePresenter

internal class MainFragmentUiComponentImpl internal constructor(
  private val presenter: MainFragmentPresenter,
  private val serviceStatePresenter: ServiceStatePresenter,
  private val frameView: MainFrameView,
  private val actionView: MainActionView
) : BaseUiComponent<MainFragmentUiComponent.Callback>(),
    MainFragmentUiComponent,
    ServiceStatePresenter.Callback,
    MainFragmentPresenter.Callback {

  override fun id(): Int {
    return frameView.id()
  }

  override fun onBind(
    owner: LifecycleOwner,
    savedInstanceState: Bundle?,
    callback: Callback
  ) {
    owner.doOnDestroy {
      presenter.unbind()
      serviceStatePresenter.unbind()
    }

    frameView.inflate(savedInstanceState)
    actionView.inflate(savedInstanceState)
    presenter.bind(this)
    serviceStatePresenter.bind(this)
  }

  override fun saveState(outState: Bundle) {
    frameView.saveState(outState)
    actionView.saveState(outState)
  }

  override fun onServiceStarted() {
    actionView.setFabFromServiceState(true)
  }

  override fun onServiceStopped() {
    actionView.setFabFromServiceState(false)
  }

  override fun onServiceRunningAction() {
    callback.showInfoDialog()
  }

  override fun onServiceStoppedAction() {
    callback.showUsageAccessRequestDialog()
  }

  override fun onSignificantScrollEvent(visible: Boolean) {
    actionView.toggleVisibility(visible)
  }

}