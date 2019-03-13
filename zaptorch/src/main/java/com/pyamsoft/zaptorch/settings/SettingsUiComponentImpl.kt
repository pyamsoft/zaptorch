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

package com.pyamsoft.zaptorch.settings

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiComponent
import com.pyamsoft.pydroid.arch.doOnDestroy
import com.pyamsoft.zaptorch.service.ServiceFinishPresenter
import com.pyamsoft.zaptorch.settings.SettingsUiComponent.Callback
import timber.log.Timber

internal class SettingsUiComponentImpl internal constructor(
  private val settingsView: SettingsView,
  private val presenter: SettingsPresenter,
  private val serviceFinishPresenter: ServiceFinishPresenter,
  private val clearPresenter: ClearAllPresenter
) : BaseUiComponent<SettingsUiComponent.Callback>(),
    SettingsUiComponent,
    SettingsPresenter.Callback,
    ClearAllPresenter.Callback {

  override fun onBind(
    owner: LifecycleOwner,
    savedInstanceState: Bundle?,
    callback: Callback
  ) {
    owner.doOnDestroy {
      settingsView.teardown()
      presenter.unbind()
      clearPresenter.unbind()
    }

    settingsView.inflate(savedInstanceState)
    presenter.bind(this)
    clearPresenter.bind(this)
  }

  override fun saveState(outState: Bundle) {
    settingsView.saveState(outState)
  }

  override fun onShowExplanation() {
    callback.showHowTo()
  }

  override fun onClearAll() {
    try {
      serviceFinishPresenter.finish()
    } catch (e: NullPointerException) {
      Timber.e(e, "Expected exception when Service is NULL")
    }

    callback.onKillApplication()

  }
}