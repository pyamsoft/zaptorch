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
import com.pyamsoft.pydroid.base.PresenterImplBase;
import com.pyamsoft.zaptorch.app.main.KeyHandlerBus;
import com.pyamsoft.zaptorch.app.main.MainActivityInteractor;
import com.pyamsoft.zaptorch.app.main.MainActivityPresenter;
import com.pyamsoft.zaptorch.app.main.MainActivityView;
import javax.inject.Inject;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

final class MainActivityPresenterImpl extends PresenterImplBase<MainActivityView>
    implements MainActivityPresenter {

  @NonNull private final MainActivityInteractor mainActivityInteractor;

  @NonNull private Subscription handleKeysSubscription = Subscriptions.empty();
  @NonNull private Subscription handleKeysBus = Subscriptions.empty();
  private boolean handleKeys;

  @Inject public MainActivityPresenterImpl(@NonNull MainActivityInteractor mainActivityInteractor) {
    this.mainActivityInteractor = mainActivityInteractor;
  }

  @Override public void bind(@NonNull MainActivityView view) {
    super.bind(view);
    registerOnKeyHandlerBus();
    unsubHandleKeysSubscription();
    mainActivityInteractor.shouldHandleKeys()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aBoolean -> {
          Timber.d("Initial handlekeys: ", handleKeys);
          handleKeys = aBoolean;
        }, throwable -> {
          // TODO handle error
          Timber.e(throwable, "onError");
        });
  }

  @Override public void unbind() {
    super.unbind();
    unregisterFromKeyHandlerBus();
    unsubHandleKeysSubscription();
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

  @Override public void registerOnKeyHandlerBus() {
    unregisterFromKeyHandlerBus();
    handleKeysBus = KeyHandlerBus.register().subscribe(event -> {
      handleKeys = event.handle();
      Timber.d("Update handle keys: %s", handleKeys);
    }, throwable -> {
      // TODO handle error
      Timber.e(throwable, "onError");
    });
  }

  @Override public void unregisterFromKeyHandlerBus() {
    if (!handleKeysBus.isUnsubscribed()) {
      handleKeysBus.unsubscribe();
    }
  }

  private void unsubHandleKeysSubscription() {
    if (!handleKeysSubscription.isUnsubscribed()) {
      handleKeysSubscription.unsubscribe();
    }
  }
}
