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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.arch.factory
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.main.MainControllerEvent.ServiceAction
import com.pyamsoft.zaptorch.settings.SettingsFragment
import com.pyamsoft.zaptorch.widget.ToolbarView
import javax.inject.Inject

class MainFragment : Fragment() {

    @JvmField
    @Inject
    internal var factory: ViewModelProvider.Factory? = null
    @JvmField
    @Inject
    internal var actionView: MainActionView? = null
    @JvmField
    @Inject
    internal var toolbarView: ToolbarView<MainViewState, MainViewEvent>? = null
    private val viewModel by factory<MainViewModel> { factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_coordinator, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val layoutRoot = view.findViewById<CoordinatorLayout>(R.id.layout_coordinator)
        Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
            .plusMainFragmentComponent()
            .create(viewLifecycleOwner, requireToolbarActivity(), layoutRoot)
            .inject(this)

        createComponent(
            savedInstanceState, viewLifecycleOwner,
            viewModel,
            requireNotNull(actionView),
            requireNotNull(toolbarView)
        ) {
            return@createComponent when (it) {
                is ServiceAction -> {
                    if (it.isServiceRunning) {
                        showInfoDialog()
                    } else {
                        showUsageAccessRequestDialog()
                    }
                }
            }
        }

        displayPreferenceFragment()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        actionView = null
        toolbarView = null
        factory = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        toolbarView?.saveState(outState)
        actionView?.saveState(outState)
    }

    private fun showUsageAccessRequestDialog() {
        AccessibilityRequestDialog().show(requireActivity(), "accessibility")
    }

    private fun showInfoDialog() {
        HowToDialog().show(requireActivity(), "how_to")
    }

    private fun displayPreferenceFragment() {
        val fragmentManager = childFragmentManager
        if (fragmentManager.findFragmentByTag(SettingsFragment.TAG) == null) {
            fragmentManager.commit(viewLifecycleOwner) {
                add(requireNotNull(actionView).id(), SettingsFragment(), SettingsFragment.TAG)
            }
        }
    }

    companion object {

        const val TAG = "MainFragment"
    }
}
