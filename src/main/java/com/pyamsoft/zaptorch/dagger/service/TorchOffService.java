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

import android.app.IntentService;
import android.content.Intent;
import com.pyamsoft.zaptorch.app.service.VolumeMonitorService;
import timber.log.Timber;

public class TorchOffService extends IntentService {

  public TorchOffService() {
    super(TorchOffService.class.getName());
  }

  @Override protected void onHandleIntent(Intent intent) {
    try {
      VolumeMonitorService.forceToggle();
    } catch (IllegalStateException e) {
      Timber.e(e, "onError");
    }
  }
}
