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

package com.pyamsoft.zaptorch;

import android.os.StrictMode;
import com.pyamsoft.pydroid.base.app.ApplicationBase;

public class ZapTorch extends ApplicationBase {

  @Override protected boolean buildConfigDebug() {
    return BuildConfig.DEBUG;
  }

  @Override protected void installInDebugMode() {
    super.installInDebugMode();
    StrictMode.setThreadPolicy(
        new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build());
    StrictMode.setVmPolicy(
        new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().penaltyDeath().build());
  }
}
