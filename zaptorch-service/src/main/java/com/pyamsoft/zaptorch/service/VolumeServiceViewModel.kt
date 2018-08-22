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

package com.pyamsoft.zaptorch.service

import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.core.bus.Listener
import com.pyamsoft.pydroid.core.viewmodel.LifecycleViewModel
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.FINISH
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.TORCH
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class VolumeServiceViewModel internal constructor(
  private val errorBus: Listener<Intent>,
  private val interactor: VolumeServiceInteractor,
  private val bus: Listener<ServiceEvent>
) : LifecycleViewModel {

  fun onCameraError(
    owner: LifecycleOwner,
    func: (Intent) -> Unit
  ) {
    errorBus.listen()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnSubscribe { interactor.setupCamera() }
        .doOnDispose { interactor.releaseCamera() }
        .subscribe(func)
        .bind(owner)
  }

  fun onTorchEvent(
    owner: LifecycleOwner,
    func: () -> Unit
  ) {
    bus.listen()
        .map { it.type }
        .filter { it == TORCH }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { interactor.toggleTorch() }
        .subscribe { func() }
        .bind(owner)
  }

  fun onServiceFinishEvent(
    owner: LifecycleOwner,
    func: () -> Unit
  ) {
    bus.listen()
        .map { it.type }
        .filter { it == FINISH }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { func() }
        .bind(owner)
  }

  fun handleKeyEvent(
    owner: LifecycleOwner,
    action: Int,
    keyCode: Int
  ) {
    interactor.handleKeyPress(action, keyCode)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
        .disposeOnClear(owner)
  }

}
