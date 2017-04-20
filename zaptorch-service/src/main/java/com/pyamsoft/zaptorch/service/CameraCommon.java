/*
 * Copyright 2017 Peter Kenji Yamanaka
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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.helper.Checker;
import com.pyamsoft.pydroid.presenter.SchedulerPresenter;
import io.reactivex.Scheduler;
import timber.log.Timber;

abstract class CameraCommon extends SchedulerPresenter implements CameraInterface {

  @SuppressWarnings("WeakerAccess") @NonNull final Context appContext;
  @SuppressWarnings("WeakerAccess") @NonNull final Intent errorExplain;
  @SuppressWarnings("WeakerAccess") @NonNull final Intent permissionExplain;
  @NonNull private final VolumeServiceInteractor interactor;
  @Nullable private OnStateChangedCallback callback;

  CameraCommon(final @NonNull Context context, @NonNull VolumeServiceInteractor interactor,
      @NonNull Scheduler obsScheduler, @NonNull Scheduler subScheduler) {
    super(obsScheduler, subScheduler);
    this.appContext = Checker.checkNonNull(context).getApplicationContext();
    this.interactor = Checker.checkNonNull(interactor);
    errorExplain = new Intent();
    errorExplain.putExtra(DIALOG_WHICH, TYPE_ERROR);
    errorExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    permissionExplain = new Intent();
    permissionExplain.putExtra(DIALOG_WHICH, TYPE_PERMISSION);
    permissionExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  }

  void startErrorExplanationActivity() {
    disposeOnStop(interactor.shouldShowErrorDialog()
        .subscribeOn(getSubscribeScheduler())
        .observeOn(getObserveScheduler())
        .subscribe(show -> {
          if (show) {
            notifyCallbackOnError(errorExplain);
          }
        }, throwable -> Timber.e(throwable, "onError startErrorExplanationActivity")));
  }

  void startPermissionExplanationActivity() {
    notifyCallbackOnError(permissionExplain);
  }

  @CheckResult @NonNull Context getAppContext() {
    return appContext;
  }

  void setOnStateChangedCallback(@Nullable OnStateChangedCallback callback) {
    this.callback = callback;
  }

  void notifyCallbackOnOpened() {
    if (callback != null) {
      Timber.d("Notify callback: opened");
      callback.onOpened();
    }
  }

  void notifyCallbackOnClosed() {
    if (callback != null) {
      Timber.d("Notify callback: closed");
      callback.onClosed();
    }
  }

  @SuppressWarnings("WeakerAccess") void notifyCallbackOnError(@NonNull Intent errorIntent) {
    if (callback != null) {
      Timber.w("Notify callback: error");
      callback.onError(errorIntent);
    }
  }

  abstract void release();

  abstract void toggleTorch();

  interface OnStateChangedCallback {

    void onOpened();

    void onClosed();

    void onError(@NonNull Intent errorIntent);
  }
}
