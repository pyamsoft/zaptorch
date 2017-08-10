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

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.CheckResult
import com.pyamsoft.zaptorch.base.preference.ClearPreferences
import com.pyamsoft.zaptorch.base.preference.UIPreferences
import io.reactivex.Single

internal class SettingsPreferenceFragmentInteractor internal constructor(
    context: Context, private val preferences: UIPreferences,
    private val clearPreferences: ClearPreferences) {

  internal val cameraApiKey = context.getString(R.string.camera_api_key)

  @CheckResult fun clearAll(): Single<Boolean> {
    return Single.fromCallable {
      clearPreferences.clearAll()
      return@fromCallable true
    }
  }

  fun registerCameraApiListener(
      cameraApiListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    unregisterCameraApiListener(cameraApiListener)
    if (cameraApiListener != null) {
      preferences.register(cameraApiListener)
    }
  }

  fun unregisterCameraApiListener(
      cameraApiListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    if (cameraApiListener != null) {
      preferences.unregister(cameraApiListener)
    }
  }
}
