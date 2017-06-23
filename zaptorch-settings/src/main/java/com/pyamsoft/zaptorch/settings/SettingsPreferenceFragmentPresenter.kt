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
import com.pyamsoft.pydroid.presenter.SchedulerPreferencePresenter
import com.pyamsoft.zaptorch.model.ConfirmEvent
import io.reactivex.Scheduler
import timber.log.Timber

class SettingsPreferenceFragmentPresenter internal constructor(
    private val bus: EventBus<ConfirmEvent>,
    private val interactor: SettingsPreferenceFragmentInteractor,
    observeScheduler: Scheduler, subscribeScheduler: Scheduler) : SchedulerPreferencePresenter(
    observeScheduler, subscribeScheduler) {

  private var cameraApiListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

  override fun onStop() {
    super.onStop()
    unregisterCameraApiListener()
  }

  private fun registerCameraApiListener() {
    unregisterCameraApiListener()
    interactor.registerCameraApiListener(cameraApiListener)
  }

  private fun unregisterCameraApiListener() {
    interactor.unregisterCameraApiListener(cameraApiListener)
  }

  fun registerEventBus(onClearAll: () -> Unit) {
    disposeOnStop {
      bus.listen().flatMapSingle { interactor.clearAll() }
          .subscribeOn(backgroundScheduler).observeOn(foregroundScheduler)
          .subscribe({ onClearAll() }, { Timber.e(it, "onError event bus") })
    }
  }

  fun listenForCameraChanges(onApiChanged: () -> Unit) {
    if (cameraApiListener == null) {
      cameraApiListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == interactor.getCameraApiKey()) {
          Timber.d("Camera API has changed")
          onApiChanged()
        }
      }
    }
    registerCameraApiListener()
  }
}
