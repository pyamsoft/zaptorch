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

package com.pyamsoft.zaptorch.dagger.main;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.dagger.presenter.PresenterBase;
import com.pyamsoft.zaptorch.app.main.MainPresenter;
import com.pyamsoft.zaptorch.bus.ConfirmationDialogBus;
import javax.inject.Inject;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

class MainPresenterImpl extends PresenterBase<MainPresenter.MainActivityView>
    implements MainPresenter {

  @NonNull final MainInteractor interactor;
  @NonNull Subscription confirmDialogBusSubscription = Subscriptions.empty();

  @Inject MainPresenterImpl(@NonNull MainInteractor interactor) {
    this.interactor = interactor;
  }

  @Override protected void onBind(@NonNull MainActivityView view) {
    super.onBind(view);
    registerOnConfirmDialogBus(view);
  }

  @Override protected void onUnbind() {
    super.onUnbind();
    unregisterFromConfirmDialogBus();
  }

  @Override @CheckResult public boolean shouldHandleKeycode(int keyCode) {
    boolean handled;
    switch (keyCode) {
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        Timber.d("Detected a Volume Down event.");
        handled = true;
        break;
      case KeyEvent.KEYCODE_VOLUME_UP:
        Timber.d("Detected a Volume Up event.");
        handled = true;
        break;
      default:
        handled = false;
    }
    return interactor.shouldHandleKeys() && handled;
  }

  void registerOnConfirmDialogBus(@NonNull MainActivityView view) {
    unregisterFromConfirmDialogBus();
    confirmDialogBusSubscription =
        ConfirmationDialogBus.get().register().subscribe(confirmationEvent -> {
          if (confirmationEvent.complete()) {
            view.onClearAll();
          }
        }, throwable -> {
          Timber.e(throwable, "ConfirmationDialogBus onError");
        });
  }

  void unregisterFromConfirmDialogBus() {
    if (!confirmDialogBusSubscription.isUnsubscribed()) {
      confirmDialogBusSubscription.unsubscribe();
    }
  }
}
