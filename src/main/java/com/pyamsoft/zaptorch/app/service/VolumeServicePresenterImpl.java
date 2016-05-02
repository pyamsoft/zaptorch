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

package com.pyamsoft.zaptorch.app.service;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.base.PresenterImplBase;
import com.pyamsoft.zaptorch.app.service.camera.CameraInterface;
import com.pyamsoft.zaptorch.app.service.camera.LollipopCamera;
import com.pyamsoft.zaptorch.app.service.camera.MarshmallowCamera;
import com.pyamsoft.zaptorch.app.service.camera.OriginalCamera;
import javax.inject.Inject;
import timber.log.Timber;

public final class VolumeServicePresenterImpl extends PresenterImplBase<VolumeServiceProvider>
    implements VolumeServicePresenter {

  private static final String TAG = VolumeServicePresenterImpl.class.getSimpleName();
  @NonNull private final Handler handler;
  @NonNull private final VolumeServiceInteractor interactor;
  @NonNull private final Context appContext;

  private CameraInterface cameraInterface;
  private boolean pressed;
  private boolean running;

  @Inject public VolumeServicePresenterImpl(@NonNull final Context context,
      @NonNull final VolumeServiceInteractor interactor) {
    this.appContext = context.getApplicationContext();
    this.interactor = interactor;
    this.handler = new Handler();
    this.pressed = false;
  }

  private void handleKeyEvent() {
    handler.removeCallbacksAndMessages(null);
    if (pressed) {
      Timber.d("Key has been double pressed");
      pressed = false;
      cameraInterface.toggleTorch();
    } else {
      pressed = true;
      final long delay = interactor.getButtonDelayTime();
      Timber.d("Post back to false after delay: %d", delay);
      handler.postDelayed(() -> {
        Timber.d("Set pressed back to false");
        pressed = false;
      }, delay);
    }
  }

  @Override public void handleKeyEvent(int action, int keyCode) {
    if (action == KeyEvent.ACTION_UP) {
      switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
          Timber.d("onKeyEvent: %s", KeyEvent.keyCodeToString(keyCode));
          handleKeyEvent();
          break;
        default:
      }
    }
  }

  @Override public boolean shouldShowErrorDialog() {
    return interactor.shouldShowErrorDialog();
  }

  @Override public void bind(@NonNull VolumeServiceProvider view) {
    super.bind(view);
    pressed = false;
    running = true;

    if (cameraInterface != null) {
      cameraInterface.release();
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      cameraInterface = new MarshmallowCamera(appContext);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      cameraInterface = new LollipopCamera(appContext);
    } else {
      cameraInterface = new OriginalCamera(appContext);
    }

    cameraInterface.setPresenter(this);
  }

  @Override public void unbind() {
    super.unbind();
    Timber.d("Unbind");
    if (cameraInterface != null) {
      cameraInterface.release();
      cameraInterface.setPresenter(null);
      cameraInterface = null;
    }

    handler.removeCallbacksAndMessages(null);
    pressed = false;
    running = false;
  }

  @Override public boolean isStarted() {
    return running;
  }
}
