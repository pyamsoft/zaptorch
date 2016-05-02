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

package com.pyamsoft.zaptorch.app.service.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import com.pyamsoft.pydroid.util.LogUtil;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;

@SuppressWarnings("deprecation") public final class OriginalCamera extends CameraCommon
    implements SurfaceHolder.Callback {

  private static final String TAG = OriginalCamera.class.getSimpleName();
  private final WindowManager windowManager;
  private final SurfaceView surfaceView;
  private final WindowManager.LayoutParams params;

  private Camera camera;
  private boolean opened;
  private CameraAsyncTask cameraAsyncTask;

  public OriginalCamera(final @NonNull Context context) {
    super(context);
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

  final void setOpened(boolean opened) {
    LogUtil.d(TAG, "Set opened: ", opened);
    this.opened = opened;
  }

  final void setCamera(Camera camera) {
    LogUtil.d(TAG, "Set camera field to: ", camera);
    this.camera = camera;
  }

  final void addSurface() {
    windowManager.addView(surfaceView, params);
  }

  final SurfaceHolder getInitializedHolder() {
    final SurfaceHolder holder = surfaceView.getHolder();
    holder.addCallback(this);
    return holder;
  }

  @Override public void toggleTorch() {
    if (opened) {
      LogUtil.d(TAG, "Camera is open, close it");
      release();
    } else {
      LogUtil.d(TAG, "Camera is closed, open it");
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
      LogUtil.d(TAG, "release camera");
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
        LogUtil.d(TAG, "Surface created");
        camera.setPreviewDisplay(surfaceHolder);
      } catch (IOException e) {
        LogUtil.e(TAG, "surfaceCreated ERROR");
        LogUtil.exception(TAG, e);
      }
    }
  }

  @Override public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

  }

  @Override public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    if (surfaceHolder != null && camera != null) {
      LogUtil.d(TAG, "Surface destroyed");
      camera.stopPreview();
    }
  }

  static final class CameraAsyncTask extends AsyncTask<Void, Void, Camera> {

    private final WeakReference<OriginalCamera> cameraWeakReference;

    CameraAsyncTask(final @NonNull OriginalCamera originalCamera) {
      this.cameraWeakReference = new WeakReference<>(originalCamera);
    }

    @Override protected Camera doInBackground(Void... voids) {
      return Camera.open();
    }

    @Override protected void onPostExecute(Camera camera) {
      super.onPostExecute(camera);
      final OriginalCamera originalCamera = cameraWeakReference.get();
      if (originalCamera == null) {
        LogUtil.e(TAG, "Camera weakRef is NULL");
        return;
      }

      try {
        LogUtil.d(TAG, "Check if camera has flash");
        if (hasFlash(camera)) {
          LogUtil.d(TAG, "Camera has flash");
          originalCamera.setCamera(camera);
          originalCamera.addSurface();
          LogUtil.d(TAG, "set preview");
          final SurfaceHolder holder = originalCamera.getInitializedHolder();
          camera.setPreviewDisplay(holder);
          final Camera.Parameters params1 = camera.getParameters();
          params1.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
          camera.setParameters(params1);
          LogUtil.d(TAG, "start camera");
          camera.startPreview();
          originalCamera.setOpened(true);
        } else {
          LogUtil.e(TAG, "Camera does not have flash");
          camera.release();
          originalCamera.setCamera(null);
          originalCamera.setOpened(false);
        }
      } catch (Exception e) {
        LogUtil.e(TAG, "toggleTorch onError");
        LogUtil.exception(TAG, e);
        originalCamera.setCamera(null);
        originalCamera.setOpened(false);
        originalCamera.startErrorExplanationActivity();
      }
    }

    @Override protected void onCancelled(Camera camera) {
      LogUtil.e(TAG, "CameraAsyncTask has been cancelled");
      final OriginalCamera originalCamera = cameraWeakReference.get();
      if (originalCamera != null) {
        originalCamera.setCamera(null);
        originalCamera.setOpened(false);
      }
    }

    private static boolean hasFlash(final Camera camera) {
      if (camera == null) {
        LogUtil.e(TAG, "Null camera");
        return false;
      }

      final Camera.Parameters parameters = camera.getParameters();
      if (parameters.getFlashMode() == null) {
        LogUtil.e(TAG, "Null flash mode");
        return false;
      }

      final List<String> supportedFlashModes = parameters.getSupportedFlashModes();
      if (supportedFlashModes == null
          || supportedFlashModes.isEmpty()
          || !supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
        LogUtil.e(TAG, "Camera parameters do not include Torch");
        return false;
      }

      LogUtil.d(TAG, "Camera should have torch mode");
      return true;
    }
  }
}
