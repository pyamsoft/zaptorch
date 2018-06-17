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
import android.content.Context
import android.os.Bundle
import android.view.View
import com.pyamsoft.pydroid.ui.app.fragment.SettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.service.ServicePublisher
import timber.log.Timber

class TorchPreferenceFragment : SettingsPreferenceFragment(),
    SettingsPreferenceFragmentPresenter.View {

  internal lateinit var servicePublisher: ServicePublisher
  internal lateinit var presenter: SettingsPreferenceFragmentPresenter

  override val preferenceXmlResId: Int = R.xml.preferences

  override val rootViewContainer: Int = R.id.main_viewport

  override val applicationName: String
    get() = getString(R.string.app_name)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusSettingsComponent()
        .inject(this)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    val zapTorchExplain = findPreference(getString(R.string.zaptorch_explain_key))
    zapTorchExplain.setOnPreferenceClickListener {
      HowToDialog().show(requireActivity(), "howto")
      return@setOnPreferenceClickListener true
    }

    presenter.bind(viewLifecycleOwner, this)
  }

  override fun onClearAll() {
    Timber.d("received completed clearAll event. Kill Process")
    try {
      servicePublisher.publish(ServiceEvent(ServiceEvent.Type.FINISH))
    } catch (e: IllegalStateException) {
      Timber.e(e, "Expected exception when Service is NULL")
    }

    val activityManager =
      requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.clearApplicationUserData()
  }

  override fun onClearAllClicked() {
    ConfirmationDialog().show(requireActivity(), "confirm_dialog")
  }

  override fun onDestroy() {
    super.onDestroy()
    ZapTorch.getRefWatcher(this)
        .watch(this)
  }

  override fun onResume() {
    super.onResume()
    toolbarActivity.withToolbar {
      it.setTitle(R.string.app_name)
      it.setUpEnabled(false)
    }
  }

  companion object {

    const val TAG = "TorchPreferenceFragment"
  }
}
