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

package com.pyamsoft.zaptorch.main;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import com.pyamsoft.pydroid.helper.Checker;
import com.pyamsoft.zaptorch.base.ZapTorchPreferences;
import io.reactivex.Observable;
import timber.log.Timber;

class MainInteractor {

  @SuppressWarnings("WeakerAccess") @NonNull final ZapTorchPreferences preferences;

  MainInteractor(@NonNull ZapTorchPreferences preferences) {
    this.preferences = Checker.checkNonNull(preferences);
  }

  @CheckResult @NonNull public Observable<Boolean> shouldHandleKeys(int keyCode) {
    return Observable.fromCallable(preferences::shouldHandleKeys).filter(shouldHandle -> {
      boolean handled;
      switch (keyCode) {
        case KeyEvent.KEYCODE_VOLUME_DOWN:
          Timber.d("Detected a Volume Down event.");
          handled = true;
          break;
        case KeyEvent.KEYCODE_VOLUME_UP:
          Timber.d("Detected a Volume Up event.");
          handled = true;
          break;
        default:
          handled = false;
      }
      return shouldHandle && handled;
    });
  }
}
