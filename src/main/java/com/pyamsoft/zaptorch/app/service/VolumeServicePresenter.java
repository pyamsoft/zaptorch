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

import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.base.Presenter;
import com.pyamsoft.zaptorch.app.service.camera.CameraInterface;
import com.pyamsoft.zaptorch.dagger.service.VolumeServiceInteractor;
import javax.inject.Inject;
import timber.log.Timber;

public final class VolumeServicePresenter
    extends Presenter<VolumeServicePresenter.VolumeServiceView> {

  @NonNull private final Handler handler;
  @NonNull private final VolumeServiceInteractor interactor;
  private final int cameraApiOld;
  private final int cameraApiLollipop;
  private final int cameraApiMarshmallow;

  @Nullable private CameraInterface cameraInterface;
  private boolean pressed;

  @Inject public VolumeServicePresenter(@NonNull final VolumeServiceInteractor interactor) {
    this.handler = new Handler();
    this.interactor = interactor;
    this.pressed = false;

    // KLUDGE duplication of values between preferences and java code
    cameraApiOld = 0;
    cameraApiLollipop = 1;
    cameraApiMarshmallow = 2;
  }

  private void handleKeyEvent() {
    handler.removeCallbacksAndMessages(null);
    if (pressed) {
      Timber.d("Key has been double pressed");
      pressed = false;
      if (cameraInterface != null) {
        cameraInterface.toggleTorch();
      }
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

  public final void handleKeyEvent(int action, int keyCode) {
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

  @Override protected void onBind(@NonNull VolumeServiceView view) {
    super.onBind(view);
    pressed = false;

    final int cameraApi = interactor.getCameraApi();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraApi == cameraApiMarshmallow) {
      cameraInterface = interactor.marshmallowCamera();
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && cameraApi == cameraApiLollipop) {
      cameraInterface = interactor.lollipopCamera();
    } else if (cameraApi == cameraApiOld) {
      cameraInterface = interactor.originalCamera();
    } else {
      throw new RuntimeException("Invalid Camera API selected: " + cameraApi);
    }
  }

  @Override protected void onUnbind(@NonNull VolumeServiceView view) {
    super.onUnbind(view);
    Timber.d("Unbind");
    if (cameraInterface != null) {
      cameraInterface.release();
      cameraInterface = null;
    }
    handler.removeCallbacksAndMessages(null);
    pressed = false;
  }

  public interface VolumeServiceView {
  }
}
