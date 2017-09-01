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

package com.pyamsoft.zaptorch.service.error

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.pyamsoft.pydroid.presenter.Presenter
import com.pyamsoft.zaptorch.uicode.WatchedDialog

class CameraErrorDialog : WatchedDialog() {

  override fun provideBoundPresenters(): List<Presenter<*>> = emptyList()

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(activity).setMessage(
        "ZapTorch was unable to access your devices setupCamera."
            + " Please make sure that your device has a setupCamera with Flash functionality."
            + " Please make sure no other application is using the setupCamera and try again.")
        .setPositiveButton("Okay") { _, _ -> dismiss() }
        .create()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    activity.finish()
  }
}
