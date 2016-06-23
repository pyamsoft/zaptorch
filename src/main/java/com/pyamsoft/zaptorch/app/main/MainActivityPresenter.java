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

package com.pyamsoft.zaptorch.app.main;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.base.Presenter;
import com.pyamsoft.zaptorch.app.frag.ConfirmationDialog;
import com.pyamsoft.zaptorch.dagger.main.MainActivityInteractor;
import javax.inject.Inject;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

public final class MainActivityPresenter extends Presenter<MainActivityPresenter.MainActivityView> {

  @NonNull private final MainActivityInteractor mainActivityInteractor;
  @NonNull private Subscription confirmDialogBusSubscription = Subscriptions.empty();

  @Inject public MainActivityPresenter(@NonNull MainActivityInteractor mainActivityInteractor) {
    this.mainActivityInteractor = mainActivityInteractor;
  }

  @Override public void onResume() {
    super.onResume();
    registerOnConfirmDialogBus();
  }

  @Override public void onPause() {
    super.onPause();
    unregisterFromConfirmDialogBus();
  }

  @CheckResult public final boolean shouldHandleKeycode(int keyCode) {
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
    return mainActivityInteractor.shouldHandleKeys() && handled;
  }

  private void registerOnConfirmDialogBus() {
    unregisterFromConfirmDialogBus();
    confirmDialogBusSubscription =
        ConfirmationDialog.ConfirmationDialogBus.get().register().subscribe(confirmationEvent -> {
          if (confirmationEvent.isComplete()) {
            Timber.d("received completed clearAll event. Kill Process");
            android.os.Process.killProcess(android.os.Process.myPid());
          }
        }, throwable -> {
          Timber.e(throwable, "ConfirmationDialogBus onError");
        });
  }

  private void unregisterFromConfirmDialogBus() {
    if (!confirmDialogBusSubscription.isUnsubscribed()) {
      confirmDialogBusSubscription.unsubscribe();
    }
  }

  public interface MainActivityView {
  }
}
