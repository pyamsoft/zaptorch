/*
 *     Copyright (C) 2017 Peter Kenji Yamanaka
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.pyamsoft.zaptorch.service.error

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.pyamsoft.zaptorch.uicode.WatchedDialog

class CameraErrorDialog : WatchedDialog() {

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    return AlertDialog.Builder(activity!!)
        .setMessage(
            "ZapTorch was unable to access your devices setupCamera."
                + " Please make sure that your device has a setupCamera with Flash functionality."
                + " Please make sure no other application is using the setupCamera and try again."
        )
        .setPositiveButton("Okay") { _, _ -> dismiss() }
        .create()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    activity?.finish()
  }
}
