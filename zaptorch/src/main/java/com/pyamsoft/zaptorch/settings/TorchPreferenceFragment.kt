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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.getSystemService
import com.pyamsoft.pydroid.core.bus.Publisher
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.pydroid.ui.app.fragment.SettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.app.fragment.requireToolbarActivity
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.model.ServiceEvent
import timber.log.Timber

class TorchPreferenceFragment : SettingsPreferenceFragment() {

  internal lateinit var publisher: Publisher<ServiceEvent>
  internal lateinit var viewModel: SettingsViewModel
  internal lateinit var settingsView: SettingsView

  private var clearDisposable by singleDisposable()
  private var scrollListenerDisposable by singleDisposable()

  override val preferenceXmlResId: Int = R.xml.preferences

  override val rootViewContainer: Int = R.id.main_viewport

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusSettingsComponent(viewLifecycleOwner, preferenceScreen, TAG)
        .inject(this)

    settingsView.create()
    return super.onCreateView(inflater, container, savedInstanceState)
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    settingsView.onExplainClicked { HowToDialog().show(requireActivity(), "howto") }

    addScrollListener()
    clearDisposable = viewModel.onClearAllEvent { onClearAll() }
  }

  private fun addScrollListener() {
    scrollListenerDisposable = viewModel.onScrollListenerCreated {
      settingsView.addScrollListener(listView, it)
    }
    viewModel.publishScrollListenerCreateRequest()
  }

  override fun onDestroyView() {
    super.onDestroyView()
    clearDisposable.tryDispose()
    scrollListenerDisposable.tryDispose()
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
