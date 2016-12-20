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

package com.pyamsoft.zaptorchpresenter.service;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.presenter.PresenterBase;
import com.pyamsoft.pydroid.tool.ExecutedOffloader;
import com.pyamsoft.pydroid.tool.OffloaderHelper;
import timber.log.Timber;

class VolumeServicePresenterImpl extends PresenterBase<VolumeServicePresenter.VolumeServiceView>
    implements VolumeServicePresenter, CameraInterface.OnStateChangedCallback {

  @SuppressWarnings("WeakerAccess") @NonNull final Handler handler;
  @NonNull private final VolumeServiceInteractor interactor;
  @SuppressWarnings("WeakerAccess") boolean pressed;
  @SuppressWarnings("WeakerAccess") @Nullable ExecutedOffloader delaySubscription;
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
      toggleTorch();
    } else {
      pressed = true;
      OffloaderHelper.cancel(delaySubscription);
      delaySubscription = interactor.getButtonDelayTime()
          .onResult(delay -> {
            Timber.d("Post back to false after delay: %d", delay);
            handler.postDelayed(() -> {
              Timber.d("Set pressed back to false");
              pressed = false;
            }, delay);
          })
          .onError(throwable -> Timber.e(throwable, "onError handleKeyEvent"))
          .onFinish(() -> OffloaderHelper.cancel(delaySubscription))
          .execute();
    }
  }

  @Override public void setTorchOffServiceClass(@NonNull Intent torchOff) {
    interactor.setTorchOffServiceClass(torchOff);
  }

  @Override public void toggleTorch() {
    if (cameraInterface != null) {
      cameraInterface.toggleTorch();
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
    cameraInterface.setOnStateChangedCallback(this);
  }

  @Override protected void onUnbind() {
    super.onUnbind();
    Timber.d("Unbind");
    if (cameraInterface != null) {
      cameraInterface.release();
      cameraInterface = null;
    }
    OffloaderHelper.cancel(delaySubscription);
    handler.removeCallbacksAndMessages(null);
    pressed = false;
  }

  @Override public void onOpened() {
    interactor.onOpened();
  }

  @Override public void onClosed() {
    interactor.onClosed();
  }

  @Override public void onError(@NonNull Intent errorIntent) {
    getView(volumeServiceView -> volumeServiceView.onCameraOpenError(errorIntent));
  }
}
