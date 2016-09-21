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
import com.pyamsoft.pydroid.ApplicationPreferences;
import com.pyamsoft.pydroid.presenter.PresenterBase;
import com.pyamsoft.zaptorch.app.service.VolumeMonitorService;
import com.pyamsoft.zaptorch.app.settings.SettingsPreferenceFragmentPresenter;
import com.pyamsoft.zaptorch.bus.ConfirmationDialogBus;
import com.pyamsoft.zaptorch.model.event.ConfirmationEvent;
import javax.inject.Inject;
import javax.inject.Named;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

class SettingsPreferenceFragmentPresenterImpl
    extends PresenterBase<SettingsPreferenceFragmentPresenter.MainFragmentView>
    implements SettingsPreferenceFragmentPresenter {

  @NonNull final SettingsPreferenceFragmentInteractor interactor;
  @NonNull final Scheduler mainScheduler;
  @NonNull final Scheduler ioScheduler;

  @NonNull Subscription confirmDialogBusSubscription = Subscriptions.empty();
  @NonNull Subscription confirmDialogSubscription = Subscriptions.empty();

  @Nullable ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener;

  @Inject SettingsPreferenceFragmentPresenterImpl(
      @NonNull SettingsPreferenceFragmentInteractor interactor,
      @NonNull @Named("main") Scheduler mainScheduler,
      @NonNull @Named("io") Scheduler ioScheduler) {
    this.interactor = interactor;
    this.mainScheduler = mainScheduler;
    this.ioScheduler = ioScheduler;
  }

  @Override protected void onBind() {
    super.onBind();
    registerOnConfirmDialogBus();
    registerCameraApiListener();
  }

  @Override protected void onUnbind() {
    super.onUnbind();
    unregisterFromConfirmDialogBus();
    unregisterCameraApiListener();
    unsubscribeConfirmDialog();
  }

  @VisibleForTesting void registerCameraApiListener() {
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

  void unsubscribeConfirmDialog() {
    if (confirmDialogSubscription.isUnsubscribed()) {
      confirmDialogSubscription.unsubscribe();
    }
  }

  Observable<Boolean> clearAll() {
    return interactor.clearAll().subscribeOn(ioScheduler).observeOn(mainScheduler);
  }

  private void unregisterFromConfirmDialogBus() {
    if (!confirmDialogBusSubscription.isUnsubscribed()) {
      confirmDialogBusSubscription.unsubscribe();
    }
  }

  @VisibleForTesting void registerOnConfirmDialogBus() {
    unregisterFromConfirmDialogBus();
    confirmDialogBusSubscription =
        ConfirmationDialogBus.get().register().subscribe(confirmationEvent -> {
          if (!confirmationEvent.complete()) {
            Timber.d("Received confirmation event!");
            // KLUDGE nested subscriptions are ugly
            unsubscribeConfirmDialog();
            Timber.d("Received all cleared confirmation event, clear All");
            confirmDialogSubscription = clearAll().subscribe(aBoolean -> {
                  // Do nothing
                }, throwable -> Timber.e(throwable, "ConfirmationDialogBus in clearAll onError"),
                () -> {
                  Timber.d("ConfirmationDialogBus in clearAll onComplete");
                  // TODO post completed event
                  ConfirmationDialogBus.get().post(ConfirmationEvent.create(true));
                });
          }
        }, throwable -> {
          Timber.e(throwable, "ConfirmationDialogBus onError");
        });
  }

  @Override public void confirmSettingsClear() {
    getView(MainFragmentView::onConfirmAttempt);
  }
}
