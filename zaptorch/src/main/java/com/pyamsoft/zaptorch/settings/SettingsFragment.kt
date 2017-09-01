/*
 * Copyright 2017 Peter Kenji Yamanaka
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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pyamsoft.pydroid.design.fab.HideScrollFABBehavior
import com.pyamsoft.pydroid.design.util.FABUtil
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.LoaderHelper
import com.pyamsoft.pydroid.presenter.Presenter
import com.pyamsoft.pydroid.ui.util.DialogUtil
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.databinding.FragmentMainBinding
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.uicode.WatchedFragment

class SettingsFragment : WatchedFragment() {

  internal lateinit var publisher: SettingPublisher
  private lateinit var binding: FragmentMainBinding
  private var fabTask = LoaderHelper.empty()

  override fun provideBoundPresenters(): List<Presenter<*>> = emptyList()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Injector.with(context) {
      it.inject(this)
    }
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    binding = FragmentMainBinding.inflate(inflater, container, false)
    return binding.root
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupFAB()
    displayPreferenceFragment()
  }

  private fun setupFAB() {
    FABUtil.setupFABBehavior(binding.mainSettingsFab, HideScrollFABBehavior(10))
    binding.mainSettingsFab.setOnClickListener {
      if (VolumeMonitorService.isRunning) {
        DialogUtil.guaranteeSingleDialogFragment(activity, ServiceInfoDialog(),
            "servce_info")
      } else {
        DialogUtil.guaranteeSingleDialogFragment(activity, AccessibilityRequestDialog(),
            "accessibility")
      }
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    fabTask = LoaderHelper.unload(fabTask)
    binding.unbind()
  }

  override fun onResume() {
    super.onResume()
    setActionBarUpEnabled(false)
    if (VolumeMonitorService.isRunning) {
      fabTask = LoaderHelper.unload(fabTask)
      fabTask = ImageLoader.fromResource(activity, R.drawable.ic_help_24dp)
          .into(binding.mainSettingsFab)
    } else {
      fabTask = LoaderHelper.unload(fabTask)
      fabTask = ImageLoader.fromResource(activity, R.drawable.ic_service_start_24dp)
          .into(binding.mainSettingsFab)
    }
  }

  private fun displayPreferenceFragment() {
    val fragmentManager = childFragmentManager
    if (fragmentManager.findFragmentByTag(SettingsPreferenceFragment.TAG) == null) {
      fragmentManager.beginTransaction()
          .replace(R.id.main_container, SettingsPreferenceFragment(),
              SettingsPreferenceFragment.TAG)
          .commit()
    }
  }

  companion object {

    const val TAG = "MainSettingsFragment"
  }
}
