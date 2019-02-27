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

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.BasePresenter
import com.pyamsoft.pydroid.arch.destroy
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.zaptorch.api.SettingsInteractor
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class ClearAllPresenterImpl internal constructor(
  private val interactor: SettingsInteractor,
  bus: EventBus<ClearAllEvent>
) : BasePresenter<ClearAllEvent, ClearAllPresenter.Callback>(bus), ClearAllPresenter {

  @CheckResult
  private fun clear(): Single<Unit> {
    return interactor.clearAll()
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
  }

  override fun onBind() {
    listen().ofType(ClearAllEvent::class.java)
        .flatMapSingle { clear() }
        .subscribeOn(Schedulers.trampoline())
        .observeOn(Schedulers.trampoline())
        .subscribe { callback.onClearAll() }
        .destroy(owner)
  }

  override fun onUnbind() {
  }

  override fun clearAll() {
    publish(ClearAllEvent)
  }
}

