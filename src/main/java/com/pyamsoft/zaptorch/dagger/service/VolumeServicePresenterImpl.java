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

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.presenter.PresenterBase;
import com.pyamsoft.zaptorch.app.service.VolumeServicePresenter;
import com.pyamsoft.zaptorch.app.service.camera.CameraInterface;
import timber.log.Timber;

class VolumeServicePresenterImpl extends PresenterBase<VolumeServicePresenter.VolumeServiceView>
    implements VolumeServicePresenter {

  @NonNull private final Handler handler;
  @NonNull private final VolumeServiceInteractor interactor;
  @SuppressWarnings("WeakerAccess") boolean pressed;
  @Nullable private CameraInterface cameraInterface;

  VolumeServicePresenterImpl(@NonNull final VolumeServiceInteractor interactor) {
    this.handler = new Handler();
    this.interactor = interactor;
    this.pressed = false;
  }

  @SuppressWarnings("WeakerAccess") void handleKeyEvent() {
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

  @Override protected void onBind() {
    super.onBind();
    pressed = false;
    cameraInterface = interactor.camera();
  }

  @Override protected void onUnbind() {
    super.onUnbind();
    Timber.d("Unbind");
    if (cameraInterface != null) {
      cameraInterface.release();
      cameraInterface = null;
    }
    handler.removeCallbacksAndMessages(null);
    pressed = false;
  }
}
