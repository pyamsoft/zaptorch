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

import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.pydroid.ui.arch.PrefUiView
import com.pyamsoft.pydroid.ui.widget.scroll.HideOnScrollListener
import com.pyamsoft.zaptorch.settings.SettingsViewEvent.SignificantScroll
import javax.inject.Inject

internal class SettingsView @Inject internal constructor(
    recyclerView: RecyclerView,
    parent: PreferenceScreen
) : PrefUiView<UnitViewState, SettingsViewEvent>(parent) {

    private var scrollListener: RecyclerView.OnScrollListener? = null

    init {
        doOnInflate {
            val listener = HideOnScrollListener.create(true) {
                publish(SignificantScroll(it))
            }
            recyclerView.addOnScrollListener(listener)
            scrollListener = listener
        }

        doOnTeardown {
            scrollListener?.also { recyclerView.removeOnScrollListener(it) }
            scrollListener = null
        }
    }

    override fun onRender(state: UnitViewState) {
    }
}
