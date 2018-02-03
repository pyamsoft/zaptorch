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

import android.content.Context
import android.content.Intent
import android.support.annotation.CheckResult
import com.pyamsoft.pydroid.ktext.clear
import com.pyamsoft.pydroid.ktext.enforceComputation
import com.pyamsoft.pydroid.ktext.enforceMainThread
import com.pyamsoft.zaptorch.api.CameraInterface
import com.pyamsoft.zaptorch.api.CameraInterface.OnStateChangedCallback
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import timber.log.Timber

internal abstract class CameraCommon protected constructor(
  context: Context,
  private val interactor: VolumeServiceInteractor,
  private val computationScheduler: Scheduler,
  private val mainScheduler: Scheduler
) :
    CameraInterface {

  @get:CheckResult
  val appContext: Context = context.applicationContext
  private val errorExplain = Intent()
  private val permissionExplain = Intent()
  private var callback: OnStateChangedCallback? = null

  private var errorDisposable: Disposable = Disposables.empty()

  init {
    mainScheduler.enforceMainThread()
    computationScheduler.enforceComputation()

    errorExplain.putExtra(
        CameraInterface.DIALOG_WHICH, CameraInterface.TYPE_ERROR
    )
    errorExplain.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    permissionExplain.putExtra(
        CameraInterface.DIALOG_WHICH, CameraInterface.TYPE_PERMISSION
    )
    permissionExplain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
  }

  override fun startErrorExplanationActivity() {
    errorDisposable = errorDisposable.clear()
    errorDisposable = interactor.shouldShowErrorDialog()
        .subscribeOn(computationScheduler)
        .observeOn(mainScheduler)
        .subscribe({
          if (it) {
            notifyCallbackOnError(errorExplain)
          }
        }, { Timber.e(it, "onError startErrorExplanationActivity") })
  }

  override fun startPermissionExplanationActivity() {
    notifyCallbackOnError(permissionExplain)
  }

  override fun setOnStateChangedCallback(callback: OnStateChangedCallback?) {
    this.callback = callback
  }

  override fun notifyCallbackOnOpened() {
    val obj = callback
    if (obj != null) {
      Timber.d("Notify callback: opened")
      obj.onOpened()
    }
  }

  override fun notifyCallbackOnClosed() {
    val obj = callback
    if (obj != null) {
      Timber.d("Notify callback: closed")
      obj.onClosed()
    }
  }

  private fun notifyCallbackOnError(errorIntent: Intent) {
    val obj = callback
    if (obj != null) {
      Timber.w("Notify callback: error")
      obj.onError(errorIntent)
    }
  }

  // Called from VolumeServiceInteractorImpl
  override fun destroy() {
    errorDisposable = errorDisposable.clear()
    release()
  }
}
