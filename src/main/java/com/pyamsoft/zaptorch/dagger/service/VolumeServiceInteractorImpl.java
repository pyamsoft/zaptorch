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

package com.pyamsoft.zaptorch.dagger.service;

import android.support.annotation.NonNull;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import javax.inject.Inject;
import rx.Observable;

final class VolumeServiceInteractorImpl implements VolumeServiceInteractor {

  @NonNull private final ZapTorchPreferences preferences;

  @Inject public VolumeServiceInteractorImpl(@NonNull ZapTorchPreferences preferences) {
    this.preferences = preferences;
  }

  @NonNull @Override public Observable<Long> getButtonDelayTime() {
    return Observable.defer(() -> Observable.just(preferences.getButtonDelayTime()))
        .map(aLong -> aLong == null ? ZapTorchPreferences.DELAY_DEFAULT : aLong);
  }

  @NonNull @Override public Observable<Long> setButtonDelayTime(long time) {
    return Observable.defer(() -> {
      preferences.setButtonDelayTime(time);
      return Observable.just(time);
    });
  }

  @NonNull @Override public Observable<Boolean> shouldShowErrorDialog() {
    return Observable.defer(() -> Observable.just(preferences.shouldShowErrorDialog()))
        .map(aBoolean -> aBoolean == null ? true : aBoolean);
  }

  @NonNull @Override public Observable<Boolean> setShowErrorDialog(boolean b) {
    return Observable.defer(() -> {
      preferences.setShowErrorDialog(b);
      return Observable.just(b);
    });
  }
}
