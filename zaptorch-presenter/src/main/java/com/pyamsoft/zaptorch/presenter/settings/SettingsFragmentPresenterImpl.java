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

package com.pyamsoft.zaptorch.presenter.settings;

import com.pyamsoft.pydroid.presenter.PresenterBase;

class SettingsFragmentPresenterImpl extends PresenterBase<SettingsFragmentPresenter.View>
    implements SettingsFragmentPresenter {

  SettingsFragmentPresenterImpl() {
  }

  @Override public void clickFABServiceRunning() {
    getView(View::onDisplayServiceInfo);
  }

  @Override public void clickFABServiceIdle() {
    getView(View::onCreateAccessibilityDialog);
  }

  @Override public void loadFABFromState(boolean serviceRunning) {
    getView(view -> {
      if (serviceRunning) {
        view.onFABEnabled();
      } else {
        view.onFABDisabled();
      }
    });
  }
}
