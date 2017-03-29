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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.helper.Checker;
import com.pyamsoft.pydroid.helper.DisposableHelper;
import com.pyamsoft.pydroid.helper.SchedulerHelper;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;

abstract class CameraCommon implements CameraInterface {

  @SuppressWarnings("WeakerAccess") @NonNull final Context appContext;
  @SuppressWarnings("WeakerAccess") @NonNull final Intent errorExplain;
  @SuppressWarnings("WeakerAccess") @NonNull final Intent permissionExplain;
  @NonNull private final VolumeServiceInteractor interactor;
  @NonNull private final Scheduler obsScheduler;
  @NonNull private final Scheduler subScheduler;
  @NonNull private Disposable errorDisposable = Disposables.empty();
  @Nullable private OnStateChangedCallback callback;

  CameraCommon(final @NonNull Context context, @NonNull VolumeServiceInteractor interactor,
      @NonNull Scheduler obsScheduler, @NonNull Scheduler subScheduler) {
    this.appContext = Checker.checkNonNull(context).getApplicationContext();
    this.interactor = Checker.checkNonNull(interactor);
    this.obsScheduler = obsScheduler;
    this.subScheduler = subScheduler;
    errorExplain = new Intent();
    errorExplain.putExtra(DIALOG_WHICH, TYPE_ERROR);
    errorExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    permissionExplain = new Intent();
    permissionExplain.putExtra(DIALOG_WHICH, TYPE_PERMISSION);
    permissionExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    SchedulerHelper.enforceObserveScheduler(obsScheduler);
    SchedulerHelper.enforceSubscribeScheduler(subScheduler);
  }

  @NonNull @CheckResult Scheduler getSubScheduler() {
    return subScheduler;
  }

  @NonNull @CheckResult Scheduler getObsScheduler() {
    return obsScheduler;
  }

  void startErrorExplanationActivity() {
    errorDisposable = DisposableHelper.dispose(errorDisposable);
    errorDisposable = interactor.shouldShowErrorDialog()
        .subscribeOn(subScheduler)
        .observeOn(obsScheduler)
        .subscribe(show -> {
              if (show) {
                notifyCallbackOnError(errorExplain);
              }
            }, throwable -> Timber.e(throwable, "onError startErrorExplanationActivity"),
            () -> DisposableHelper.dispose(errorDisposable));
  }

  void startPermissionExplanationActivity() {
    notifyCallbackOnError(permissionExplain);
  }

  @CheckResult @NonNull Context getAppContext() {
    return appContext;
  }

  @Override public void setOnStateChangedCallback(@Nullable OnStateChangedCallback callback) {
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

  @CallSuper @Override public void release() {
    errorDisposable = DisposableHelper.dispose(errorDisposable);
  }
}
