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

package com.pyamsoft.zaptorch.settings

import android.app.ActivityManager
import android.os.Bundle
import android.view.View
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.core.bus.Publisher
import com.pyamsoft.pydroid.ui.app.fragment.SettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.app.fragment.requireToolbarActivity
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.popHide
import com.pyamsoft.pydroid.ui.util.popShow
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.pydroid.ui.widget.HideOnScrollListener
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.main.MainFragment
import com.pyamsoft.zaptorch.model.ServiceEvent
import timber.log.Timber

class TorchPreferenceFragment : SettingsPreferenceFragment() {

  private var hideScrollListener: HideOnScrollListener? = null
  internal lateinit var publisher: Publisher<ServiceEvent>
  internal lateinit var viewModel: SettingsViewModel
  internal lateinit var theming: Theming

  override val preferenceXmlResId: Int = R.xml.preferences

  override val rootViewContainer: Int = R.id.main_viewport

  override val applicationName: String
    get() = getString(R.string.app_name)

  override val bugreportUrl: String = "https://github.com/pyamsoft/zaptorch/issues"

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusSettingsComponent(viewLifecycleOwner)
        .inject(this)

    setupExplain()
    setupDarkTheme()

    addScrollListener()
    viewModel.onClearAllEvent { onClearAll() }
  }

  private fun setupDarkTheme() {
    val theme = findPreference(getString(R.string.dark_mode_key))
    theme.setOnPreferenceChangeListener { _, newValue ->
      if (newValue is Boolean) {
        theming.setDarkTheme(newValue)
        requireActivity().recreate()
        return@setOnPreferenceChangeListener true
      }
      return@setOnPreferenceChangeListener false
    }
  }

  private fun setupExplain() {
    val zapTorchExplain = findPreference(getString(R.string.zaptorch_explain_key))
    zapTorchExplain.setOnPreferenceClickListener {
      HowToDialog().show(requireActivity(), "howto")
      return@setOnPreferenceClickListener true
    }
  }

  private fun addScrollListener() {
    val fragment = requireActivity().supportFragmentManager.findFragmentByTag(MainFragment.TAG)
    if (fragment is MainFragment) {
      val fab = fragment.getFloatingActionButton()
      val listener = HideOnScrollListener.withView(fab) {
        if (it) {
          fab.popShow()
        } else {
          fab.popHide()
        }
      }

      listView.addOnScrollListener(listener)
      hideScrollListener = listener
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    hideScrollListener?.also { listView?.removeOnScrollListener(it) }
    hideScrollListener = null
  }

  private fun onClearAll() {
    Timber.d("received completed clearAll event. Kill Process")
    try {
      publisher.publish(ServiceEvent(ServiceEvent.Type.FINISH))
    } catch (e: IllegalStateException) {
      Timber.e(e, "Expected exception when Service is NULL")
    }

    requireNotNull(requireContext().getSystemService<ActivityManager>()).clearApplicationUserData()
  }

  override fun onClearAllClicked() {
    ConfirmationDialog().show(requireActivity(), "confirm_dialog")
  }

  override fun onResume() {
    super.onResume()
    requireToolbarActivity().withToolbar {
      it.setTitle(R.string.app_name)
      it.setUpEnabled(false)
    }
  }

  companion object {

    const val TAG = "TorchPreferenceFragment"
  }
}
