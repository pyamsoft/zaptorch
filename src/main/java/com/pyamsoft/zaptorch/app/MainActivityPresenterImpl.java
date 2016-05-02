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

package com.pyamsoft.zaptorch.app;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.base.PresenterImplBase;
import com.pyamsoft.pydroid.util.LogUtil;

public class MainActivityPresenterImpl extends PresenterImplBase<MainActivityView>
    implements MainActivityPresenter {

  private static final String TAG = MainActivityPresenterImpl.class.getSimpleName();
  @NonNull private final MainActivityInteractor mainActivityInteractor;

  public MainActivityPresenterImpl() {
    this.mainActivityInteractor = new MainActivityInteractorImpl();
  }

  @Override public boolean shouldHandleKeycode(int keyCode) {
    final MainActivityView view = get();
    if (view != null) {
      boolean handled;
      switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
          LogUtil.d(TAG, "Detected a Volume Down event. Consume and do nothing");
          handled = true;
          break;
        case KeyEvent.KEYCODE_VOLUME_UP:
          LogUtil.d(TAG, "Detected a Volume Up event. Consume and do nothing");
          handled = true;
          break;
        default:
          handled = false;
      }
      return mainActivityInteractor.shouldHandleKeys(view.getContext()) && handled;
    } else {
      return false;
    }
  }
}
