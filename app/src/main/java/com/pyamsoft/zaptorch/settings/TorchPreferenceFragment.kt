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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.pydroid.ui.settings.AppSettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.util.show
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.settings.SettingsControllerEvent.ClearAll
import com.pyamsoft.zaptorch.settings.SettingsControllerEvent.Explain
import com.pyamsoft.zaptorch.settings.SettingsControllerEvent.NavigationError
import com.pyamsoft.zaptorch.widget.ToolbarView
import timber.log.Timber
import javax.inject.Inject

class TorchPreferenceFragment : AppSettingsPreferenceFragment() {

  @JvmField @Inject internal var factory: ViewModelProvider.Factory? = null
  @JvmField @Inject internal var settingsView: SettingsView? = null
  @JvmField @Inject internal var toolbarView: ToolbarView<UnitViewState, SettingsViewEvent>? = null
  private var viewModel: SettingsViewModel? = null

  override val preferenceXmlResId: Int = R.xml.preferences

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    Injector.obtain<ZapTorchComponent>(requireContext().applicationContext)
        .plusSettingsComponent()
        .create(viewLifecycleOwner, requireToolbarActivity(), listView, preferenceScreen)
        .inject(this)

    ViewModelProviders.of(this, factory)
        .let { factory ->
          viewModel = factory.get(SettingsViewModel::class.java)
        }

    createComponent(
        savedInstanceState, viewLifecycleOwner,
        requireNotNull(viewModel),
        requireNotNull(settingsView),
        requireNotNull(toolbarView)
    ) {
      return@createComponent when (it) {
        is Explain -> showHowTo()
        is ClearAll -> killApplication()
        is NavigationError -> requireNotNull(settingsView).showError(it.throwable)
      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    toolbarView?.saveState(outState)
    settingsView?.saveState(outState)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    viewModel = null
    settingsView = null
    toolbarView = null
    factory = null
  }

  private fun killApplication() {
    requireContext().also {
      Timber.d("Clear application data")
      val activityManager = it.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      activityManager.clearApplicationUserData()
    }
  }

  private fun showHowTo() {
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
