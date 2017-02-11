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
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.helper.SubscriptionHelper;
import com.pyamsoft.pydroid.presenter.SchedulerPresenter;
import rx.Scheduler;
import rx.Subscription;
import timber.log.Timber;

class VolumeServicePresenter extends SchedulerPresenter<VolumeServicePresenter.VolumeServiceView> {

  @SuppressWarnings("WeakerAccess") @NonNull final VolumeServiceInteractor interactor;
  @SuppressWarnings("WeakerAccess") @Nullable Subscription keySubscription;

  VolumeServicePresenter(@NonNull VolumeServiceInteractor interactor,
      @NonNull Scheduler observeScheduler, @NonNull Scheduler subscribeScheduler) {
    super(observeScheduler, subscribeScheduler);
    this.interactor = interactor;
  }

  public void toggleTorch() {
    interactor.toggleTorch();
  }

  public void handleKeyEvent(int action, int keyCode) {
    SubscriptionHelper.unsubscribe(keySubscription);
    keySubscription = interactor.handleKeyPress(action, keyCode)
        .subscribeOn(getSubscribeScheduler())
        .observeOn(getObserveScheduler())
        .subscribe(time -> Timber.d("Set back after %d delay", time),
            throwable -> Timber.e(throwable, "onError handleKeyEvent"),
            () -> SubscriptionHelper.unsubscribe(keySubscription));
  }

  @Override protected void onBind(@Nullable VolumeServiceView view) {
    super.onBind(view);
    interactor.setupCamera(
        intent -> ifViewExists(volumeServiceView -> volumeServiceView.onCameraOpenError(intent)),
        getObserveScheduler(), getSubscribeScheduler());
  }

  @Override protected void onUnbind() {
    super.onUnbind();
    Timber.d("Unbind");
    interactor.releaseCamera();
    SubscriptionHelper.unsubscribe(keySubscription);
  }

  interface VolumeServiceView {

    void onCameraOpenError(@NonNull Intent errorIntent);
  }
}
