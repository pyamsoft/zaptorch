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
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.pyamsoft.zaptorch.app.service.VolumeServicePresenter;
import java.io.IOException;
import java.util.List;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

@SuppressWarnings("deprecation") final class OriginalCamera extends CameraCommon
    implements SurfaceHolder.Callback {

  @NonNull private final WindowManager windowManager;
  @NonNull private final SurfaceView surfaceView;
  @NonNull private final WindowManager.LayoutParams params;

  @NonNull private Subscription cameraSubscription = Subscriptions.empty();

  private Camera camera;
  private boolean opened;

  public OriginalCamera(final @NonNull Context context,
      final @NonNull VolumeServicePresenter presenter) {
    super(context, presenter);
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

  private SurfaceHolder getInitializedHolder() {
    final SurfaceHolder holder = surfaceView.getHolder();
    holder.addCallback(this);
    return holder;
  }

  private void unsubCameraSubscription() {
    if (!cameraSubscription.isUnsubscribed()) {
      cameraSubscription.unsubscribe();
    }
  }

  @Override public void toggleTorch() {
    if (opened) {
      Timber.d("Camera is open, close it");
      release();
    } else {
      Timber.d("Camera is closed, open it");
      unsubCameraSubscription();
      cameraSubscription = Observable.defer(() -> Observable.just(Camera.open()))
          .filter(camera1 -> camera1 != null)
          .filter(camera1 -> {
            final Camera.Parameters parameters = camera1.getParameters();
            if (parameters.getFlashMode() == null) {
              Timber.e("Null flash mode");
              camera1.release();
              return false;
            }

            final List<String> supportedFlashModes = parameters.getSupportedFlashModes();
            if (supportedFlashModes == null || supportedFlashModes.isEmpty() || !supportedFlashModes
                .contains(Camera.Parameters.FLASH_MODE_TORCH)) {
              Timber.e("Camera parameters do not include Torch");
              camera1.release();
              return false;
            }

            Timber.d("Camera should have torch mode");
            return true;
          })
          .subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(camera1 -> {
            try {
              Timber.d("Camera has flash");
              camera = camera1;
              windowManager.addView(surfaceView, params);

              Timber.d("set preview");
              final SurfaceHolder holder = getInitializedHolder();
              camera.setPreviewDisplay(holder);
              final Camera.Parameters cameraParameters = camera.getParameters();
              cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
              camera.setParameters(cameraParameters);

              Timber.d("start camera");
              camera.startPreview();
              opened = true;
            } catch (IOException e) {
              throw new CameraSetupError();
            }
          }, throwable -> {
            Timber.e(throwable, "Error opening camera");
            if (camera != null) {
              camera.release();
              camera = null;
              opened = false;
            }
            startErrorExplanationActivity();
          });
    }
  }

  @Override public void release() {
    if (camera != null && opened) {
      final Camera.Parameters params = camera.getParameters();
      params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
      camera.setParameters(params);
      camera.stopPreview();
      Timber.d("release camera");
      camera.release();
      camera = null;
      opened = false;
    }
    unsubCameraSubscription();

    windowManager.removeView(surfaceView);
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

  static final class CameraSetupError extends RuntimeException {

  }
}
