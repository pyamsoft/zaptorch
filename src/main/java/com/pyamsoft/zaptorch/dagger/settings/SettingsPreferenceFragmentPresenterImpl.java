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

package com.pyamsoft.zaptorch.dagger.settings;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import com.pyamsoft.pydroid.app.ApplicationPreferences;
import com.pyamsoft.pydroid.presenter.PresenterBase;
import com.pyamsoft.pydroid.tool.ExecutedOffloader;
import com.pyamsoft.zaptorch.app.service.VolumeMonitorService;
import com.pyamsoft.zaptorch.app.settings.SettingsPreferenceFragmentPresenter;
import timber.log.Timber;

class SettingsPreferenceFragmentPresenterImpl
    extends PresenterBase<SettingsPreferenceFragmentPresenter.MainFragmentView>
    implements SettingsPreferenceFragmentPresenter {

  @SuppressWarnings("WeakerAccess") @NonNull final SettingsPreferenceFragmentInteractor interactor;
  @Nullable private ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener;
  @NonNull private ExecutedOffloader clearAllEvent = new ExecutedOffloader.Empty();

  SettingsPreferenceFragmentPresenterImpl(
      @NonNull SettingsPreferenceFragmentInteractor interactor) {
    this.interactor = interactor;
  }

  @Override protected void onBind() {
    super.onBind();
    registerCameraApiListener();
  }

  @Override protected void onUnbind() {
    super.onUnbind();
    unregisterCameraApiListener();
    unsubscribeConfirmDialog();
  }

  @SuppressWarnings("WeakerAccess") @VisibleForTesting void registerCameraApiListener() {
    unregisterCameraApiListener();
    cameraApiListener = new ApplicationPreferences.OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (interactor.getCameraApiKey().equals(key)) {
          Timber.d("Camera API has changed");
          if (VolumeMonitorService.isRunning()) {
            VolumeMonitorService.changeCameraApi();
          }
        }
      }
    };
    interactor.registerCameraApiListener(cameraApiListener);
  }

  private void unregisterCameraApiListener() {
    if (cameraApiListener != null) {
      interactor.unregisterCameraApiListener(cameraApiListener);
      cameraApiListener = null;
    }
  }

  private void unsubscribeConfirmDialog() {
    if (!clearAllEvent.isCancelled()) {
      clearAllEvent.cancel();
    }
  }

  @Override public void confirmSettingsClear() {
    getView(MainFragmentView::onConfirmAttempt);
  }

  @Override public void processClearRequest() {
    unsubscribeConfirmDialog();
    Timber.d("Received all cleared confirmation event, clear All");
    clearAllEvent = interactor.clearAll()
        .onError(throwable -> Timber.e(throwable, "onError clearAll"))
        .onResult(item -> getView(MainFragmentView::onClearAll))
        .execute();
  }
}
