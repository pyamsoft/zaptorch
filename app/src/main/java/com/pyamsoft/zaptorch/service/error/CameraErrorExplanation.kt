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
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.pyamsoft.pydroid.ui.app.ActivityBase
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.core.CameraInterface.CameraError
import com.pyamsoft.zaptorch.core.CameraInterface.Companion.DIALOG_WHICH
import com.pyamsoft.zaptorch.core.CameraInterface.Companion.TYPE_ERROR
import com.pyamsoft.zaptorch.core.CameraInterface.Companion.TYPE_NONE
import timber.log.Timber

class CameraErrorExplanation : ActivityBase() {

    override val fragmentContainerId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ZapTorch_Error)
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

    companion object {

        @JvmStatic
        fun renderError(context: Context, cameraError: CameraError) {
            cameraError.exception?.let { e -> Timber.e(e, "Received camera error") }
            context.applicationContext.also { ctx ->
                val intent = cameraError.intent
                intent.setClass(ctx, CameraErrorExplanation::class.java)
                ctx.startActivity(intent)
            }
        }
    }
}
