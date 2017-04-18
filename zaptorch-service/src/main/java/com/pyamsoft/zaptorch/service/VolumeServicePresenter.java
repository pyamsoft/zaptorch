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
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.bus.EventBus;
import com.pyamsoft.pydroid.helper.Checker;
import com.pyamsoft.pydroid.presenter.SchedulerPresenter;
import com.pyamsoft.zaptorch.model.ServiceEvent;
import io.reactivex.Scheduler;
import timber.log.Timber;

class VolumeServicePresenter extends SchedulerPresenter {

  @NonNull private final VolumeServiceInteractor interactor;

  VolumeServicePresenter(@NonNull VolumeServiceInteractor interactor,
      @NonNull Scheduler observeScheduler, @NonNull Scheduler subscribeScheduler) {
    super(observeScheduler, subscribeScheduler);
    this.interactor = Checker.checkNonNull(interactor);
  }

  @Override protected void onStop() {
    super.onStop();
    interactor.releaseCamera();
  }

  /**
   * public
   */
  void toggleTorch() {
    interactor.toggleTorch();
  }

  /**
   * public
   */
  void handleKeyEvent(int action, int keyCode) {
    disposeOnStop(interactor.handleKeyPress(action, keyCode)
        .subscribeOn(getSubscribeScheduler())
        .observeOn(getObserveScheduler())
        .subscribe(time -> Timber.d("Set back after %d delay", time),
            throwable -> Timber.e(throwable, "onError handleKeyEvent")));
  }

  /**
   * public
   */
  void setupCamera(@NonNull VolumeServiceView view) {
    interactor.setupCamera(intent -> {
      VolumeServiceView cameraView = Checker.checkNonNull(view);
      cameraView.onCameraOpenError(intent);
    }, getObserveScheduler(), getSubscribeScheduler());
  }

  /**
   * public
   */
  void registerOnBus(@NonNull ServiceCallback callback) {
    ServiceCallback serviceCallback = Checker.checkNonNull(callback);
    disposeOnDestroy(EventBus.get()
        .listen(ServiceEvent.class)
        .subscribeOn(getSubscribeScheduler())
        .observeOn(getObserveScheduler())
        .subscribe(serviceEvent -> {
          switch (serviceEvent.type()) {
            case FINISH:
              serviceCallback.onFinishService();
              break;
            case TORCH:
              serviceCallback.onToggleTorch();
              break;
            case CHANGE_CAMERA:
              serviceCallback.onChangeCameraApi();
              break;
            default:
              throw new IllegalArgumentException(
                  "Invalid ServiceEvent.Type: " + serviceEvent.type());
          }
        }, throwable -> Timber.e(throwable, "onError event bus")));
  }

  interface ServiceCallback {

    void onToggleTorch();

    void onFinishService();

    void onChangeCameraApi();
  }

  interface VolumeServiceView {

    void onCameraOpenError(@NonNull Intent errorIntent);
  }
}
