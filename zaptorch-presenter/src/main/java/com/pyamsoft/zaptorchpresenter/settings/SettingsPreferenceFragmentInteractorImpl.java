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

package com.pyamsoft.zaptorchpresenter.settings;

import android.content.Context;
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.app.ApplicationPreferences;
import com.pyamsoft.pydroid.tool.AsyncOffloader;
import com.pyamsoft.pydroid.tool.Offloader;
import com.pyamsoft.zaptorchpresenter.R;
import com.pyamsoft.zaptorchpresenter.ZapTorchPreferences;
import timber.log.Timber;

class SettingsPreferenceFragmentInteractorImpl implements SettingsPreferenceFragmentInteractor {

  @SuppressWarnings("WeakerAccess") @NonNull final ZapTorchPreferences preferences;
  @SuppressWarnings("WeakerAccess") @NonNull final String cameraApiKey;

  SettingsPreferenceFragmentInteractorImpl(@NonNull Context context,
      @NonNull ZapTorchPreferences preferences) {
    this.preferences = preferences;
    this.cameraApiKey = context.getString(R.string.camera_api_key);
  }

  @NonNull @Override public Offloader<Boolean> clearAll() {
    return AsyncOffloader.newInstance(() -> {
      Timber.d("Clear all preferences");
      preferences.clearAll();
      return Boolean.TRUE;
    });
  }

  @Override public void registerCameraApiListener(
      @NonNull ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener) {
    unregisterCameraApiListener(cameraApiListener);
    preferences.register(cameraApiListener);
  }

  @Override public void unregisterCameraApiListener(
      @NonNull ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener) {
    preferences.unregister(cameraApiListener);
  }

  @NonNull @Override public String getCameraApiKey() {
    return cameraApiKey;
  }
}
