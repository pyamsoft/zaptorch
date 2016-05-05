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

  @Inject public MainFragmentPresenterImpl(@NonNull VolumeServiceInteractor serviceInteractor,
      @NonNull MainActivityInteractor mainActivityInteractor) {
    this.serviceInteractor = serviceInteractor;
    this.mainActivityInteractor = mainActivityInteractor;
  }

  @Override public void unbind() {
    super.unbind();
    unsubHandleKeysSubscription();
  }

  @Override public void setDisplayErrorsFromPreference() {
    final MainFragmentView view = get();
    final boolean set = serviceInteractor.shouldShowErrorDialog();
    if (set) {
      view.setDisplayErrors();
    } else {
      view.unsetDisplayErrors();
    }
  }

  private void setDisplayErrors(boolean b) {
    serviceInteractor.setShowErrorDialog(b);
  }

  @Override public void setDisplayErrors() {
    setDisplayErrors(true);
  }

  @Override public void unsetDisplayErrors() {
    setDisplayErrors(false);
  }

  @Override public void setDelayFromPreference() {
    final MainFragmentView view = get();
    final long delay = serviceInteractor.getButtonDelayTime();
    if (delay == ZapTorchPreferences.DELAY_SHORT) {
      view.setDelayShort();
    } else if (delay == ZapTorchPreferences.DELAY_DEFAULT) {
      view.setDelayDefault();
    } else {
      view.setDelayLong();
    }
  }

  @Override public void setDelayShort() {
    serviceInteractor.setButtonDelayTime(ZapTorchPreferences.DELAY_SHORT);
  }

  @Override public void setDelayDefault() {
    serviceInteractor.setButtonDelayTime(ZapTorchPreferences.DELAY_DEFAULT);
  }

  @Override public void setDelayLong() {
    serviceInteractor.setButtonDelayTime(ZapTorchPreferences.DELAY_LONG);
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
