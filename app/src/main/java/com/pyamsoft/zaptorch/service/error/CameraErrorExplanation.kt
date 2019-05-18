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

package com.pyamsoft.zaptorch.service.error

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.app.ActivityBase
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.api.CameraInterface.Companion.DIALOG_WHICH
import com.pyamsoft.zaptorch.api.CameraInterface.Companion.TYPE_ERROR
import com.pyamsoft.zaptorch.api.CameraInterface.Companion.TYPE_NONE

class CameraErrorExplanation : ActivityBase() {

  override val fragmentContainerId: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    if (Injector.obtain<Theming>(applicationContext).isDarkTheme()) {
      setTheme(R.style.Theme_ZapTorch_Dark)
    } else {
      setTheme(R.style.Theme_ZapTorch_Light)
    }
    super.onCreate(savedInstanceState)
  }

  override fun onPostResume() {
    super.onPostResume()
    val fragment: DialogFragment? = when (intent.getIntExtra(DIALOG_WHICH, TYPE_NONE)) {
      TYPE_ERROR -> CameraErrorDialog()
      else -> null
    }
    fragment?.show(this, "camera_error")
  }
}
