/*
 *     Copyright (C) 2017 Peter Kenji Yamanaka
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.pyamsoft.zaptorch.settings

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.view.View
import com.pyamsoft.pydroid.presenter.Presenter
import com.pyamsoft.pydroid.ui.app.fragment.ActionBarSettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.util.DialogUtil
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.service.ServicePublisher
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import timber.log.Timber

class SettingsPreferenceFragment : ActionBarSettingsPreferenceFragment(), SettingsPreferenceFragmentPresenter.View {

  internal lateinit var servicePublisher: ServicePublisher
  internal lateinit var presenter: SettingsPreferenceFragmentPresenter

  override fun provideBoundPresenters(): List<Presenter<*>> =
      super.provideBoundPresenters() + listOf(presenter)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Injector.obtain<ZapTorchComponent>(context.applicationContext).plusSettingsComponent(
        context.getString(R.string.camera_api_key)).inject(this)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val zapTorchExplain = findPreference(getString(R.string.zaptorch_explain_key))
    zapTorchExplain.setOnPreferenceClickListener {
      DialogUtil.guaranteeSingleDialogFragment(activity, HowToDialog(), "howto")
      return@setOnPreferenceClickListener true
    }

    presenter.bind(this)
  }

  override fun onApiChanged() {
    if (VolumeMonitorService.isRunning) {
      servicePublisher.publish(ServiceEvent(ServiceEvent.Type.CHANGE_CAMERA))
    }
  }

  override fun onClearAll() {
    Timber.d("received completed clearAll event. Kill Process")
    try {
      servicePublisher.publish(ServiceEvent(ServiceEvent.Type.FINISH))
    } catch (e: IllegalStateException) {
      Timber.e(e, "Expected exception when Service is NULL")
    }

    val activityManager = context.applicationContext
        .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    activityManager.clearApplicationUserData()
  }

  override fun onClearAllClicked() {
    DialogUtil.guaranteeSingleDialogFragment(activity, ConfirmationDialog(),
        "confirm_dialog")
  }

  override val rootViewContainer: Int
    get() = R.id.main_viewport

  override val applicationName: String
    get() = getString(R.string.app_name)

  override val preferenceXmlResId: Int
    get() = R.xml.preferences

  override fun onDestroy() {
    super.onDestroy()
    ZapTorch.getRefWatcher(this).watch(this)
  }

  companion object {

    const val TAG = "SettingsPreferenceFragment"
  }
}
