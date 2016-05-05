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

import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.base.PresenterImplBase;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import com.pyamsoft.zaptorch.app.frag.MainFragmentPresenter;
import com.pyamsoft.zaptorch.app.frag.MainFragmentView;
import com.pyamsoft.zaptorch.app.main.MainActivityInteractor;
import com.pyamsoft.zaptorch.app.service.VolumeServiceInteractor;
import javax.inject.Inject;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;
import timber.log.Timber;

final class MainFragmentPresenterImpl extends PresenterImplBase<MainFragmentView>
    implements MainFragmentPresenter {

  @NonNull private final VolumeServiceInteractor serviceInteractor;
  @NonNull private final MainActivityInteractor mainActivityInteractor;

  @NonNull private Subscription handleKeysSubscription = Subscriptions.empty();
  @NonNull private Subscription errorDialogSubscription = Subscriptions.empty();
  @NonNull private Subscription delaySubscription = Subscriptions.empty();

  @Inject public MainFragmentPresenterImpl(@NonNull VolumeServiceInteractor serviceInteractor,
      @NonNull MainActivityInteractor mainActivityInteractor) {
    this.serviceInteractor = serviceInteractor;
    this.mainActivityInteractor = mainActivityInteractor;
  }

  @Override public void unbind() {
    super.unbind();
    unsubHandleKeysSubscription();
    unsubErrorDialogSubscription();
    unsubDelaySubscription();
  }

  private void unsubErrorDialogSubscription() {
    if (!errorDialogSubscription.isUnsubscribed()) {
      errorDialogSubscription.unsubscribe();
    }
  }

  private void unsubDelaySubscription() {
    if (!delaySubscription.isUnsubscribed()) {
      delaySubscription.unsubscribe();
    }
  }

  @Override public void setDisplayErrorsFromPreference() {
    unsubErrorDialogSubscription();
    errorDialogSubscription = serviceInteractor.shouldShowErrorDialog()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aBoolean -> {
          if (aBoolean) {
            get().setDisplayErrors();
          } else {
            get().unsetDisplayErrors();
          }
        }, throwable -> {
          // todo handle error
          Timber.e(throwable, "onError");
        });
  }

  private void setDisplayErrors(boolean b) {
    unsubErrorDialogSubscription();
    errorDialogSubscription = serviceInteractor.setShowErrorDialog(b)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();
  }

  @Override public void setDisplayErrors() {
    setDisplayErrors(true);
  }

  @Override public void unsetDisplayErrors() {
    setDisplayErrors(false);
  }

  @Override public void setDelayFromPreference() {
    unsubDelaySubscription();
    delaySubscription = serviceInteractor.getButtonDelayTime()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(delay -> {
          if (delay == ZapTorchPreferences.DELAY_SHORT) {
            get().setDelayShort();
          } else if (delay == ZapTorchPreferences.DELAY_DEFAULT) {
            get().setDelayDefault();
          } else {
            get().setDelayLong();
          }
        }, throwable -> {
          // todo handle error
          Timber.e(throwable, "onError");
        });
  }

  @Override public void setDelayShort() {
    unsubDelaySubscription();
    delaySubscription = serviceInteractor.setButtonDelayTime(ZapTorchPreferences.DELAY_SHORT)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();
  }

  @Override public void setDelayDefault() {
    unsubDelaySubscription();
    delaySubscription = serviceInteractor.setButtonDelayTime(ZapTorchPreferences.DELAY_DEFAULT)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();
  }

  @Override public void setDelayLong() {
    unsubDelaySubscription();
    delaySubscription = serviceInteractor.setButtonDelayTime(ZapTorchPreferences.DELAY_LONG)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();
  }

  @Override public void setHandleKeysFromPreference() {
    unsubHandleKeysSubscription();
    handleKeysSubscription = mainActivityInteractor.shouldHandleKeys()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aBoolean -> {
          if (aBoolean) {
            get().setHandleKeys();
          } else {
            get().unsetHandleKeys();
          }
        }, throwable -> {
          // TODO handle errors
          Timber.e(throwable, "onError");
        });
  }

  private void setHandleKeys(boolean b) {
    unsubHandleKeysSubscription();
    handleKeysSubscription = mainActivityInteractor.setHandleKeys(b)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();
  }

  @Override public void setHandleKeys() {
    setHandleKeys(true);
  }

  @Override public void unsetHandleKeys() {
    setHandleKeys(false);
  }

  private void unsubHandleKeysSubscription() {
    if (!handleKeysSubscription.isUnsubscribed()) {
      handleKeysSubscription.unsubscribe();
    }
  }
}
