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
import com.pyamsoft.pydroid.ui.arch.ViewEvent.EMPTY
import com.pyamsoft.pydroid.ui.arch.destroy
import com.pyamsoft.zaptorch.main.MainStateEvent.PrivacyPolicyLinkFailed
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal class MainFrameUiComponent internal constructor(
  private val controllerBus: Listener<MainStateEvent>,
  view: MainFrameView,
  owner: LifecycleOwner
) : BaseUiComponent<EMPTY, MainFrameView>(view, owner) {

  override fun onCreate(savedInstanceState: Bundle?) {
    controllerBus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          return@subscribe when (it) {
            is PrivacyPolicyLinkFailed -> view.showPrivacyPolicyError(it.error)
          }
        }
        .destroy(owner)
  }
}
