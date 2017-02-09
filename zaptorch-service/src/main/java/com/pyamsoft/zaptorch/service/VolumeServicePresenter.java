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

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.presenter.Presenter;
import com.pyamsoft.pydroid.tool.ExecutedOffloader;
import com.pyamsoft.pydroid.tool.OffloaderHelper;
import timber.log.Timber;

class VolumeServicePresenter extends Presenter<VolumeServicePresenter.VolumeServiceView> {

  @SuppressWarnings("WeakerAccess") @NonNull final Handler handler;
  @SuppressWarnings("WeakerAccess") @NonNull final VolumeServiceInteractor interactor;
  @SuppressWarnings("WeakerAccess") boolean pressed;
  @SuppressWarnings("WeakerAccess") @Nullable ExecutedOffloader delaySubscription;

  VolumeServicePresenter(@NonNull final VolumeServiceInteractor interactor) {
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

  public void toggleTorch() {
    interactor.toggleTorch();
  }

  public void handleKeyEvent(int action, int keyCode) {
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

  @Override protected void onBind(@Nullable VolumeServiceView view) {
    super.onBind(view);
    pressed = false;
    interactor.setupCamera(
        intent -> ifViewExists(volumeServiceView -> volumeServiceView.onCameraOpenError(intent)));
  }

  @Override protected void onUnbind() {
    super.onUnbind();
    Timber.d("Unbind");
    interactor.releaseCamera();
    OffloaderHelper.cancel(delaySubscription);
    handler.removeCallbacksAndMessages(null);
    pressed = false;
  }

  interface VolumeServiceView {

    void onCameraOpenError(@NonNull Intent errorIntent);
  }
}
