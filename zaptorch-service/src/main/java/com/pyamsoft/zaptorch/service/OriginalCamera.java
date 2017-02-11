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

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.pyamsoft.pydroid.helper.SubscriptionHelper;
import java.io.IOException;
import java.util.List;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import timber.log.Timber;

@SuppressWarnings("deprecation") class OriginalCamera extends CameraCommon
    implements SurfaceHolder.Callback {

  @SuppressWarnings("WeakerAccess") @NonNull final WindowManager windowManager;
  @SuppressWarnings("WeakerAccess") @NonNull final SurfaceView surfaceView;
  @SuppressWarnings("WeakerAccess") @NonNull final WindowManager.LayoutParams params;
  @NonNull private final Scheduler obsScheduler;
  @NonNull private final Scheduler subScheduler;
  @SuppressWarnings("WeakerAccess") @Nullable Camera camera;
  @SuppressWarnings("WeakerAccess") boolean opened;
  @SuppressWarnings("WeakerAccess") @Nullable Subscription cameraSubscription;

  OriginalCamera(final @NonNull Context context, final @NonNull VolumeServiceInteractor interactor,
      @NonNull Scheduler obsScheduler, @NonNull Scheduler subScheduler) {
    super(context, interactor, obsScheduler, subScheduler);
    this.obsScheduler = obsScheduler;
    this.subScheduler = subScheduler;
    Timber.d("OLD CAMERA API");
    opened = false;

    surfaceView = new SurfaceView(getAppContext());
    windowManager = (WindowManager) getAppContext().getSystemService(Context.WINDOW_SERVICE);

    params = new WindowManager.LayoutParams();
    params.width = 1;
    params.height = 1;
    params.gravity = Gravity.TOP | Gravity.START;
    params.format = PixelFormat.TRANSLUCENT;
    params.type = WindowManager.LayoutParams.TYPE_TOAST;
    params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
  }

  @SuppressWarnings("WeakerAccess") SurfaceHolder getInitializedHolder() {
    final SurfaceHolder holder = surfaceView.getHolder();
    holder.addCallback(this);
    return holder;
  }

  @Override public void toggleTorch() {
    if (ActivityCompat.checkSelfPermission(getAppContext(), Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED) {
      if (opened) {
        Timber.d("Camera is open, close it");
        release();
      } else {
        connectToCameraService();
      }
    } else {
      Timber.e("Missing setupCamera permission");
      startPermissionExplanationActivity();
    }
  }

  private void connectToCameraService() {
    Timber.d("Camera is closed, open it");
    SubscriptionHelper.unsubscribe(cameraSubscription);
    cameraSubscription = Observable.fromCallable(Camera::open)
        .subscribeOn(subScheduler)
        .observeOn(obsScheduler)
        .subscribe(this::cameraOpened, throwable -> {
          Timber.e(throwable, "onError connectToCameraService");
          clearCamera(throwable);
          startErrorExplanationActivity();
        }, () -> SubscriptionHelper.unsubscribe(cameraSubscription));
  }

  @SuppressWarnings("WeakerAccess") void cameraOpened(@NonNull Camera camera) {
    final Camera.Parameters parameters = camera.getParameters();
    if (parameters.getFlashMode() == null) {
      Timber.e("Null flash mode");
      camera.release();
      startErrorExplanationActivity();
      return;
    }

    final List<String> supportedFlashModes = parameters.getSupportedFlashModes();
    if (supportedFlashModes == null
        || supportedFlashModes.isEmpty()
        || !supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
      Timber.e("Camera parameters do not include Torch");
      camera.release();
      startErrorExplanationActivity();
      return;
    }

    Timber.d("Camera should have torch mode");
    try {
      Timber.d("Camera has flash");
      this.camera = camera;
      windowManager.addView(surfaceView, params);
      assert this.camera != null;
      Timber.d("set preview");
      final SurfaceHolder holder = getInitializedHolder();
      this.camera.setPreviewDisplay(holder);
      final Camera.Parameters cameraParameters = this.camera.getParameters();
      cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
      this.camera.setParameters(cameraParameters);

      Timber.d("start setupCamera");
      this.camera.startPreview();
      opened = true;
      notifyCallbackOnOpened();
    } catch (IOException e) {
      clearCamera(e);
      startErrorExplanationActivity();
    }
  }

  @SuppressWarnings("WeakerAccess") void clearCamera(@NonNull Throwable throwable) {
    Timber.e(throwable, "Error opening setupCamera");
    release();
  }

  private void releaseCamera() {
    Timber.d("release setupCamera");
    if (camera != null) {
      camera.release();
      camera = null;
    }
  }

  @Override public void release() {
    if (opened) {
      if (camera != null) {
        final Camera.Parameters params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        camera.stopPreview();
        releaseCamera();
        windowManager.removeView(surfaceView);
      }
      opened = false;
      notifyCallbackOnClosed();
    }
    SubscriptionHelper.unsubscribe(cameraSubscription);
    super.release();
  }

  @Override public void surfaceCreated(SurfaceHolder surfaceHolder) {
    if (surfaceHolder != null && camera != null) {
      try {
        Timber.d("Surface created");
        camera.setPreviewDisplay(surfaceHolder);
      } catch (IOException e) {
        Timber.e(e, "surfaceCreated ERROR");
      }
    }
  }

  @Override public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

  }

  @Override public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (surfaceHolder != null && camera != null) {
      Timber.d("Surface destroyed");
      camera.stopPreview();
    }
  }
}
