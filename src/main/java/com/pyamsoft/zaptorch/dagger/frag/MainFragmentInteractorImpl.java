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

package com.pyamsoft.zaptorch.dagger.frag;

import android.content.Context;
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.base.ApplicationPreferences;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import javax.inject.Inject;
import rx.Observable;
import timber.log.Timber;

class MainFragmentInteractorImpl implements MainFragmentInteractor {

  @NonNull final ZapTorchPreferences preferences;
  @NonNull final Context appContext;
  @NonNull final String cameraApiKey;

  @Inject MainFragmentInteractorImpl(@NonNull Context context,
      @NonNull ZapTorchPreferences preferences) {
    this.appContext = context.getApplicationContext();
    this.preferences = preferences;
    this.cameraApiKey = appContext.getString(R.string.camera_api_key);
  }

  @NonNull @Override public Observable<Boolean> clearAll() {
    return Observable.defer(() -> {
      Timber.d("Clear all preferences");
      preferences.clearAll();
      return Observable.just(true);
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
