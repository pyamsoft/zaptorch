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
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.ServiceStatePresenter
import com.pyamsoft.zaptorch.settings.SettingsFragment
import com.pyamsoft.zaptorch.widget.ToolbarView

class MainFragment : Fragment(), ServiceStatePresenter.Callback, MainFragmentPresenter.Callback {

  internal lateinit var presenter: MainFragmentPresenter
  internal lateinit var serviceStatePresenter: ServiceStatePresenter

  internal lateinit var frameView: MainFrameView
  internal lateinit var actionView: MainActionView
  internal lateinit var toolbarView: ToolbarView

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
        .plusMainFragmentComponent(layoutRoot)
        .inject(this)

    frameView.inflate(savedInstanceState)
    actionView.inflate(savedInstanceState)
    toolbarView.inflate(savedInstanceState)

    displayPreferenceFragment()

    presenter.bind(viewLifecycleOwner, this)
    serviceStatePresenter.bind(viewLifecycleOwner, this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    toolbarView.teardown()
    frameView.teardown()
    actionView.teardown()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    toolbarView.saveState(outState)
    frameView.saveState(outState)
    actionView.saveState(outState)
  }

  override fun onServiceStarted() {
    actionView.setFabFromServiceState(true)
  }

  override fun onServiceStopped() {
    actionView.setFabFromServiceState(false)
  }

  override fun onServiceRunningAction() {
    ServiceInfoDialog()
        .show(requireActivity(), "service_info")
  }

  override fun onServiceStoppedAction() {
    AccessibilityRequestDialog()
        .show(requireActivity(), "accessibility")
  }

  override fun onSignificantScrollEvent(visible: Boolean) {
    actionView.toggleVisibility(visible)
  }

  private fun displayPreferenceFragment() {
    val fragmentManager = childFragmentManager
    if (fragmentManager.findFragmentByTag(SettingsFragment.TAG) == null) {
      fragmentManager.beginTransaction()
          .add(frameView.id(), SettingsFragment(), SettingsFragment.TAG)
          .commit(viewLifecycleOwner)
    }
  }

  companion object {

    const val TAG = "MainFragment"
  }
}
