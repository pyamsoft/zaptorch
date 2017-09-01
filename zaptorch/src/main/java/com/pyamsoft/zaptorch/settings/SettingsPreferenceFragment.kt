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
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.service.ServicePublisher
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.settings.SettingsPreferenceFragmentPresenter.Callback
import timber.log.Timber

class SettingsPreferenceFragment : ActionBarSettingsPreferenceFragment(), Callback {

  internal lateinit var servicePublisher: ServicePublisher
  internal lateinit var presenter: SettingsPreferenceFragmentPresenter

  override fun provideBoundPresenters(): List<Presenter<*>> =
      super.provideBoundPresenters() + listOf(presenter)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Injector.with(context) {
      it.plusSettingsComponent(context.getString(R.string.camera_api_key)).inject(this)
    }
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
