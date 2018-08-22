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

package com.pyamsoft.zaptorch.service.error

import android.Manifest
import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.pyamsoft.pydroid.ui.app.fragment.ToolbarDialog

class PermissionErrorDialog : ToolbarDialog() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireActivity())
        .setMessage("""ZapTorch was unable to access your devices Camera
              |Please grant ZapTorch the 'Camera' permission.""".trimMargin()
        )
        .setPositiveButton("Okay") { _, _ ->
          ActivityCompat.requestPermissions(
              requireActivity(), arrayOf(Manifest.permission.CAMERA), 5000
          )
          dismiss()
        }
        .setNegativeButton("No") { _, _ -> dismiss() }
        .create()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    activity?.finish()
  }
}
