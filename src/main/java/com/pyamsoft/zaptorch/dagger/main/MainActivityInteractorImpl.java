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
import android.support.annotation.WorkerThread;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import com.pyamsoft.zaptorch.app.main.KeyHandlerBus;
import javax.inject.Inject;
import rx.Observable;

public class MainActivityInteractorImpl implements MainActivityInteractor {

  @NonNull private final ZapTorchPreferences preferences;

  @Inject public MainActivityInteractorImpl(@NonNull ZapTorchPreferences preferences) {
    this.preferences = preferences;
  }

  @WorkerThread @NonNull @Override public Observable<Boolean> shouldHandleKeys() {
    return Observable.defer(() -> Observable.just(preferences.shouldHandleKeys()))
        .map(aBoolean -> aBoolean == null ? false : aBoolean);
  }

  @WorkerThread @NonNull @Override public Observable<Boolean> setHandleKeys(boolean b) {
    return Observable.defer(() -> {
      preferences.setHandleKeys(b);
      KeyHandlerBus.post(new KeyHandlerBus.Event(b));
      return Observable.just(b);
    }).map(aBoolean -> aBoolean == null ? false : aBoolean);
  }
}
