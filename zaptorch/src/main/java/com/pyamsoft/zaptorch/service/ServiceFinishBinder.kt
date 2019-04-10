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

import com.pyamsoft.pydroid.arch.UiBinder
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.zaptorch.service.ServiceFinishBinder.Callback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

internal class ServiceFinishBinder internal constructor(
  private val bus: EventBus<ServiceFinishEvent>
) : UiBinder<Callback>() {

  override fun onBind() {
    bus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { callback.onServiceFinished() }
        .destroy()
  }

  override fun onUnbind() {
  }

  fun finish() {
    bus.publish(ServiceFinishEvent)
  }

  interface Callback : UiBinder.Callback {

    fun onServiceFinished()
  }

}

