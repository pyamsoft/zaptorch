/*
 * Copyright (C) 2018 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.zaptorch.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CheckResult
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pyamsoft.pydroid.core.bus.Publisher
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.app.fragment.ToolbarFragment
import com.pyamsoft.pydroid.ui.app.fragment.requireToolbarActivity
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.databinding.FragmentMainBinding
import com.pyamsoft.zaptorch.model.ConfirmEvent
import com.pyamsoft.zaptorch.service.VolumeServiceViewModel
import com.pyamsoft.zaptorch.settings.AccessibilityRequestDialog
import com.pyamsoft.zaptorch.settings.ServiceInfoDialog
import com.pyamsoft.zaptorch.settings.SettingsFragment

class MainFragment : ToolbarFragment() {

  private lateinit var binding: FragmentMainBinding
  internal lateinit var imageLoader: ImageLoader
  internal lateinit var publisher: Publisher<ConfirmEvent>
  internal lateinit var serviceViewModel: VolumeServiceViewModel

  @CheckResult
  internal fun getFloatingActionButton(): FloatingActionButton {
    return binding.mainSettingsFab
  }

  @SuppressLint("WrongConstant")
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    Injector.obtain<MainComponent>(requireActivity())
        .inject(this)
    binding = FragmentMainBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    displayPreferenceFragment()

    serviceViewModel.onServiceStateChanged {
      setupFAB(it)
    }
  }

  private fun setupFAB(running: Boolean) {
    binding.apply {
      mainSettingsFab.setOnDebouncedClickListener {
        if (running) {
          ServiceInfoDialog().show(requireActivity(), "service_info")
        } else {
          AccessibilityRequestDialog().show(requireActivity(), "accessibility")
        }
      }
    }

    imageLoader.apply {
      if (running) {
        load(R.drawable.ic_help_24dp).into(binding.mainSettingsFab)
            .bind(viewLifecycleOwner)
      } else {
        load(R.drawable.ic_service_start_24dp).into(binding.mainSettingsFab)
            .bind(viewLifecycleOwner)
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    binding.unbind()
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
