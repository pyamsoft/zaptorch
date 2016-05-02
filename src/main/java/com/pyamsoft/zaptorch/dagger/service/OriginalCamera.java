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
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.pyamsoft.zaptorch.app.service.VolumeServicePresenter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import timber.log.Timber;

@SuppressWarnings("deprecation") final class OriginalCamera extends CameraCommon
    implements SurfaceHolder.Callback {

  private final WindowManager windowManager;
  private final SurfaceView surfaceView;
  private final WindowManager.LayoutParams params;

  private Camera camera;
  private boolean opened;
  private CameraAsyncTask cameraAsyncTask;

  public OriginalCamera(final @NonNull Context context, final @NonNull VolumeServicePresenter presenter) {
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

  private void setOpened(boolean opened) {
    Timber.d("Set opened: %s", opened);
    this.opened = opened;
  }

  private void setCamera(Camera camera) {
    Timber.d("Set camera field to: %s", camera);
    this.camera = camera;
  }

  private void addSurface() {
    windowManager.addView(surfaceView, params);
  }

  private SurfaceHolder getInitializedHolder() {
    final SurfaceHolder holder = surfaceView.getHolder();
    holder.addCallback(this);
    return holder;
  }

  @Override public void toggleTorch() {
    if (opened) {
      Timber.d("Camera is open, close it");
      release();
    } else {
      Timber.d("Camera is closed, open it");
      if (cameraAsyncTask != null) {
        cameraAsyncTask.cancel(true);
      }
      cameraAsyncTask = new CameraAsyncTask(this);
      cameraAsyncTask.execute();
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

    if (cameraAsyncTask != null) {
      cameraAsyncTask.cancel(true);
      cameraAsyncTask = null;
    }

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

  static final class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

    private final WeakReference<OriginalCamera> cameraWeakReference;

    CameraAsyncTask(final @NonNull OriginalCamera originalCamera) {
      this.cameraWeakReference = new WeakReference<>(originalCamera);
    }

    private static boolean hasFlash(final Camera camera) {
      if (camera == null) {
        Timber.e("Null camera");
        return false;
      }

      final Camera.Parameters parameters = camera.getParameters();
      if (parameters.getFlashMode() == null) {
        Timber.e("Null flash mode");
        return false;
      }

      final List<String> supportedFlashModes = parameters.getSupportedFlashModes();
      if (supportedFlashModes == null
          || supportedFlashModes.isEmpty()
          || !supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
        Timber.e("Camera parameters do not include Torch");
        return false;
      }

      Timber.d("Camera should have torch mode");
      return true;
    }

    @Override protected Camera doInBackground(Void... voids) {
      return Camera.open();
    }

    @Override protected void onPostExecute(Camera camera) {
      super.onPostExecute(camera);
      final OriginalCamera originalCamera = cameraWeakReference.get();
      if (originalCamera == null) {
        Timber.e("Camera weakRef is NULL");
        return;
      }

      try {
        Timber.d("Check if camera has flash");
        if (hasFlash(camera)) {
          Timber.d("Camera has flash");
          originalCamera.setCamera(camera);
          originalCamera.addSurface();
          Timber.d("set preview");
          final SurfaceHolder holder = originalCamera.getInitializedHolder();
          camera.setPreviewDisplay(holder);
          final Camera.Parameters params1 = camera.getParameters();
          params1.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
          camera.setParameters(params1);
          Timber.d("start camera");
          camera.startPreview();
          originalCamera.setOpened(true);
        } else {
          Timber.e("Camera does not have flash");
          camera.release();
          originalCamera.setCamera(null);
          originalCamera.setOpened(false);
        }
      } catch (Exception e) {
        Timber.e(e, "toggleTorch onError");
        originalCamera.setCamera(null);
        originalCamera.setOpened(false);
        originalCamera.startErrorExplanationActivity();
      }
    }

    @Override protected void onCancelled(Camera camera) {
      Timber.e("CameraAsyncTask has been cancelled");
      final OriginalCamera originalCamera = cameraWeakReference.get();
      if (originalCamera != null) {
        originalCamera.setCamera(null);
        originalCamera.setOpened(false);
      }
    }
  }
}
