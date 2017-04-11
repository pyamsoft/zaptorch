/*
 * Copyright 2016 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import com.pyamsoft.zaptorch.base.preference.CameraPreferences;
import com.pyamsoft.zaptorch.base.preference.ClearPreferences;
import com.pyamsoft.zaptorch.base.preference.UIPreferences;

class ZapTorchPreferencesImpl implements CameraPreferences, ClearPreferences, UIPreferences {

  @NonNull private final String doublePressDelayKey;
  @NonNull private final String displayCameraErrorsKey;
  @NonNull private final String handleVolumeKeysKey;
  @NonNull private final String doublePressDelayDefault;
  @NonNull private final String cameraApiKey;
  @NonNull private final String cameraApiDefault;
  @NonNull private final SharedPreferences preferences;
  private final boolean displayCameraErrorsDefault;
  private final boolean handleVolumeKeysDefault;

  ZapTorchPreferencesImpl(@NonNull Context context) {
    final Context appContext = context.getApplicationContext();
    preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
    final Resources res = appContext.getResources();
    doublePressDelayKey = appContext.getString(R.string.double_press_delay_key);
    displayCameraErrorsKey = appContext.getString(R.string.display_camera_errors_key);
    handleVolumeKeysKey = appContext.getString(R.string.handle_volume_keys_key);
    doublePressDelayDefault = appContext.getString(R.string.double_press_delay_default);
    displayCameraErrorsDefault = res.getBoolean(R.bool.display_camera_errors_default);
    handleVolumeKeysDefault = res.getBoolean(R.bool.handle_volume_keys_default);
    cameraApiKey = appContext.getString(R.string.camera_api_key);
    cameraApiDefault = appContext.getString(R.string.camera_api_default);
  }

  @Override @CheckResult public long getButtonDelayTime() {
    return Long.parseLong(preferences.getString(doublePressDelayKey, doublePressDelayDefault));
  }

  @Override @CheckResult public boolean shouldShowErrorDialog() {
    return preferences.getBoolean(displayCameraErrorsKey, displayCameraErrorsDefault);
  }

  @Override @CheckResult public boolean shouldHandleKeys() {
    return preferences.getBoolean(handleVolumeKeysKey, handleVolumeKeysDefault);
  }

  @Override @CheckResult public int getCameraApi() {
    return Integer.parseInt(preferences.getString(cameraApiKey, cameraApiDefault));
  }

  @SuppressLint("ApplySharedPref") @Override public void clearAll() {
    // Commit because we must be sure transaction takes place before we continue
    preferences.edit().clear().commit();
  }

  @Override
  public void register(@NonNull SharedPreferences.OnSharedPreferenceChangeListener listener) {
    preferences.registerOnSharedPreferenceChangeListener(listener);
  }

  @Override
  public void unregister(@NonNull SharedPreferences.OnSharedPreferenceChangeListener listener) {
    preferences.unregisterOnSharedPreferenceChangeListener(listener);
  }
}
