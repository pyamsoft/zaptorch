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
import android.view.View
import com.pyamsoft.pydroid.ui.settings.AppSettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.service.ServiceFinishPresenter
import com.pyamsoft.zaptorch.settings.SettingsPresenter.Callback
import com.pyamsoft.zaptorch.widget.ToolbarView
import timber.log.Timber

class TorchPreferenceFragment : AppSettingsPreferenceFragment(), Callback,
    ClearAllPresenter.Callback {

  internal lateinit var toolbarView: ToolbarView
  internal lateinit var settingsView: SettingsView

  internal lateinit var presenter: SettingsPresenter
  internal lateinit var serviceFinishPresenter: ServiceFinishPresenter
  internal lateinit var clearPresenter: ClearAllPresenter

  override val preferenceXmlResId: Int = R.xml.preferences

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusSettingsComponent(listView, preferenceScreen)
        .inject(this)

    settingsView.inflate(savedInstanceState)
    toolbarView.inflate(savedInstanceState)

    presenter.bind(viewLifecycleOwner, this)
    clearPresenter.bind(viewLifecycleOwner, this)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    toolbarView.saveState(outState)
    settingsView.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    toolbarView.teardown()
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

  companion object {

    const val TAG = "TorchPreferenceFragment"
  }
}
