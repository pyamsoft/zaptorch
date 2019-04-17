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

package com.pyamsoft.zaptorch.main

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.ui.util.Snackbreak
import com.pyamsoft.zaptorch.R
import javax.inject.Inject

internal class MainFrameView @Inject internal constructor(
  private val owner: LifecycleOwner,
  parent: ViewGroup
) : BaseUiView<Unit>(parent, Unit) {

  override val layoutRoot by lazyView<FrameLayout>(R.id.layout_frame)

  override val layout: Int = R.layout.layout_frame

  fun showError() {
    Snackbreak.bindTo(owner)
        .short(layoutRoot, "Unable to open browser for policy viewing")
        .show()
  }

  override fun onTeardown() {
    Snackbreak.bindTo(owner)
        .dismiss()
  }
}

