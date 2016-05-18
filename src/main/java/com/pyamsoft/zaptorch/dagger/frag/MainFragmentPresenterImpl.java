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
import com.pyamsoft.pydroid.base.PresenterImpl;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import com.pyamsoft.zaptorch.app.frag.MainFragmentPresenter;
import com.pyamsoft.zaptorch.dagger.main.MainActivityInteractor;
import com.pyamsoft.zaptorch.dagger.service.VolumeServiceInteractor;
import javax.inject.Inject;

final class MainFragmentPresenterImpl extends PresenterImpl<MainFragmentPresenter.MainFragmentView>
    implements MainFragmentPresenter {

  @NonNull private final VolumeServiceInteractor serviceInteractor;
  @NonNull private final MainActivityInteractor mainActivityInteractor;

  @Inject public MainFragmentPresenterImpl(@NonNull VolumeServiceInteractor serviceInteractor,
      @NonNull MainActivityInteractor mainActivityInteractor) {
    this.serviceInteractor = serviceInteractor;
    this.mainActivityInteractor = mainActivityInteractor;
  }

  @Override public void setDisplayErrorsFromPreference() {
    final boolean show = serviceInteractor.shouldShowErrorDialog();
    final MainFragmentView mainFragmentView = getView();
    if (mainFragmentView != null) {
      if (show) {
        mainFragmentView.setDisplayErrors();
      } else {
        mainFragmentView.unsetDisplayErrors();
      }
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
    final long delay = serviceInteractor.getButtonDelayTime();
    final MainFragmentView mainFragmentView = getView();
    if (mainFragmentView != null) {
      if (delay == ZapTorchPreferences.DELAY_SHORT) {
        mainFragmentView.setDelayShort();
      } else if (delay == ZapTorchPreferences.DELAY_DEFAULT) {
        mainFragmentView.setDelayDefault();
      } else {
        mainFragmentView.setDelayLong();
      }
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
    final boolean handle = mainActivityInteractor.shouldHandleKeys();
    final MainFragmentView mainFragmentView = getView();
    if (mainFragmentView != null) {
      if (handle) {
        mainFragmentView.setHandleKeys();
      } else {
        mainFragmentView.unsetHandleKeys();
      }
    }
  }

  private void setHandleKeys(boolean b) {
    mainActivityInteractor.setHandleKeys(b);
  }

  @Override public void setHandleKeys() {
    setHandleKeys(true);
  }

  @Override public void unsetHandleKeys() {
    setHandleKeys(false);
  }
}
