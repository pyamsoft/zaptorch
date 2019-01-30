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
import com.pyamsoft.pydroid.core.bus.Listener
import com.pyamsoft.pydroid.ui.arch.BaseUiComponent
import com.pyamsoft.pydroid.ui.arch.destroy
import com.pyamsoft.zaptorch.service.ServiceStateWorker
import com.pyamsoft.zaptorch.settings.SettingsStateEvent
import com.pyamsoft.zaptorch.settings.SettingsStateEvent.ClearAllEvent
import com.pyamsoft.zaptorch.settings.SettingsStateEvent.SignificantScroll
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

internal class MainActionUiComponent internal constructor(
  private val controllerBus: Listener<SettingsStateEvent>,
  private val worker: ServiceStateWorker,
  view: MainActionView,
  owner: LifecycleOwner
) : BaseUiComponent<ActionViewEvent, MainActionView>(view, owner) {

  override fun onCreate(savedInstanceState: Bundle?) {
    worker.onStateEvent { view.setFabFromServiceState(it) }
        .destroy(owner)

    controllerBus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          return@subscribe when (it) {
            is SignificantScroll -> view.toggleVisibility(it.visible)
            is ClearAllEvent -> Timber.d("Ignoring event: $it")
          }
        }
        .destroy(owner)
  }

}
