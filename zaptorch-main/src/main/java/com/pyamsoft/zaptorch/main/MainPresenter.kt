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

package com.pyamsoft.zaptorch.main

import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.main.MainPresenter.View
import io.reactivex.Scheduler
import timber.log.Timber

class MainPresenter internal constructor(
        private val handleKeyPressKey: String,
        private val interactor: MainInteractor,
        computationScheduler: Scheduler, ioScheduler: Scheduler,
        mainScheduler: Scheduler) : SchedulerPresenter<View>(computationScheduler, ioScheduler,
        mainScheduler) {

    override fun onCreate() {
        super.onCreate()
        shouldHandleKeycode()
        interactor.register(handleKeyPressKey, { view?.onHandleKeyPress(it) })
    }

    override fun onDestroy() {
        super.onDestroy()
        interactor.unregister()
    }

    private fun shouldHandleKeycode() {
        dispose {
            interactor.shouldHandleKeys()
                    .subscribeOn(ioScheduler)
                    .observeOn(mainThreadScheduler)
                    .subscribe({
                        view?.onHandleKeyPress(it)
                    }, {
                        Timber.e(it, "Error while handling keycode")
                        view?.onError(it)
                    })
        }
    }

    interface View : OnHandleKeyPressChangedCallback

    interface OnHandleKeyPressChangedCallback {

        fun onHandleKeyPress(handle: Boolean)

        fun onError(throwable: Throwable)
    }
}
