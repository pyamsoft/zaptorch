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

package com.pyamsoft.zaptorch.service;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import rx.Scheduler;
import timber.log.Timber;

@TargetApi(Build.VERSION_CODES.M) class MarshmallowCamera extends CameraCommon {

  @NonNull private final TorchCallback torchCallback;
  @NonNull private final CameraManager cameraManager;

  MarshmallowCamera(final @NonNull Context context,
      final @NonNull VolumeServiceInteractor interactor, @NonNull Scheduler obsScheduler,
      @NonNull Scheduler subScheduler) {
    super(context, interactor, obsScheduler, subScheduler);
    Timber.d("MARSHMALLOW CAMERA API");
    cameraManager = LollipopCamera.setupCameraManager(context);
    this.torchCallback = new TorchCallback(this);
    setupCamera();
  }

  @Override public void toggleTorch() {
    setTorch(!torchCallback.isEnabled());
  }

  private void setTorch(final boolean enable) {
    final String cameraId = torchCallback.getCameraId();
    if (cameraId != null) {
      try {
        Timber.d("Set torch: %s", enable);
        cameraManager.setTorchMode(cameraId, enable);
      } catch (CameraAccessException e) {
        Timber.e(e, "toggleTorch ERROR");
        startErrorExplanationActivity();
      }
    } else {
      Timber.e("Torch unavailable");
      startErrorExplanationActivity();
    }
  }

  @Override public void release() {
    super.release();
    if (torchCallback.isEnabled()) {
      setTorch(false);
    }

    Timber.d("Unregister torch callback");
    cameraManager.unregisterTorchCallback(torchCallback);
  }

  private void setupCamera() {
    Timber.d("Register torch callback");
    cameraManager.registerTorchCallback(torchCallback, null);
  }

  @SuppressWarnings("WeakerAccess") static final class TorchCallback
      extends CameraManager.TorchCallback {

    @NonNull private final CameraCommon cameraCommon;
    @Nullable String cameraId;
    boolean enabled;

    TorchCallback(@NonNull CameraCommon cameraCommon) {
      this.cameraCommon = cameraCommon;
    }

    @Nullable @CheckResult String getCameraId() {
      return cameraId;
    }

    @CheckResult boolean isEnabled() {
      return enabled;
    }

    @Override public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
      super.onTorchModeChanged(cameraId, enabled);
      Timber.d("Torch changed: %s", enabled);
      this.cameraId = cameraId;
      this.enabled = enabled;

      if (enabled) {
        cameraCommon.notifyCallbackOnOpened();
      } else {
        cameraCommon.notifyCallbackOnClosed();
      }
    }

    @Override public void onTorchModeUnavailable(@NonNull String cameraId) {
      super.onTorchModeUnavailable(cameraId);
      Timber.e("Torch unavailable");
      this.cameraId = null;
      this.enabled = false;

      cameraCommon.notifyCallbackOnClosed();
    }
  }
}
