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
import com.pyamsoft.pydroid.core.bus.Publisher
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.pydroid.ui.app.fragment.ToolbarFragment
import com.pyamsoft.pydroid.ui.app.fragment.requireToolbarActivity
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.model.ConfirmEvent
import com.pyamsoft.zaptorch.service.VolumeServiceViewModel
import com.pyamsoft.zaptorch.settings.AccessibilityRequestDialog
import com.pyamsoft.zaptorch.settings.ServiceInfoDialog
import com.pyamsoft.zaptorch.settings.SettingsFragment
import timber.log.Timber

class MainFragment : ToolbarFragment() {

  internal lateinit var mainView: MainFragmentView
  internal lateinit var publisher: Publisher<ConfirmEvent>
  internal lateinit var serviceViewModel: VolumeServiceViewModel
  internal lateinit var mainViewModel: MainFragmentViewModel

  private var serviceStateDisposable by singleDisposable()
  private var fabScrollRequestDisposable by singleDisposable()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusMainFragmentComponent(viewLifecycleOwner, inflater, container)
        .inject(this)

    mainView.create()
    return mainView.root()
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    displayPreferenceFragment()

    serviceStateDisposable = serviceViewModel.onServiceStateChanged { running: Boolean ->
      mainView.setFabFromServiceState(running) {
        if (it) {
          ServiceInfoDialog().show(requireActivity(), "service_info")
        } else {
          AccessibilityRequestDialog().show(requireActivity(), "accessibility_request")
        }
      }
    }

    fabScrollRequestDisposable = mainViewModel.onFabScrollListenerCreateRequest { tag: String ->
      mainView.createFabScrollListener { listener ->
        mainViewModel.publishScrollListener(tag, listener)
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    serviceStateDisposable.tryDispose()
    fabScrollRequestDisposable.tryDispose()
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
          .add(R.id.main_container, SettingsFragment(), SettingsFragment.TAG)
          .commit(viewLifecycleOwner)
    }
  }

  companion object {

    const val TAG = "MainFragment"
  }
}
