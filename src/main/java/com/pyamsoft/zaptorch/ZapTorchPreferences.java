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

package com.pyamsoft.zaptorch;

import android.content.Context;
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.base.ApplicationPreferences;

public class ZapTorchPreferences extends ApplicationPreferences {

  @NonNull private final String doublePressDelayKey;
  @NonNull private final String displayCameraErrorsKey;
  @NonNull private final String handleVolumeKeysKey;
  @NonNull private final String doublePressDelayDefault;
  private final boolean displayCameraErrorsDefault;
  private final boolean handleVolumeKeysDefault;

  public ZapTorchPreferences(Context context) {
    super(context);
    final Context appContext = context.getApplicationContext();
    doublePressDelayKey = appContext.getString(R.string.double_press_delay_key);
    displayCameraErrorsKey = appContext.getString(R.string.double_press_delay_key);
    handleVolumeKeysKey = appContext.getString(R.string.double_press_delay_key);
    doublePressDelayDefault = appContext.getString(R.string.double_press_delay_default);
    displayCameraErrorsDefault =
        appContext.getResources().getBoolean(R.bool.display_camera_errors_default);
    handleVolumeKeysDefault =
        appContext.getResources().getBoolean(R.bool.handle_volume_keys_default);
  }

  public final long getButtonDelayTime() {
    return Long.parseLong(get(doublePressDelayKey, doublePressDelayDefault));
  }

  public final boolean shouldShowErrorDialog() {
    return get(displayCameraErrorsKey, displayCameraErrorsDefault);
  }

  public final boolean shouldHandleKeys() {
    return get(handleVolumeKeysKey, handleVolumeKeysDefault);
  }
}
