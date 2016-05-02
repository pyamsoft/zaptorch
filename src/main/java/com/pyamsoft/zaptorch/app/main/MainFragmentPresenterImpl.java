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

import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.base.PresenterImplBase;
import com.pyamsoft.zaptorch.app.MainActivityInteractor;
import com.pyamsoft.zaptorch.app.MainActivityInteractorImpl;
import com.pyamsoft.zaptorch.app.service.VolumeServiceInteractor;
import com.pyamsoft.zaptorch.app.service.VolumeServiceInteractorImpl;

class MainFragmentPresenterImpl extends PresenterImplBase<MainFragmentView> implements
    MainFragmentPresenter {

  @NonNull private final VolumeServiceInteractor serviceInteractor;
  @NonNull private final MainActivityInteractor mainActivityInteractor;

  public MainFragmentPresenterImpl() {
    serviceInteractor = new VolumeServiceInteractorImpl();
    mainActivityInteractor = new MainActivityInteractorImpl();
  }

  @Override public void setDisplayErrorsFromPreference() {
    final MainFragmentView view = get();
    if (view != null) {
      final boolean set = serviceInteractor.shouldShowErrorDialog(view.getContext());
      if (set) {
        view.setDisplayErrors();
      } else {
        view.unsetDisplayErrors();
      }
    }
  }

  private void setDisplayErrors(boolean b) {
    final MainFragmentView view = get();
    if (view != null) {
      serviceInteractor.setShowErrorDialog(view.getContext(), b);
    }
  }

  @Override public void setDisplayErrors() {
    setDisplayErrors(true);
  }

  @Override public void unsetDisplayErrors() {
    setDisplayErrors(false);
  }

  @Override public void setDelayFromPreference() {
    final MainFragmentView view = get();
    if (view != null) {
      final long delay = serviceInteractor.getButtonDelayTime(view.getContext());
      if (delay == VolumeServiceInteractorImpl.ServicePreferences.DELAY_SHORT) {
        view.setDelayShort();
      } else if (delay == VolumeServiceInteractorImpl.ServicePreferences.DELAY_DEFAULT) {
        view.setDelayDefault();
      } else {
        view.setDelayLong();
      }
    }
  }

  @Override public void setDelayShort() {
    final MainFragmentView view = get();
    if (view != null) {
      serviceInteractor.setButtonDelayTime(view.getContext(),
          VolumeServiceInteractorImpl.ServicePreferences.DELAY_SHORT);
    }
  }

  @Override public void setDelayDefault() {
    final MainFragmentView view = get();
    if (view != null) {
      serviceInteractor.setButtonDelayTime(view.getContext(),
          VolumeServiceInteractorImpl.ServicePreferences.DELAY_DEFAULT);
    }
  }

  @Override public void setDelayLong() {
    final MainFragmentView view = get();
    if (view != null) {
      serviceInteractor.setButtonDelayTime(view.getContext(),
          VolumeServiceInteractorImpl.ServicePreferences.DELAY_LONG);
    }
  }

  @Override public void setHandleKeysFromPreference() {
    final MainFragmentView view = get();
    if (view != null) {
      final boolean set = serviceInteractor.shouldShowErrorDialog(view.getContext());
      if (set) {
        view.setHandleKeys();
      } else {
        view.unsetHandleKeys();
      }
    }
  }

  private void setHandleKeys(boolean b) {
    final MainFragmentView view = get();
    if (view != null) {
      mainActivityInteractor.setHandleKeys(view.getContext(), b);
    }
  }

  @Override public void setHandleKeys() {
    setHandleKeys(true);
  }

  @Override public void unsetHandleKeys() {
    setHandleKeys(false);
  }
}
