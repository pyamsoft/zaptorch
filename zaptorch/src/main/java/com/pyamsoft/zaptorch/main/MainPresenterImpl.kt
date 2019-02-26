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

import android.view.MenuItem
import com.pyamsoft.pydroid.core.bus.RxBus
import com.pyamsoft.pydroid.arch.BasePresenter
import com.pyamsoft.pydroid.arch.destroy
import com.pyamsoft.zaptorch.api.MainInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal class MainPresenterImpl internal constructor(
  private val interactor: MainInteractor
) : BasePresenter<Unit, MainPresenter.Callback>(RxBus.empty()),
    MainPresenter,
    MainToolbarView.Callback {

  override fun onToolbarMenuClicked(item: MenuItem) {
    callback.onMenuItemSelected(item)
  }

  override fun onToolbarNavClicked() {
    callback.onToolbarNavEvent()
  }

  override fun onBind() {
    interactor.onHandleKeyPressChanged()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { callback.onHandleKeyPressChanged(it) }
        .destroy(owner)
  }

  override fun onUnbind() {
  }

}
