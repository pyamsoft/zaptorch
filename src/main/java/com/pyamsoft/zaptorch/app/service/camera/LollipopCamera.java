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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.Surface;
import com.pyamsoft.pydroid.util.LogUtil;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP) public final class LollipopCamera extends CameraCommon {

  private static final String TAG = LollipopCamera.class.getSimpleName();
  private final CameraManager cameraManager;
  private final String flashCameraId;
  private final CameraCallback cameraCallback;

  static CameraManager setupCameraManager(final @NonNull Context context) {
    return (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
  }

  public LollipopCamera(final @NonNull Context context) {
    super(context);
    this.cameraManager = setupCameraManager(getAppContext());
    this.flashCameraId = setupCamera();
    this.cameraCallback = new CameraCallback(cameraManager);
  }

  @Nullable private String setupCamera() {
    try {
      final String[] cameraList = cameraManager.getCameraIdList();
      for (final String camera : cameraList) {
        final Boolean hasFlash = cameraManager.getCameraCharacteristics(camera)
            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        if (hasFlash != null && hasFlash) {
          return camera;
        }
      }
    } catch (CameraAccessException e) {
      LogUtil.e(TAG, "setupCamera ERROR");
      LogUtil.exception(TAG, e);
    }
    return null;
  }

  @Override public void release() {
    cameraCallback.close();
  }

  @Override public void toggleTorch() {
    if (flashCameraId == null) {
      LogUtil.e(TAG, "No camera with Flash");
      startErrorExplanationActivity();
    } else {
      LogUtil.d(TAG, "Open camera");
      cameraCallback.accessCamera(getAppContext(), flashCameraId);
    }
  }

  static final class CameraCallback extends CameraDevice.StateCallback {
    private final CameraManager manager;
    private final List<Surface> list;

    private CameraDevice cameraDevice;
    private SessionCallback session;

    private boolean opened;
    private Size size;

    public CameraCallback(final @NonNull CameraManager manager) {
      this.manager = manager;
      opened = false;
      list = new ArrayList<>(1);
    }

    public void close() {
      // Surface texture is released by CameraManager so we don't have to
      if (opened) {
        if (session != null) {
          LogUtil.d(TAG, "close SessionCallback");
          session.close();
          session = null;
        }

        if (cameraDevice != null) {
          LogUtil.d(TAG, "close camera device");
          cameraDevice.close();
          cameraDevice = null;
        }
      }
      opened = false;
    }

    public void accessCamera(final @NonNull Context context, final @NonNull String id) {
      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
          == PackageManager.PERMISSION_GRANTED) {
        try {
          LogUtil.d(TAG, "Has camera permission, attempt to access");
          if (opened) {
            LogUtil.d(TAG, "Close opened camera");
            close();
          } else {
            LogUtil.d(TAG, "Open closed camera");
            manager.openCamera(id, this, null);
          }
        } catch (CameraAccessException e) {
          LogUtil.e(TAG, "toggleTorch ERROR");
          LogUtil.exception(TAG, e);
        }
      } else {
        LogUtil.e(TAG, "Missing camera permission");
      }
    }

    @Override public void onOpened(@NonNull CameraDevice camera) {
      LogUtil.d(TAG, "onOpened");
      opened = true;
      this.cameraDevice = camera;

      try {
        LogUtil.d(TAG, "create capture builder");
        final CaptureRequest.Builder captureBuilder =
            camera.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
        captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);

        if (size == null) {
          LogUtil.d(TAG, "get smallest size for texture");
          size = getSmallestSize(manager, camera.getId());
        } else {
          LogUtil.d(TAG, "using cached smallest size");
        }
        // The camera session recycles the surface texture, so we should not have to
        @SuppressLint("Recycle") final SurfaceTexture surfaceTexture = new SurfaceTexture(1);
        surfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());

        LogUtil.d(TAG, "add surface to texture");
        final Surface surface = new Surface(surfaceTexture);
        if (list.isEmpty()) {
          list.add(0, surface);
        } else {
          list.set(0, surface);
        }

        LogUtil.d(TAG, "add capture target");
        captureBuilder.addTarget(surface);

        LogUtil.d(TAG, "create new session callback");
        session = new SessionCallback(captureBuilder.build());

        LogUtil.d(TAG, "register capture session");
        camera.createCaptureSession(list, session, null);
      } catch (CameraAccessException e) {
        LogUtil.e(TAG, "onOpened");
        LogUtil.exception(TAG, e);
      }
    }

    @NonNull private static Size getSmallestSize(final @NonNull CameraManager manager,
        final @NonNull String id) throws CameraAccessException {
      LogUtil.d(TAG, "Get stream config map");
      final StreamConfigurationMap map = manager.getCameraCharacteristics(id)
          .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      if (map == null) {
        throw new IllegalStateException("Camera " + id + "doesn't support any Stream Maps.");
      }

      LogUtil.d(TAG, "Get possible output sizes");
      final Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);
      if (outputSizes == null || outputSizes.length == 0) {
        throw new IllegalStateException("Camera " + id + "doesn't support any outputSize.");
      }

      LogUtil.d(TAG, "Select a size");
      Size chosen = outputSizes[0];
      for (final Size s : outputSizes) {
        if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
          chosen = s;
        }
      }
      return chosen;
    }

    @Override public void onDisconnected(@NonNull CameraDevice cameraDevice) {
      this.cameraDevice = null;
      LogUtil.d(TAG, "onDisconnected");
      opened = false;
    }

    @Override public void onError(@NonNull CameraDevice cameraDevice, int i) {
      this.cameraDevice = null;
      LogUtil.e(TAG, "onError");
      opened = false;
    }
  }

  static final class SessionCallback extends CameraCaptureSession.StateCallback {

    private final CaptureRequest request;

    private CameraCaptureSession session;

    public SessionCallback(CaptureRequest request) {
      this.request = request;
    }

    @Override public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
      LogUtil.d(TAG, "Camera configured");
      session = cameraCaptureSession;
      try {
        LogUtil.d(TAG, "set repeating");
        cameraCaptureSession.setRepeatingRequest(request, null, null);
      } catch (CameraAccessException e) {
        LogUtil.e(TAG, "onConfigured");
        LogUtil.exception(TAG, e);
      }
    }

    @Override public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
      LogUtil.e(TAG, "onConfigureFailed");
      session = cameraCaptureSession;
      close();
    }

    public void close() {
      if (session != null) {
        LogUtil.d(TAG, "close session");
        session.close();
        session = null;
      }
    }
  }
}
