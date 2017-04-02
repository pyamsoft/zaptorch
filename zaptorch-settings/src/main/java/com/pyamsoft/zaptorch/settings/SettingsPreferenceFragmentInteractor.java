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
import com.pyamsoft.zaptorch.base.ZapTorchPreferences;
import io.reactivex.Observable;
import timber.log.Timber;

class SettingsPreferenceFragmentInteractor {

  @SuppressWarnings("WeakerAccess") @NonNull final ZapTorchPreferences preferences;
  @SuppressWarnings("WeakerAccess") @NonNull final String cameraApiKey;

  SettingsPreferenceFragmentInteractor(@NonNull Context context,
      @NonNull ZapTorchPreferences preferences) {
    this.preferences = Checker.checkNonNull(preferences);
    this.cameraApiKey = Checker.checkNonNull(context).getString(R.string.camera_api_key);
  }

  @NonNull @CheckResult public Observable<Boolean> clearAll() {
    return Observable.fromCallable(() -> {
      Timber.d("Clear all preferences");
      preferences.clearAll();
      return Boolean.TRUE;
    });
  }

  public void registerCameraApiListener(
      @Nullable SharedPreferences.OnSharedPreferenceChangeListener cameraApiListener) {
    unregisterCameraApiListener(cameraApiListener);
    if (cameraApiListener != null) {
      preferences.register(cameraApiListener);
    }
  }

  public void unregisterCameraApiListener(
      @Nullable SharedPreferences.OnSharedPreferenceChangeListener cameraApiListener) {
    if (cameraApiListener != null) {
      preferences.unregister(cameraApiListener);
    }
  }

  @NonNull @CheckResult public String getCameraApiKey() {
    return cameraApiKey;
  }
}
