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
import android.os.Build;
import com.pyamsoft.zaptorch.app.service.VolumeServiceInteractor;
import com.pyamsoft.zaptorch.app.service.VolumeServicePresenter;
import com.pyamsoft.zaptorch.app.service.camera.CameraInterface;
import com.pyamsoft.zaptorch.app.service.camera.LollipopCamera;
import com.pyamsoft.zaptorch.app.service.camera.MarshmallowCamera;
import com.pyamsoft.zaptorch.app.service.camera.OriginalCamera;
import com.pyamsoft.zaptorch.dagger.ActivityScope;
import dagger.Module;
import dagger.Provides;

@Module public class VolumeServiceModule {

  @Provides @ActivityScope VolumeServicePresenter provideVolumeServicePresenter(
      final VolumeServicePresenterImpl presenter) {
    return presenter;
  }

  @Provides @ActivityScope VolumeServiceInteractor provideVolumeServiceInteractor(
      final VolumeServiceInteractorImpl interactor) {
    return interactor;
  }

  @Provides @ActivityScope CameraInterface provideCameraInterface(final Context context) {
    final Context appContext = context.getApplicationContext();
    CameraInterface cameraInterface;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      cameraInterface = new MarshmallowCamera(appContext);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      cameraInterface = new LollipopCamera(appContext);
    } else {
      cameraInterface = new OriginalCamera(appContext);
    }

    return cameraInterface;
  }
}
