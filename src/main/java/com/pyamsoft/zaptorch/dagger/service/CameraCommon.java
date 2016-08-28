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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.pyamsoft.zaptorch.app.service.camera.CameraInterface;
import com.pyamsoft.zaptorch.app.service.error.CameraErrorExplanation;

abstract class CameraCommon implements CameraInterface {

  @NonNull final VolumeServiceInteractor interactor;
  @NonNull final Context appContext;
  @NonNull final Handler handler;
  @NonNull final Intent errorExplain;
  @NonNull final Intent permissionExplain;

  CameraCommon(final @NonNull Context context, final @NonNull VolumeServiceInteractor interactor) {
    this.appContext = context.getApplicationContext();
    this.interactor = interactor;
    handler = new Handler(Looper.getMainLooper());
    errorExplain = new Intent(appContext, CameraErrorExplanation.class);
    errorExplain.putExtra(CameraErrorExplanation.DIALOG_WHICH, CameraErrorExplanation.TYPE_ERROR);
    errorExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    permissionExplain = new Intent(appContext, CameraErrorExplanation.class);
    permissionExplain.putExtra(CameraErrorExplanation.DIALOG_WHICH,
        CameraErrorExplanation.TYPE_PERMISSION);
    permissionExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  }

  void startErrorExplanationActivity() {
    final boolean show = interactor.shouldShowErrorDialog();
    if (show) {
      handler.post(() -> appContext.startActivity(errorExplain));
    }
  }

  void startPermissionExplanationActivity() {
    handler.post(() -> appContext.startActivity(permissionExplain));
  }

  @CheckResult @NonNull Context getAppContext() {
    return appContext;
  }
}
