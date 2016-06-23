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

package com.pyamsoft.zaptorch.dagger;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.base.app.ApplicationPreferences;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorchPreferences;

public class ZapTorchPreferencesImpl extends ApplicationPreferences implements ZapTorchPreferences {

  @NonNull private final String doublePressDelayKey;
  @NonNull private final String displayCameraErrorsKey;
  @NonNull private final String handleVolumeKeysKey;
  @NonNull private final String doublePressDelayDefault;
  @NonNull private final String cameraApiKey;
  @NonNull private final String cameraApiDefault;
  private final boolean displayCameraErrorsDefault;
  private final boolean handleVolumeKeysDefault;

  public ZapTorchPreferencesImpl(@NonNull Context context) {
    super(context);
    final Context appContext = context.getApplicationContext();
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

  @Override public final long getButtonDelayTime() {
    return Long.parseLong(get(doublePressDelayKey, doublePressDelayDefault));
  }

  @Override public final boolean shouldShowErrorDialog() {
    return get(displayCameraErrorsKey, displayCameraErrorsDefault);
  }

  @Override public final boolean shouldHandleKeys() {
    return get(handleVolumeKeysKey, handleVolumeKeysDefault);
  }

  @Override public final int getCameraApi() {
    return Integer.parseInt(get(cameraApiKey, cameraApiDefault));
  }

  @Override public void clearAll() {
    clear(true);
  }
}
