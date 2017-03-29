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
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Size;
import android.view.Surface;
import com.pyamsoft.pydroid.helper.Checker;
import io.reactivex.Scheduler;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

@TargetApi(Build.VERSION_CODES.LOLLIPOP) class LollipopCamera extends CameraCommon {

  @NonNull private final CameraManager cameraManager;
  @NonNull private final CameraCallback cameraCallback;
  @Nullable private final String flashCameraId;

  LollipopCamera(final @NonNull Context context, final @NonNull VolumeServiceInteractor interactor,
      @NonNull Scheduler obsScheduler, @NonNull Scheduler subScheduler) {
    super(context, interactor, obsScheduler, subScheduler);
    Timber.d("LOLLIPOP CAMERA API");
    this.cameraManager = setupCameraManager(getAppContext());
    this.flashCameraId = setupCamera();
    this.cameraCallback = new CameraCallback(this, cameraManager);
  }

  @CheckResult @NonNull static CameraManager setupCameraManager(final @NonNull Context context) {
    return (CameraManager) context.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
  }

  @CheckResult @Nullable private String setupCamera() {
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
      Timber.e(e, "setupCamera ERROR");
    }
    return null;
  }

  @Override public void release() {
    cameraCallback.close();
    super.release();
  }

  @Override public void toggleTorch() {
    if (flashCameraId == null) {
      Timber.e("No setupCamera with Flash");
      startErrorExplanationActivity();
    } else {
      Timber.d("Open setupCamera");
      final int result = cameraCallback.accessCamera(getAppContext(), flashCameraId);
      switch (result) {
        case TYPE_ERROR:
          startErrorExplanationActivity();
          break;
        case TYPE_PERMISSION:
          startPermissionExplanationActivity();
          break;
        default:
          Timber.d("Do nothing");
      }
    }
  }

  @SuppressWarnings("WeakerAccess") static final class CameraCallback
      extends CameraDevice.StateCallback {
    @NonNull final CameraManager manager;
    @NonNull final List<Surface> list;
    @NonNull private final CameraCommon cameraInterface;

    @Nullable CameraDevice cameraDevice;
    @Nullable SessionCallback session;

    @Nullable Size size;
    boolean opened;

    CameraCallback(@NonNull CameraCommon cameraInterface, @NonNull CameraManager manager) {
      this.cameraInterface = Checker.checkNonNull(cameraInterface);
      this.manager = Checker.checkNonNull(manager);
      opened = false;
      list = new ArrayList<>(1);
    }

    @CheckResult @NonNull
    static Size getSmallestSize(final @NonNull CameraManager manager, final @NonNull String id)
        throws CameraAccessException {
      Timber.d("Get stream config map");
      final StreamConfigurationMap map = manager.getCameraCharacteristics(id)
          .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      if (map == null) {
        throw new IllegalStateException("Camera " + id + "doesn't support any Stream Maps.");
      }

      Timber.d("Get possible output sizes");
      final Size[] outputSizes = map.getOutputSizes(SurfaceTexture.class);
      if (outputSizes == null || outputSizes.length == 0) {
        throw new IllegalStateException("Camera " + id + "doesn't support any outputSize.");
      }

      Timber.d("Select a size");
      Size chosen = outputSizes[0];
      for (final Size s : outputSizes) {
        if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
          chosen = s;
        }
      }
      return chosen;
    }

    void close() {
      // Surface texture is released by CameraManager so we don't have to
      if (opened) {
        if (session != null) {
          Timber.d("close SessionCallback");
          session.close();
          session = null;
        }

        if (cameraDevice != null) {
          Timber.d("close setupCamera device");
          cameraDevice.close();
          cameraDevice = null;
        }

        cameraInterface.notifyCallbackOnClosed();
      }
      opened = false;
    }

    @CheckResult int accessCamera(final @NonNull Context context, final @NonNull String id) {
      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
          == PackageManager.PERMISSION_GRANTED) {
        try {
          Timber.d("Has setupCamera permission, attempt to access");
          if (opened) {
            Timber.d("Close opened setupCamera");
            close();
          } else {
            Timber.d("Open closed setupCamera");
            manager.openCamera(id, this, null);
          }
          return TYPE_NONE;
        } catch (CameraAccessException e) {
          Timber.e(e, "toggleTorch ERROR");
          return TYPE_ERROR;
        }
      } else {
        Timber.e("Missing setupCamera permission");
        return TYPE_PERMISSION;
      }
    }

    @Override public void onOpened(@NonNull CameraDevice camera) {
      Timber.d("onOpened");
      opened = true;
      this.cameraDevice = camera;

      try {
        Timber.d("create capture builder");
        final CaptureRequest.Builder captureBuilder =
            camera.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
        captureBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);

        if (size == null) {
          Timber.d("get smallest size for texture");
          size = getSmallestSize(manager, camera.getId());
        } else {
          Timber.d("using cached smallest size");
        }
        // The setupCamera session recycles the surface texture, so we should not have to
        @SuppressLint("Recycle") final SurfaceTexture surfaceTexture = new SurfaceTexture(1);
        surfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());

        Timber.d("add surface to texture");
        final Surface surface = new Surface(surfaceTexture);
        if (list.isEmpty()) {
          list.add(0, surface);
        } else {
          list.set(0, surface);
        }

        Timber.d("add capture target");
        captureBuilder.addTarget(surface);

        Timber.d("create new session callback");
        session = new SessionCallback(captureBuilder.build());

        Timber.d("register capture session");
        camera.createCaptureSession(list, session, null);

        cameraInterface.notifyCallbackOnOpened();
      } catch (CameraAccessException e) {
        Timber.e(e, "onOpened");
      }
    }

    @Override public void onDisconnected(@NonNull CameraDevice cameraDevice) {
      this.cameraDevice = null;
      Timber.d("onDisconnected");
      opened = false;
    }

    @Override public void onError(@NonNull CameraDevice cameraDevice, int i) {
      this.cameraDevice = null;
      Timber.e("onError");
      opened = false;
    }
  }

  @SuppressWarnings("WeakerAccess") static final class SessionCallback
      extends CameraCaptureSession.StateCallback {

    @NonNull final CaptureRequest request;
    @Nullable CameraCaptureSession session;

    SessionCallback(@NonNull CaptureRequest request) {
      this.request = Checker.checkNonNull(request);
    }

    @Override public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
      Timber.d("Camera configured");
      session = cameraCaptureSession;
      try {
        Timber.d("set repeating");
        cameraCaptureSession.setRepeatingRequest(request, null, null);
      } catch (CameraAccessException e) {
        Timber.e(e, "onConfigured");
      }
    }

    @Override public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
      Timber.e("onConfigureFailed");
      session = cameraCaptureSession;
      close();
    }

    void close() {
      if (session != null) {
        Timber.d("close session");
        session.close();
        session = null;
      }
    }
  }
}
