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

package com.pyamsoft.zaptorch.dagger.service;

import android.content.Context;
import android.support.annotation.NonNull;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import com.pyamsoft.zaptorch.app.service.camera.CameraInterface;
import javax.inject.Inject;

final class VolumeServiceInteractorImpl implements VolumeServiceInteractor {

  @NonNull private final ZapTorchPreferences preferences;
  @NonNull private final Context appContext;

  @Inject public VolumeServiceInteractorImpl(@NonNull Context context,
      @NonNull ZapTorchPreferences preferences) {
    this.appContext = context.getApplicationContext();
    this.preferences = preferences;
  }

  @Override public long getButtonDelayTime() {
    return preferences.getButtonDelayTime();
  }

  @Override public boolean shouldShowErrorDialog() {
    return preferences.shouldShowErrorDialog();
  }

  @Override public int getCameraApi() {
    return preferences.getCameraApi();
  }

  @NonNull @Override public CameraInterface marshmallowCamera() {
    return new MarshmallowCamera(appContext, this);
  }

  @NonNull @Override public CameraInterface lollipopCamera() {
    return new LollipopCamera(appContext, this);
  }

  @NonNull @Override public CameraInterface originalCamera() {
    return new OriginalCamera(appContext, this);
  }
}
