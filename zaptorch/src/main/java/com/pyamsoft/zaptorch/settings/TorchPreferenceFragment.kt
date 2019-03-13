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
import com.pyamsoft.zaptorch.widget.ToolbarView
import timber.log.Timber

class TorchPreferenceFragment : AppSettingsPreferenceFragment(),
    SettingsUiComponent.Callback {

  internal lateinit var component: SettingsUiComponent
  internal lateinit var toolbarView: ToolbarView

  override val preferenceXmlResId: Int = R.xml.preferences

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusSettingsComponent(listView, preferenceScreen)
        .inject(this)

    component.bind(viewLifecycleOwner, savedInstanceState, this)
    toolbarView.inflate(savedInstanceState)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    toolbarView.saveState(outState)
    component.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    toolbarView.teardown()
  }

  override fun onKillApplication() {
    requireContext().also {
      Timber.d("Clear application data")
      val activityManager = it.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      activityManager.clearApplicationUserData()
    }
  }

  override fun showHowTo() {
    HowToDialog().show(requireActivity(), "howto")
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
