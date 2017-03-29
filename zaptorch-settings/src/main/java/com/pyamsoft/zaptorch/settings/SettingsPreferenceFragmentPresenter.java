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

package com.pyamsoft.zaptorch.settings;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.app.OnRegisteredSharedPreferenceChangeListener;
import com.pyamsoft.pydroid.bus.EventBus;
import com.pyamsoft.pydroid.helper.Checker;
import com.pyamsoft.pydroid.helper.DisposableHelper;
import com.pyamsoft.pydroid.presenter.Presenter;
import com.pyamsoft.pydroid.presenter.SchedulerPresenter;
import com.pyamsoft.zaptorch.model.ConfirmEvent;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import timber.log.Timber;

class SettingsPreferenceFragmentPresenter extends SchedulerPresenter<Presenter.Empty> {

  @SuppressWarnings("WeakerAccess") @NonNull final SettingsPreferenceFragmentInteractor interactor;
  @NonNull private Disposable clearAllDisposable = Disposables.empty();
  @Nullable private OnRegisteredSharedPreferenceChangeListener cameraApiListener;

  SettingsPreferenceFragmentPresenter(@NonNull SettingsPreferenceFragmentInteractor interactor,
      @NonNull Scheduler observeScheduler, @NonNull Scheduler subscribeScheduler) {
    super(observeScheduler, subscribeScheduler);
    this.interactor = Checker.checkNonNull(interactor);
  }

  @Override protected void onUnbind() {
    super.onUnbind();
    unregisterCameraApiListener();
    clearAllDisposable = DisposableHelper.dispose(clearAllDisposable);
  }

  private void registerCameraApiListener() {
    unregisterCameraApiListener();
    interactor.registerCameraApiListener(cameraApiListener);
  }

  private void unregisterCameraApiListener() {
    interactor.unregisterCameraApiListener(cameraApiListener);
  }

  public void registerEventBus(@NonNull ClearRequestCallback callback) {
    ClearRequestCallback requestCallback = Checker.checkNonNull(callback);
    clearAllDisposable = DisposableHelper.dispose(clearAllDisposable);
    clearAllDisposable = EventBus.get()
        .listen(ConfirmEvent.class)
        .flatMap(event -> interactor.clearAll())
        .subscribeOn(getSubscribeScheduler())
        .observeOn(getObserveScheduler())
        .subscribe(aBoolean -> requestCallback.onClearAll(),
            throwable -> Timber.e(throwable, "onError event bus"));
  }

  public void listenForCameraChanges(@NonNull CameraChangeCallback callback) {
    if (cameraApiListener == null) {
      this.cameraApiListener = new OnRegisteredSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
          if (interactor.getCameraApiKey().equals(key)) {
            Timber.d("Camera API has changed");
            Checker.checkNonNull(callback).onCameraApiChanged();
          }
        }
      };
    }
    registerCameraApiListener();
  }

  interface CameraChangeCallback {

    void onCameraApiChanged();
  }

  interface ClearRequestCallback {

    void onClearAll();
  }
}
