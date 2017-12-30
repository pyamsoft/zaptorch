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

import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.model.ConfirmEvent
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragmentPresenter.View
import io.reactivex.Scheduler
import timber.log.Timber

class SettingsPreferenceFragmentPresenter internal constructor(
        private val bus: EventBus<ConfirmEvent>,
        private val interactor: SettingsPreferenceFragmentInteractor,
        computationScheduler: Scheduler,
        ioScheduler: Scheduler,
        mainThreadScheduler: Scheduler) : SchedulerPresenter<View>(
        computationScheduler, ioScheduler, mainThreadScheduler) {

    override fun onCreate() {
        super.onCreate()
        registerEventBus()
    }

    private fun registerEventBus() {
        dispose {
            bus.listen().flatMapSingle { interactor.clearAll() }
                    .subscribeOn(ioScheduler).observeOn(mainThreadScheduler)
                    .subscribe({ view?.onClearAll() }, { Timber.e(it, "onError event bus") })
        }
    }

    interface View : ClearCallback

    interface ClearCallback {

        fun onClearAll()
    }
}
