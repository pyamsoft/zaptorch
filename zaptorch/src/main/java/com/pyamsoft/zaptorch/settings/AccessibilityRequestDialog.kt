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
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.pyamsoft.pydroid.ui.app.fragment.ToolbarDialog
import com.pyamsoft.zaptorch.R

class AccessibilityRequestDialog : ToolbarDialog() {

  private val accessibilityServiceIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireActivity())
        .setTitle("Enable ZapTorch AccessibilityService")
        .setMessage(R.string.explain_accessibility_service)
        .setPositiveButton("Let's Go") { _, _ ->
          requireActivity().startActivity(accessibilityServiceIntent)
          dismiss()
        }
        .setNegativeButton("No Thanks") { _, _ -> dismiss() }
        .create()
  }
}
