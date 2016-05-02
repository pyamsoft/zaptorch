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

package com.pyamsoft.zaptorch.app.service.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.zaptorch.app.service.VolumeServicePresenter;
import com.pyamsoft.zaptorch.app.service.error.CameraErrorExplanation;

abstract class CameraCommon implements CameraInterface {

  private final Context appContext;
  private final Handler handler;
  private final Intent errorExplain;
  private VolumeServicePresenter presenter;

  CameraCommon(final @NonNull Context context) {
    this.appContext = context.getApplicationContext();
    handler = new Handler(Looper.getMainLooper());
    errorExplain = new Intent(appContext, CameraErrorExplanation.class);
    errorExplain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  }

  void startErrorExplanationActivity() {
    if (presenter != null && presenter.shouldShowErrorDialog()) {
      handler.post(() -> appContext.startActivity(errorExplain));
    }
  }

  Context getAppContext() {
    return appContext;
  }

  public void setPresenter(final @Nullable VolumeServicePresenter presenter) {
    this.presenter = presenter;
  }
}
