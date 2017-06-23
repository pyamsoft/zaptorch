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

package com.pyamsoft.zaptorch.settings

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.pyamsoft.zaptorch.uicode.WatchedDialog

class HowToDialog : WatchedDialog() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(activity).setMessage(
        "Just double press the volume down key to turn the flashlight on and off")
        .setTitle("How to Use")
        .setNeutralButton("Got It") { _, _ -> dismiss() }
        .create()
  }
}
