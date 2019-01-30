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

package com.pyamsoft.zaptorch.service

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.ui.arch.Worker
import com.pyamsoft.zaptorch.service.ServiceStateEvent.TorchToggle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

internal class TorchWorker internal constructor(
  bus: EventBus<ServiceStateEvent>
) : Worker<ServiceStateEvent>(bus) {

  @CheckResult
  fun onRequest(func: () -> Unit): Disposable {
    return listen()
        .ofType(TorchToggle::class.java)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { func() }
  }

  fun publish() {
    publish(TorchToggle)
  }

}
