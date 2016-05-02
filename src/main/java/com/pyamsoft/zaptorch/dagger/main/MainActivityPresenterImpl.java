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
import com.pyamsoft.zaptorch.app.main.MainActivityInteractor;
import com.pyamsoft.zaptorch.app.main.MainActivityPresenter;
import com.pyamsoft.zaptorch.app.main.MainActivityView;
import javax.inject.Inject;
import timber.log.Timber;

final class MainActivityPresenterImpl extends PresenterImplBase<MainActivityView>
    implements MainActivityPresenter {

  @NonNull private final MainActivityInteractor mainActivityInteractor;

  @Inject public MainActivityPresenterImpl(@NonNull MainActivityInteractor mainActivityInteractor) {
    this.mainActivityInteractor = mainActivityInteractor;
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
    return mainActivityInteractor.shouldHandleKeys() && handled;
  }
}
