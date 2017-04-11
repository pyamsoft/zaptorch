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

package com.pyamsoft.zaptorch.service;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.helper.Checker;
import com.pyamsoft.zaptorch.base.ZapTorchModule;
import io.reactivex.Scheduler;

public class VolumeServiceModule {

  @NonNull private final VolumeServiceInteractor interactor;
  @NonNull private final Scheduler obsScheduler;
  @NonNull private final Scheduler subScheduler;

  public VolumeServiceModule(@NonNull ZapTorchModule module) {
    module = Checker.checkNonNull(module);
    interactor =
        new VolumeServiceInteractor(module.provideContext(), module.provideCameraPreferences(),
            module.provideTorchOffServiceClass());
    obsScheduler = module.provideObsScheduler();
    subScheduler = module.provideSubScheduler();
  }

  @NonNull @CheckResult VolumeServicePresenter getPresenter() {
    return new VolumeServicePresenter(interactor, obsScheduler, subScheduler);
  }
}
