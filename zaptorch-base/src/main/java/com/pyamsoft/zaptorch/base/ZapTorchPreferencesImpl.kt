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

package com.pyamsoft.zaptorch.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.CheckResult
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.ClearPreferences
import com.pyamsoft.zaptorch.api.UIPreferences

internal class ZapTorchPreferencesImpl(
  context: Context
) : CameraPreferences,
    ClearPreferences, UIPreferences {

  private val doublePressDelayKey: String
  private val displayCameraErrorsKey: String
  private val handleVolumeKeysKey: String
  private val doublePressDelayDefault: String
  private val displayCameraErrorsDefault: Boolean
  private val handleVolumeKeysDefault: Boolean
  private val preferences: SharedPreferences =
    PreferenceManager.getDefaultSharedPreferences(context)

  init {
    val res = context.resources
    doublePressDelayKey = res.getString(R.string.double_press_delay_key)
    displayCameraErrorsKey = res.getString(R.string.display_camera_errors_key)
    handleVolumeKeysKey = res.getString(R.string.handle_volume_keys_key)
    doublePressDelayDefault = res.getString(R.string.double_press_delay_default)
    displayCameraErrorsDefault = res.getBoolean(R.bool.display_camera_errors_default)
    handleVolumeKeysDefault = res.getBoolean(R.bool.handle_volume_keys_default)
  }

  override val buttonDelayTime: Long
    @CheckResult get() = preferences.getString(
        doublePressDelayKey, doublePressDelayDefault
    ).orEmpty().toLong()

  @CheckResult
  override fun shouldShowErrorDialog(): Boolean =
    preferences.getBoolean(displayCameraErrorsKey, displayCameraErrorsDefault)

  @CheckResult
  override fun shouldHandleKeys(): Boolean =
    preferences.getBoolean(handleVolumeKeysKey, handleVolumeKeysDefault)

  @SuppressLint("ApplySharedPref")
  override fun clearAll() {
    // Commit because we must be sure transaction takes place before we continue
    preferences.edit(commit = true) {
      clear()
    }
  }

  override fun register(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
    preferences.registerOnSharedPreferenceChangeListener(listener)
  }

  override fun unregister(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
    preferences.unregisterOnSharedPreferenceChangeListener(listener)
  }
}
