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

import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.arch.BasePresenter
import com.pyamsoft.pydroid.arch.destroy
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal class MainFragmentPresenterImpl internal constructor(
  bus: EventBus<SignificantScrollEvent>
) : BasePresenter<SignificantScrollEvent, MainFragmentPresenter.Callback>(bus),
    MainFragmentPresenter,
    MainActionView.Callback {

  override fun onActionButtonClicked(running: Boolean) {
    if (running) {
      callback.onServiceRunningAction()
    } else {
      callback.onServiceStoppedAction()
    }
  }

  override fun onBind() {
    listen().subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { callback.onSignificantScrollEvent(it.visible) }
        .destroy(owner)
  }

  override fun onUnbind() {
  }

}
