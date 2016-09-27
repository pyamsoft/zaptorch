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

package com.pyamsoft.zaptorch.dagger.settings;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.ActionSingle;
import com.pyamsoft.pydroid.app.ApplicationPreferences;
import com.pyamsoft.pydroid.tool.AsyncCallbackTask;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import timber.log.Timber;

class SettingsPreferenceFragmentInteractorImpl implements SettingsPreferenceFragmentInteractor {

  @SuppressWarnings("WeakerAccess") @NonNull final ZapTorchPreferences preferences;
  @SuppressWarnings("WeakerAccess") @NonNull final String cameraApiKey;

  SettingsPreferenceFragmentInteractorImpl(@NonNull Context context,
      @NonNull ZapTorchPreferences preferences) {
    this.preferences = preferences;
    this.cameraApiKey = context.getString(R.string.camera_api_key);
  }

  @NonNull @Override
  public AsyncTask<Void, Void, Boolean> clearAll(@NonNull ActionSingle<Boolean> onLoaded) {
    return new AsyncCallbackTask<Void, Boolean>(onLoaded) {
      @Override protected Boolean doInBackground(Void... params) {
        Timber.d("Clear all preferences");
        preferences.clearAll();
        return true;
      }
    };
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
