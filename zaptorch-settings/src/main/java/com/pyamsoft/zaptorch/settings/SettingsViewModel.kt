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

package com.pyamsoft.zaptorch.settings

import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.core.threads.Enforcer
import com.pyamsoft.pydroid.core.viewmodel.LifecycleViewModel
import com.pyamsoft.zaptorch.api.SettingsPreferenceFragmentInteractor
import com.pyamsoft.zaptorch.model.ConfirmEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SettingsViewModel internal constructor(
  private val enforcer: Enforcer,
  private val bus: EventBus<ConfirmEvent>,
  private val interactor: SettingsPreferenceFragmentInteractor
) : LifecycleViewModel {

  fun onClearAllEvent(
    owner: LifecycleOwner,
    func: () -> Unit
  ) {
    bus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .flatMapSingle {
          enforcer.assertNotOnMainThread()
          return@flatMapSingle interactor.clearAll()
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { func() }
        .bind(owner)
  }
}
