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
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.arch.viewModelFactory
import com.pyamsoft.pydroid.ui.settings.AppSettingsPreferenceFragment
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.widget.ToolbarView
import javax.inject.Inject

class TorchPreferenceFragment : AppSettingsPreferenceFragment() {
    @JvmField
    @Inject
    internal var factory: ViewModelProvider.Factory? = null

    @JvmField
    @Inject
    internal var settingsView: SettingsView? = null

    @JvmField
    @Inject
    internal var toolbarView: ToolbarView<UnitViewState, SettingsViewEvent>? = null
    private val viewModel by viewModelFactory<SettingsViewModel> { factory }

    private var stateSaver: StateSaver? = null

    override val preferenceXmlResId: Int = R.xml.preferences

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
            .plusSettingsComponent()
            .create(requireToolbarActivity(), listView, preferenceScreen)
            .inject(this)

        stateSaver = createComponent(
            savedInstanceState, viewLifecycleOwner,
            viewModel,
            requireNotNull(settingsView),
            requireNotNull(toolbarView)
        ) {
            // TODO(Peter): Handle future controller events
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stateSaver?.saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stateSaver = null
        settingsView = null
        toolbarView = null
        factory = null
    }

    companion object {

        const val TAG = "TorchPreferenceFragment"
    }
}
