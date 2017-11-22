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

package com.pyamsoft.zaptorch.settings

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import com.pyamsoft.pydroid.presenter.Presenter
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.uicode.WatchedDialog

class AccessibilityRequestDialog : WatchedDialog() {

    override fun provideBoundPresenters(): List<Presenter<*>> = emptyList()

    private val accessibilityServiceIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity!!.let {
            return AlertDialog.Builder(it).setTitle("Enable ZapTorch AccessibilityService")
                    .setMessage(R.string.explain_accessibility_service)
                    .setPositiveButton("Let's Go") { _, _ ->
                        it.startActivity(accessibilityServiceIntent)
                        dismiss()
                    }
                    .setNegativeButton("No Thanks") { _, _ -> dismiss() }
                    .create()
        }
    }
}
