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

package com.pyamsoft.zaptorch.settings

import android.content.Context
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.preference.PreferenceViewHolder
import com.pyamsoft.pydroid.ui.preference.PreferenceCompat
import com.pyamsoft.pydroid.util.doOnApplyWindowInsets
import com.pyamsoft.zaptorch.R

internal class PreferenceBottomSpace internal constructor(context: Context) :
    PreferenceCompat(context), LifecycleOwner {

  private val registry by lazy(LazyThreadSafetyMode.NONE) { LifecycleRegistry(this) }

  init {
    layoutResource = R.layout.preference_spacer

    registry.currentState = Lifecycle.State.RESUMED
  }

  override fun getLifecycle(): Lifecycle {
    return registry
  }

  override fun onBindViewHolder(holder: PreferenceViewHolder) {
    super.onBindViewHolder(holder)
    holder.itemView.doOnApplyWindowInsets(this) { v, insets, _ ->
      v.updateLayoutParams<ViewGroup.MarginLayoutParams> { height = insets.systemWindowInsetBottom }
    }
  }

  override fun onDetached() {
    super.onDetached()
    registry.currentState = Lifecycle.State.DESTROYED
  }
}
