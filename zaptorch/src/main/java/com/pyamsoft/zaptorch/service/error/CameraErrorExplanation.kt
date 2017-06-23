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

import android.support.v4.app.DialogFragment
import com.pyamsoft.pydroid.ui.app.activity.ActivityBase
import com.pyamsoft.pydroid.ui.util.DialogUtil
import com.pyamsoft.zaptorch.service.CameraInterface.Companion.DIALOG_WHICH
import com.pyamsoft.zaptorch.service.CameraInterface.Companion.TYPE_ERROR
import com.pyamsoft.zaptorch.service.CameraInterface.Companion.TYPE_NONE
import com.pyamsoft.zaptorch.service.CameraInterface.Companion.TYPE_PERMISSION

class CameraErrorExplanation : ActivityBase() {

  override fun onPostResume() {
    super.onPostResume()
    val type = intent.getIntExtra(DIALOG_WHICH, TYPE_NONE)
    val fragment: DialogFragment?
    when (type) {
      TYPE_PERMISSION -> fragment = PermissionErrorDialog()
      TYPE_ERROR -> fragment = CameraErrorDialog()
      else -> fragment = null
    }
    if (fragment != null) {
      DialogUtil.guaranteeSingleDialogFragment(this, fragment, "error")
    }
  }
}
