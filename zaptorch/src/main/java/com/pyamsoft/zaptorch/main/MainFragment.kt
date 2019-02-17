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
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.ServiceStatePresenter
import com.pyamsoft.zaptorch.settings.SettingsFragment

class MainFragment : Fragment(), ServiceStatePresenter.Callback, MainFragmentPresenter.Callback {

  private lateinit var layoutRoot: CoordinatorLayout

  internal lateinit var presenter: MainFragmentPresenter
  internal lateinit var serviceStatePresenter: ServiceStatePresenter
  internal lateinit var frameView: MainFrameView
  internal lateinit var actionView: MainActionView

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val root = inflater.inflate(R.layout.layout_coordinator, container, false)
    layoutRoot = root.findViewById(R.id.layout_coordinator)

    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusMainFragmentComponent(layoutRoot)
        .inject(this)

    return root
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    frameView.inflate(savedInstanceState)
    actionView.inflate(savedInstanceState)

    displayPreferenceFragment()

    presenter.bind(viewLifecycleOwner, this)
    serviceStatePresenter.bind(viewLifecycleOwner, this)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    frameView.teardown()
    actionView.teardown()
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

  override fun onResume() {
    super.onResume()
    requireToolbarActivity().withToolbar {
      it.setTitle(R.string.app_name)
      it.setUpEnabled(false)
    }
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
