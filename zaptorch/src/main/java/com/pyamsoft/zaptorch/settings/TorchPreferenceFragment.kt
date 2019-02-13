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

package com.pyamsoft.zaptorch.settings

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pyamsoft.pydroid.core.singleDisposable
import com.pyamsoft.pydroid.core.tryDispose
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.settings.AppSettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.ServiceFinishPresenterImpl
import com.pyamsoft.zaptorch.settings.SettingsPresenter.Callback
import timber.log.Timber

class TorchPreferenceFragment : AppSettingsPreferenceFragment(), Callback,
    ClearAllPresenter.Callback {

  internal lateinit var settingsView: SettingsView
  internal lateinit var presenter: SettingsPresenter
  internal lateinit var serviceFinishPresenter: ServiceFinishPresenterImpl
  internal lateinit var clearPresenter: ClearAllPresenter

  override val preferenceXmlResId: Int = R.xml.preferences

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val view = requireNotNull(super.onCreateView(inflater, container, savedInstanceState))

    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusSettingsComponent(viewLifecycleOwner, listView, preferenceScreen)
        .inject(this)

    return view
  }

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)
    settingsView.inflate(savedInstanceState)
    presenter.bind(this)
    clearPresenter.bind(this)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    settingsView.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    settingsView.teardown()
  }

  override fun onClearAll() {
    requireContext().also {
      try {
        serviceFinishPresenter.finish()
      } catch (e: NullPointerException) {
        Timber.e(e, "Expected exception when Service is NULL")
      }

      Timber.d("Clear application data")
      val activityManager = it.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      activityManager.clearApplicationUserData()
    }
  }

  override fun onShowExplanation() {
    HowToDialog()
        .show(requireActivity(), "howto")
  }

  override fun onClearAllClicked() {
    super.onClearAllClicked()
    ConfirmationDialog()
        .show(requireActivity(), "confirm")
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
