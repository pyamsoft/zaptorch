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

package com.pyamsoft.zaptorch.settings;

import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.presenter.Presenter;
import com.pyamsoft.pydroid.presenter.PresenterBase;

class SettingsFragmentPresenterImpl extends PresenterBase<Presenter.Empty>
    implements SettingsFragmentPresenter {

  SettingsFragmentPresenterImpl() {
  }

  @Override public void clickFABServiceRunning(@NonNull DisplayServiceCallback callback) {
    callback.onDisplayServiceInfo();
  }

  @Override public void clickFABServiceIdle(@NonNull AccessibilityDialogCallback callback) {
    callback.onCreateAccessibilityDialog();
  }

  @Override
  public void loadFABFromState(boolean serviceRunning, @NonNull FABStateCallback callback) {
    if (serviceRunning) {
      callback.onFABEnabled();
    } else {
      callback.onFABDisabled();
    }
  }
}
