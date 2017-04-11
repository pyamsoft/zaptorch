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

package com.pyamsoft.zaptorch.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.helper.Checker;
import com.pyamsoft.zaptorch.base.preference.ClearPreferences;
import com.pyamsoft.zaptorch.base.preference.UIPreferences;
import io.reactivex.Single;

class SettingsPreferenceFragmentInteractor {

  @SuppressWarnings("WeakerAccess") @NonNull final ClearPreferences clearPreferences;
  @SuppressWarnings("WeakerAccess") @NonNull final UIPreferences preferences;
  @SuppressWarnings("WeakerAccess") @NonNull final String cameraApiKey;

  SettingsPreferenceFragmentInteractor(@NonNull Context context, @NonNull UIPreferences preferences,
      @NonNull ClearPreferences clearPreferences) {
    this.preferences = Checker.checkNonNull(preferences);
    this.clearPreferences = Checker.checkNonNull(clearPreferences);
    this.cameraApiKey = Checker.checkNonNull(context).getString(R.string.camera_api_key);
  }

  /**
   * public
   */
  @NonNull @CheckResult Single<Boolean> clearAll() {
    return Single.fromCallable(() -> {
      clearPreferences.clearAll();
      return Boolean.TRUE;
    });
  }

  /**
   * public
   */
  void registerCameraApiListener(
      @Nullable SharedPreferences.OnSharedPreferenceChangeListener cameraApiListener) {
    unregisterCameraApiListener(cameraApiListener);
    if (cameraApiListener != null) {
      preferences.register(cameraApiListener);
    }
  }

  /**
   * public
   */
  void unregisterCameraApiListener(
      @Nullable SharedPreferences.OnSharedPreferenceChangeListener cameraApiListener) {
    if (cameraApiListener != null) {
      preferences.unregister(cameraApiListener);
    }
  }

  /**
   * public
   */
  @NonNull @CheckResult String getCameraApiKey() {
    return cameraApiKey;
  }
}
