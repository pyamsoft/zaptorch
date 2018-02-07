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
