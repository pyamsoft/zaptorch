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

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.base.PresenterImpl;
import com.pyamsoft.zaptorch.app.main.KeyHandlerBus;
import com.pyamsoft.zaptorch.app.main.MainActivityPresenter;
import javax.inject.Inject;
import rx.Subscription;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

final class MainActivityPresenterImpl extends PresenterImpl<MainActivityPresenter.MainActivityView>
    implements MainActivityPresenter {

  @NonNull private final MainActivityInteractor mainActivityInteractor;

  @NonNull private Subscription handleKeysBus = Subscriptions.empty();
  private boolean handleKeys;

  @Inject public MainActivityPresenterImpl(@NonNull MainActivityInteractor mainActivityInteractor) {
    this.mainActivityInteractor = mainActivityInteractor;
  }

  @Override public void onCreateView(@NonNull MainActivityView view) {
    super.onCreateView(view);
    handleKeys = mainActivityInteractor.shouldHandleKeys();
  }

  @Override public void onResume() {
    super.onResume();
    registerOnKeyHandlerBus();
  }

  @Override public void onPause() {
    super.onPause();
    unregisterFromKeyHandlerBus();
  }

  @Override public boolean shouldHandleKeycode(int keyCode) {
    boolean handled;
    switch (keyCode) {
      case KeyEvent.KEYCODE_VOLUME_DOWN:
        Timber.d("Detected a Volume Down event. Consume and do nothing");
        handled = true;
        break;
      case KeyEvent.KEYCODE_VOLUME_UP:
        Timber.d("Detected a Volume Up event. Consume and do nothing");
        handled = true;
        break;
      default:
        handled = false;
    }
    return handleKeys && handled;
  }

  private void registerOnKeyHandlerBus() {
    unregisterFromKeyHandlerBus();
    handleKeysBus = KeyHandlerBus.register().subscribe(event -> {
      handleKeys = event.handle();
      Timber.d("Update handle keys: %s", handleKeys);
    }, throwable -> {
      // TODO handle error
      Timber.e(throwable, "onError");
    });
  }

  private void unregisterFromKeyHandlerBus() {
    if (!handleKeysBus.isUnsubscribed()) {
      handleKeysBus.unsubscribe();
    }
  }
}
