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

package com.pyamsoft.zaptorch.settings

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.ui.arch.PrefUiView
import com.pyamsoft.pydroid.ui.util.Snackbreak
import com.pyamsoft.pydroid.ui.widget.scroll.HideOnScrollListener
import com.pyamsoft.zaptorch.R

internal class SettingsView internal constructor(
  private val owner: LifecycleOwner,
  private val recyclerView: RecyclerView,
  parent: PreferenceScreen,
  callback: SettingsView.Callback
) : PrefUiView<SettingsView.Callback>(parent, callback) {

  private val explain by lazyPref<Preference>(R.string.zaptorch_explain_key)

  private var scrollListener: RecyclerView.OnScrollListener? = null

  override fun onInflated(
    preferenceScreen: PreferenceScreen,
    savedInstanceState: Bundle?
  ) {
    explain.setOnPreferenceClickListener {
      callback.onExplainClicked()
      return@setOnPreferenceClickListener true
    }

    val listener = HideOnScrollListener.create(true) {
      callback.onSignificantScrollEvent(it)
    }
    recyclerView.addOnScrollListener(listener)
    scrollListener = listener
  }

  override fun onTeardown() {
    explain.onPreferenceClickListener = null

    scrollListener?.also { recyclerView.removeOnScrollListener(it) }
    scrollListener = null
  }

  fun showError(message: String) {
    Snackbreak.bindTo(owner)
        .short(recyclerView, message)
        .show()
  }

  fun clearError() {
    Snackbreak.bindTo(owner)
        .dismiss()
  }

  interface Callback {

    fun onExplainClicked()

    fun onSignificantScrollEvent(visible: Boolean)

  }
}

