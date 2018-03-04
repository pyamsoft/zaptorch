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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pyamsoft.pydroid.design.fab.HideScrollFABBehavior
import com.pyamsoft.pydroid.design.util.withBehavior
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.databinding.FragmentMainBinding
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.settings.AccessibilityRequestDialog
import com.pyamsoft.zaptorch.settings.ServiceInfoDialog
import com.pyamsoft.zaptorch.settings.SettingPublisher
import com.pyamsoft.zaptorch.settings.SettingsFragment
import com.pyamsoft.zaptorch.uicode.WatchedFragment

class MainFragment : WatchedFragment() {

  internal lateinit var imageLoader: ImageLoader
  internal lateinit var publisher: SettingPublisher
  private lateinit var binding: FragmentMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    binding = FragmentMainBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    setupFAB()
    displayPreferenceFragment()
  }

  private fun setupFAB() {
    binding.apply {
      binding.mainSettingsFab.withBehavior(HideScrollFABBehavior(10))
      mainSettingsFab.setOnDebouncedClickListener {
        if (VolumeMonitorService.isRunning) {
          ServiceInfoDialog().show(activity, "service_info")
        } else {
          AccessibilityRequestDialog().show(activity, "accessibility")
        }
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    binding.unbind()
  }

  override fun onResume() {
    super.onResume()
    toolbarActivity.withToolbar {
      it.setTitle(R.string.app_name)
      it.setUpEnabled(false)
    }

    imageLoader.apply {
      if (VolumeMonitorService.isRunning) {
        fromResource(R.drawable.ic_help_24dp).into(binding.mainSettingsFab)
            .bind(viewLifecycle)
      } else {
        fromResource(R.drawable.ic_service_start_24dp).into(binding.mainSettingsFab)
            .bind(viewLifecycle)
      }
    }
  }

  private fun displayPreferenceFragment() {
    val fragmentManager = childFragmentManager
    if (fragmentManager.findFragmentByTag(SettingsFragment.TAG) == null) {
      fragmentManager.beginTransaction()
          .add(R.id.main_container, SettingsFragment(), SettingsFragment.TAG)
          .commit()
    }
  }

  companion object {

    const val TAG = "MainFragment"
  }
}
