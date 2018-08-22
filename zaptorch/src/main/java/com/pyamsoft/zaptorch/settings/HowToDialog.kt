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
import androidx.appcompat.app.AlertDialog
import com.pyamsoft.pydroid.ui.app.fragment.ToolbarDialog

class HowToDialog : ToolbarDialog() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(requireActivity())
        .setMessage("Just double press the volume down key to turn the flashlight on and off")
        .setTitle("How to Use")
        .setNeutralButton("Got It") { _, _ -> dismiss() }
        .create()
  }
}
