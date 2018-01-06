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

import android.support.v4.app.DialogFragment
import com.pyamsoft.pydroid.ui.app.activity.ActivityBase
import com.pyamsoft.pydroid.ui.util.DialogUtil
import com.pyamsoft.zaptorch.api.CameraInterface.Companion.DIALOG_WHICH
import com.pyamsoft.zaptorch.api.CameraInterface.Companion.TYPE_ERROR
import com.pyamsoft.zaptorch.api.CameraInterface.Companion.TYPE_NONE
import com.pyamsoft.zaptorch.api.CameraInterface.Companion.TYPE_PERMISSION

class CameraErrorExplanation : ActivityBase() {

    override fun onPostResume() {
        super.onPostResume()
        val type = intent.getIntExtra(DIALOG_WHICH, TYPE_NONE)
        val fragment: DialogFragment? = when (type) {
            TYPE_PERMISSION -> PermissionErrorDialog()
            TYPE_ERROR -> CameraErrorDialog()
            else -> null
        }
        if (fragment != null) {
            DialogUtil.guaranteeSingleDialogFragment(this, fragment, "error")
        }
    }
}
