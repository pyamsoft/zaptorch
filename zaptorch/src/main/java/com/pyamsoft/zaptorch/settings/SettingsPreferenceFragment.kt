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
import android.support.v7.preference.Preference
import android.view.View
import com.pyamsoft.pydroid.ui.app.fragment.ActionBarSettingsPreferenceFragment
import com.pyamsoft.pydroid.ui.util.DialogUtil
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorch
import com.pyamsoft.zaptorch.model.ServiceEvent
import com.pyamsoft.zaptorch.service.ServicePresenter
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import timber.log.Timber

class SettingsPreferenceFragment : ActionBarSettingsPreferenceFragment() {

  private lateinit var zapTorchExplain: Preference
  internal lateinit var servicePresenter: ServicePresenter
  internal lateinit var presenter: SettingsPreferenceFragmentPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    Injector.with(context) {
      it.inject(this)
    }
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    zapTorchExplain = findPreference(getString(R.string.zaptorch_explain_key))
  }

  override fun onStart() {
    super.onStart()
    presenter.listenForCameraChanges {
      if (VolumeMonitorService.isRunning) {
        servicePresenter.publish(ServiceEvent(ServiceEvent.Type.CHANGE_CAMERA))
      }
    }

    presenter.clickEvent(zapTorchExplain, {
      DialogUtil.guaranteeSingleDialogFragment(activity, HowToDialog(), "howto")
    })

    presenter.registerEventBus {
      Timber.d("received completed clearAll event. Kill Process")
      try {
        servicePresenter.publish(ServiceEvent(ServiceEvent.Type.FINISH))
      } catch (e: IllegalStateException) {
        Timber.e(e, "Expected exception when Service is NULL")
      }

      val activityManager = context.applicationContext
          .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
      activityManager.clearApplicationUserData()
    }
  }

  override fun onStop() {
    super.onStop()
    presenter.stop()
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
    presenter.destroy()
    ZapTorch.getRefWatcher(this).watch(this)
  }

  companion object {

    const val TAG = "SettingsPreferenceFragment"
  }
}
