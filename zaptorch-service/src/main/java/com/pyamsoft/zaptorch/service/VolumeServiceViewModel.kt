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
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.pydroid.core.viewmodel.BaseViewModel
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.FINISH
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.TORCH
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class VolumeServiceViewModel internal constructor(
  owner: LifecycleOwner,
  private val errorBus: Listener<Intent>,
  private val interactor: VolumeServiceInteractor,
  private val bus: Listener<ServiceEvent>
) : BaseViewModel(owner) {

  private var handleKeyDisposable by singleDisposable()

  override fun onCleared() {
    super.onCleared()
    handleKeyDisposable.tryDispose()
  }

  fun onCameraError(func: (Intent) -> Unit) {
    dispose {
      errorBus.listen()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .doOnSubscribe { interactor.setupCamera() }
          .doOnDispose { interactor.releaseCamera() }
          .subscribe(func)
    }
  }

  fun onTorchEvent(func: () -> Unit) {
    dispose {
      bus.listen()
          .map { it.type }
          .filter { it == TORCH }
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .doOnNext { interactor.toggleTorch() }
          .subscribe { func() }
    }
  }

  fun onServiceFinishEvent(func: () -> Unit) {
    dispose {
      bus.listen()
          .map { it.type }
          .filter { it == FINISH }
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe { func() }
    }
  }

  fun handleKeyEvent(
    action: Int,
    keyCode: Int
  ) {
    handleKeyDisposable = interactor.handleKeyPress(action, keyCode)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe()
  }

  fun onServiceStateChanged(func: (Boolean) -> Unit) {
    dispose {
      interactor.observeServiceState()
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(func)
    }
  }

  fun setServiceState(running: Boolean) {
    interactor.setServiceState(running)
  }

}
