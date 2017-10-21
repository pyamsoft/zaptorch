/*
 *     Copyright (C) 2017 Peter Kenji Yamanaka
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.pyamsoft.zaptorch.settings

import android.content.SharedPreferences
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.model.ConfirmEvent
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragmentPresenter.View
import io.reactivex.Scheduler
import timber.log.Timber

class SettingsPreferenceFragmentPresenter internal constructor(
    private val cameraApiKey: String,
    private val bus: EventBus<ConfirmEvent>,
    private val interactor: SettingsPreferenceFragmentInteractor,
    computationScheduler: Scheduler,
    ioScheduler: Scheduler,
    mainThreadScheduler: Scheduler) : SchedulerPresenter<View>(
    computationScheduler, ioScheduler, mainThreadScheduler) {

  private var cameraApiListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

  override fun onBind(v: View) {
    super.onBind(v)
    listenForCameraChanges(v)
    registerEventBus(v)
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

  private fun registerEventBus(v: ClearCallback) {
    dispose {
      bus.listen().flatMapSingle { interactor.clearAll() }
          .subscribeOn(ioScheduler).observeOn(mainThreadScheduler)
          .subscribe({ v.onClearAll() }, { Timber.e(it, "onError event bus") })
    }
  }

  private fun listenForCameraChanges(v: ApiCallback) {
    if (cameraApiListener == null) {
      cameraApiListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == cameraApiKey) {
          Timber.d("Camera API has changed")
          v.onApiChanged()
        }
      }
    }
    registerCameraApiListener()
  }

  interface View : ApiCallback, ClearCallback

  interface ApiCallback {

    fun onApiChanged()
  }

  interface ClearCallback {

    fun onClearAll()
  }
}
