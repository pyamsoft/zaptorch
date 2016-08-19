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

package com.pyamsoft.zaptorch.dagger.frag;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.base.Presenter;
import com.pyamsoft.pydroid.base.app.ApplicationPreferences;
import com.pyamsoft.zaptorch.app.frag.ConfirmationDialog;
import com.pyamsoft.zaptorch.app.service.VolumeMonitorService;
import javax.inject.Inject;
import javax.inject.Named;
import rx.Observable;
import rx.Scheduler;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

public final class MainFragmentPresenter extends Presenter<MainFragmentPresenter.MainFragmentView> {

  @NonNull private final MainFragmentInteractor interactor;
  @NonNull private final Scheduler mainScheduler;
  @NonNull private final Scheduler ioScheduler;

  @NonNull private Subscription confirmDialogBusSubscription = Subscriptions.empty();
  @NonNull private Subscription confirmDialogSubscription = Subscriptions.empty();

  @Nullable private ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener;

  @Inject MainFragmentPresenter(@NonNull MainFragmentInteractor interactor,
      @NonNull @Named("main") Scheduler mainScheduler,
      @NonNull @Named("io") Scheduler ioScheduler) {
    this.interactor = interactor;
    this.mainScheduler = mainScheduler;
    this.ioScheduler = ioScheduler;
  }

  @Override protected void onResume(@NonNull MainFragmentView view) {
    super.onResume(view);
    registerOnConfirmDialogBus();
    registerCameraApiListener();
  }

  @Override protected void onPause(@NonNull MainFragmentView view) {
    super.onPause(view);
    unregisterFromConfirmDialogBus();
    unregisterCameraApiListener();
  }

  @Override protected void onUnbind(@NonNull MainFragmentView view) {
    super.onUnbind(view);
    unsubscribeConfirmDialog();
  }

  void registerCameraApiListener() {
    unregisterCameraApiListener();
    cameraApiListener = new ApplicationPreferences.OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (interactor.getCameraApiKey().equals(key)) {
          Timber.d("Camera API has changed");
          VolumeMonitorService.changeCameraApi();
        }
      }
    };
    interactor.registerCameraApiListener(cameraApiListener);
  }

  void unregisterCameraApiListener() {
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

  void unregisterFromConfirmDialogBus() {
    if (!confirmDialogBusSubscription.isUnsubscribed()) {
      confirmDialogBusSubscription.unsubscribe();
    }
  }

  void registerOnConfirmDialogBus() {
    unregisterFromConfirmDialogBus();
    confirmDialogBusSubscription =
        ConfirmationDialog.ConfirmationDialogBus.get().register().subscribe(confirmationEvent -> {
          if (!confirmationEvent.isComplete()) {
            Timber.d("Received confirmation event!");
            // KLUDGE nested subscriptions are ugly
            unsubscribeConfirmDialog();
            Timber.d("Received all cleared confirmation event, clear All");
            confirmDialogSubscription = clearAll().subscribe(aBoolean -> {

                }, throwable -> Timber.e(throwable, "ConfirmationDialogBus in clearAll onError"),
                () -> {
                  Timber.d("ConfirmationDialogBus in clearAll onComplete");
                  // TODO post completed event
                  ConfirmationDialog.ConfirmationDialogBus.get()
                      .post(new ConfirmationDialog.ConfirmationEvent(true));
                });
          }
        }, throwable -> {
          Timber.e(throwable, "ConfirmationDialogBus onError");
        });
  }

  public void confirmSettingsClear() {
    getView().onConfirmAttempt();
  }

  public interface MainFragmentView {

    void onConfirmAttempt();
  }
}
