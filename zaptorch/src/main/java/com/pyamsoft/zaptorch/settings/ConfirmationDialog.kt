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

package com.pyamsoft.zaptorch.settings

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.zaptorch.ZapTorchComponent
import javax.inject.Inject

class ConfirmationDialog : DialogFragment() {

  @field:Inject internal lateinit var viewModel: ClearAllViewModel

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .inject(this)

    return AlertDialog.Builder(requireActivity())
        .setMessage("Really clear all application settings?")
        .setPositiveButton("Yes") { _, _ ->
          viewModel.clearAll()
          dismiss()
        }
        .setNegativeButton("No") { _, _ -> dismiss() }
        .create()
  }
}
