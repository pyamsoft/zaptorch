/*
 * Copyright 2020 Peter Kenji Yamanaka
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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.R

class CameraErrorExplanation : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_ZapTorch_Error)
    super.onCreate(savedInstanceState)
  }

  override fun onPostResume() {
    super.onPostResume()
    CameraErrorDialog().show(this, "camera_error")
  }

  companion object {

    @JvmStatic
    fun showError(context: Context) {
      val appContext = context.applicationContext
      val intent =
          Intent(appContext, CameraErrorExplanation::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
          }
      appContext.startActivity(intent)
    }
  }
}
