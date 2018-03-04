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

package com.pyamsoft.zaptorch.settings

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.model.ConfirmEvent
import com.pyamsoft.zaptorch.uicode.WatchedDialog

class ConfirmationDialog : WatchedDialog() {

  internal lateinit var publisher: SettingPublisher

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .inject(this)
  }

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireActivity())
        .setMessage(
            "Really clear all application settings?"
        )
        .setPositiveButton("Yes") { _, _ ->
          publisher.publish(ConfirmEvent)
          dismiss()
        }
        .setNegativeButton("No") { _, _ -> dismiss() }
        .create()
  }
}
