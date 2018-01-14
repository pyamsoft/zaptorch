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

package com.pyamsoft.zaptorch.service

import android.content.Intent
import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.ktext.clear
import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.FINISH
import com.pyamsoft.zaptorch.model.ServiceEvent.Type.TORCH
import com.pyamsoft.zaptorch.service.VolumeServicePresenter.View
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import timber.log.Timber

class VolumeServicePresenter internal constructor(private val interactor: VolumeServiceInteractor,
        private val bus: EventBus<ServiceEvent>,
        computationScheduler: Scheduler, ioScheduler: Scheduler,
        mainThreadScheduler: Scheduler) : SchedulerPresenter<View>(
        computationScheduler, ioScheduler, mainThreadScheduler) {

    private var keyDisposable: Disposable = Disposables.empty()

    override fun onCreate() {
        super.onCreate()
        registerOnBus()
    }

    override fun onDestroy() {
        super.onDestroy()
        interactor.releaseCamera()
        keyDisposable = keyDisposable.clear()
    }

    override fun onStart() {
        super.onStart()
        setupCamera()
    }

    private fun setupCamera() {
        interactor.setupCamera(computationScheduler, mainThreadScheduler) {
            view?.onError(it)
        }
    }

    private fun toggleTorch() {
        Timber.d("Toggle torch")
        interactor.toggleTorch()
    }

    fun handleKeyEvent(action: Int, keyCode: Int) {
        keyDisposable = keyDisposable.clear()
        keyDisposable = interactor.handleKeyPress(action, keyCode)
                .subscribeOn(ioScheduler)
                .observeOn(mainThreadScheduler)
                .subscribe({ time -> Timber.d("Set back after %d delay", time) }
                        , { throwable -> Timber.e(throwable, "onError handleKeyEvent") })
    }

    private fun registerOnBus() {
        dispose {
            bus.listen()
                    .subscribeOn(ioScheduler)
                    .observeOn(mainThreadScheduler)
                    .subscribe({ (type) ->
                        when (type) {
                            TORCH -> toggleTorch()
                            FINISH -> view?.onFinishService()
                            else -> throw IllegalArgumentException(
                                    "Invalid ServiceEvent.Type: " + type)
                        }
                    }, { Timber.e(it, "onError event bus") })
        }
    }

    interface View : ServiceCallback, ErrorHandlerCallback

    interface ErrorHandlerCallback {

        fun onError(intent: Intent)
    }

    interface ServiceCallback {

        fun onFinishService()
    }
}
