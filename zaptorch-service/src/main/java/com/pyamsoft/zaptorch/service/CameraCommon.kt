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

package com.pyamsoft.zaptorch.service

import android.content.Context
import android.content.Intent
import android.support.annotation.CheckResult
import com.pyamsoft.pydroid.presenter.SchedulerPresenter
import io.reactivex.Scheduler
import timber.log.Timber

internal abstract class CameraCommon(context: Context,
    private val interactor: VolumeServiceInteractor,
    obsScheduler: Scheduler, subScheduler: Scheduler) : SchedulerPresenter<Unit>(obsScheduler,
    subScheduler), CameraInterface {

  @get:CheckResult val appContext: Context = context.applicationContext
  val errorExplain = Intent()
  val permissionExplain = Intent()
  private var callback: OnStateChangedCallback? = null

  init {
    errorExplain.putExtra(CameraInterface.DIALOG_WHICH, CameraInterface.TYPE_ERROR)
    errorExplain.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    permissionExplain.putExtra(CameraInterface.DIALOG_WHICH, CameraInterface.TYPE_PERMISSION)
    permissionExplain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
  }

  fun startErrorExplanationActivity() {
    disposeOnStop {
      interactor.shouldShowErrorDialog()
          .subscribeOn(backgroundScheduler)
          .observeOn(foregroundScheduler)
          .subscribe({
            if (it) {
              notifyCallbackOnError(errorExplain)
            }
          }, { Timber.e(it, "onError startErrorExplanationActivity") })
    }
  }

  fun startPermissionExplanationActivity() {
    notifyCallbackOnError(permissionExplain)
  }

  fun setOnStateChangedCallback(callback: OnStateChangedCallback?) {
    this.callback = callback
  }

  fun notifyCallbackOnOpened() {
    if (callback != null) {
      Timber.d("Notify callback: opened")
      callback!!.onOpened()
    }
  }

  fun notifyCallbackOnClosed() {
    if (callback != null) {
      Timber.d("Notify callback: closed")
      callback!!.onClosed()
    }
  }

  fun notifyCallbackOnError(errorIntent: Intent) {
    if (callback != null) {
      Timber.w("Notify callback: error")
      callback!!.onError(errorIntent)
    }
  }

  abstract fun release()

  abstract fun toggleTorch()

  internal interface OnStateChangedCallback {

    fun onOpened()

    fun onClosed()

    fun onError(errorIntent: Intent)
  }
}
