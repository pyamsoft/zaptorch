/*
 * Copyright 2019 Peter Kenji Yamanaka
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
 *
 */

package com.pyamsoft.zaptorch.api

import android.content.Intent

interface CameraInterface {

  fun startErrorExplanationActivity()

  fun startPermissionExplanationActivity()

  fun setOnStateChangedCallback(callback: OnStateChangedCallback?)

  fun notifyCallbackOnOpened()

  fun notifyCallbackOnClosed()

  // Called from VolumeServiceInteractorImpl
  fun destroy()

  fun release()

  fun toggleTorch()

  interface OnStateChangedCallback {

    fun onOpened()

    fun onClosed()

    fun onError(errorIntent: Intent)
  }

  companion object {

    const val TYPE_NONE = -1
    const val TYPE_PERMISSION = 0
    const val TYPE_ERROR = 1
    const val DIALOG_WHICH = "dialog"
  }
}
