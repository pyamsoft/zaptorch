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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.tool.AsyncOffloader;
import com.pyamsoft.pydroid.tool.ExecutedOffloader;
import com.pyamsoft.pydroid.tool.OffloaderHelper;
import timber.log.Timber;

abstract class CameraCommon implements CameraInterface {

  @SuppressWarnings("WeakerAccess") @NonNull final Context appContext;
  @SuppressWarnings("WeakerAccess") @NonNull final Intent errorExplain;
  @SuppressWarnings("WeakerAccess") @NonNull final Intent permissionExplain;
  @NonNull private final VolumeServiceInteractor interactor;
  @SuppressWarnings("WeakerAccess") @Nullable ExecutedOffloader errorSubscription;
  @SuppressWarnings("WeakerAccess") @Nullable ExecutedOffloader permissionSubscription;
  @Nullable private OnStateChangedCallback callback;

  CameraCommon(final @NonNull Context context, final @NonNull VolumeServiceInteractor interactor) {
    this.appContext = context.getApplicationContext();
    this.interactor = interactor;
    errorExplain = new Intent();
    errorExplain.putExtra(DIALOG_WHICH, TYPE_ERROR);
    errorExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    permissionExplain = new Intent();
    permissionExplain.putExtra(DIALOG_WHICH, TYPE_PERMISSION);
    permissionExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  }

  void startErrorExplanationActivity() {
    OffloaderHelper.cancel(errorSubscription);
    errorSubscription = interactor.shouldShowErrorDialog()
        .onResult(show -> {
          if (show) {
            notifyCallbackOnError(errorExplain);
          }
        })
        .onError(throwable -> Timber.e(throwable, "onError startErrorExplanationActivity"))
        .onFinish(() -> OffloaderHelper.cancel(errorSubscription))
        .execute();
  }

  void startPermissionExplanationActivity() {
    OffloaderHelper.cancel(permissionSubscription);
    permissionSubscription = AsyncOffloader.newInstance(() -> Boolean.TRUE)
        .onResult(show -> {
          if (show) {
            notifyCallbackOnError(permissionExplain);
          }
        })
        .onError(throwable -> Timber.e(throwable, "onError startPermissionExplanationActivity"))
        .onFinish(() -> OffloaderHelper.cancel(permissionSubscription))
        .execute();
  }

  @CheckResult @NonNull Context getAppContext() {
    return appContext;
  }

  @Override public void setOnStateChangedCallback(@NonNull OnStateChangedCallback callback) {
    this.callback = callback;
  }

  void notifyCallbackOnOpened() {
    if (callback != null) {
      callback.onOpened();
    }
  }

  void notifyCallbackOnClosed() {
    if (callback != null) {
      callback.onClosed();
    }
  }

  private void notifyCallbackOnError(@NonNull Intent errorIntent) {
    if (callback != null) {
      callback.onError(errorIntent);
    }
  }

  @CallSuper @Override public void release() {
    OffloaderHelper.cancel(errorSubscription);
    callback = null;
  }
}
