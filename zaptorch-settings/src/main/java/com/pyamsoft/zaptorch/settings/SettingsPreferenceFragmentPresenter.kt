/*
 * Copyright 2017 Peter Kenji Yamanaka
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
 */

package com.pyamsoft.zaptorch.settings

import android.content.SharedPreferences
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.model.ConfirmEvent
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragmentPresenter.Callback
import io.reactivex.Scheduler
import timber.log.Timber

class SettingsPreferenceFragmentPresenter internal constructor(
    private val cameraApiKey: String,
    private val bus: EventBus<ConfirmEvent>,
    private val interactor: SettingsPreferenceFragmentInteractor,
    computationScheduler: Scheduler,
    ioScheduler: Scheduler,
    mainThreadScheduler: Scheduler) : SchedulerPresenter<Callback>(
    computationScheduler, ioScheduler, mainThreadScheduler) {

  private var cameraApiListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

  override fun onBind(v: Callback) {
    super.onBind(v)
    listenForCameraChanges(v::onApiChanged)
    registerEventBus(v::onClearAll)
  }

  override fun onUnbind() {
    super.onUnbind()
    unregisterCameraApiListener()
  }

  private fun registerCameraApiListener() {
    unregisterCameraApiListener()
    interactor.registerCameraApiListener(cameraApiListener)
  }

  private fun unregisterCameraApiListener() {
    interactor.unregisterCameraApiListener(cameraApiListener)
  }

  private fun registerEventBus(onClearAll: () -> Unit) {
    dispose {
      bus.listen().flatMapSingle { interactor.clearAll() }
          .subscribeOn(ioScheduler).observeOn(mainThreadScheduler)
          .subscribe({ onClearAll() }, { Timber.e(it, "onError event bus") })
    }
  }

  private fun listenForCameraChanges(onApiChanged: () -> Unit) {
    if (cameraApiListener == null) {
      cameraApiListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == cameraApiKey) {
          Timber.d("Camera API has changed")
          onApiChanged()
        }
      }
    }
    registerCameraApiListener()
  }

  interface Callback {

    fun onApiChanged()

    fun onClearAll()
  }
}
