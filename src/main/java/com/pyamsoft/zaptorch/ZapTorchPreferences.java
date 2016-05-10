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

import android.content.Context;
import com.pyamsoft.pydroid.base.ApplicationPreferences;

public class ZapTorchPreferences extends ApplicationPreferences {
  public static final long DELAY_SHORT = 360L;
  public static final long DELAY_DEFAULT = 500L;
  public static final long DELAY_LONG = 1000L;
  private static final String DELAY = "delay";
  private static final String ERROR = "error";
  private static final String HANDLE_KEYS = "handle_keys";

  public ZapTorchPreferences(Context context) {
    super(context);
  }

  public final long getButtonDelayTime() {
    return get(DELAY, DELAY_DEFAULT);
  }

  public final void setButtonDelayTime(final long l) {
    put(DELAY, l);
  }

  public final boolean shouldShowErrorDialog() {
    return get(ERROR, true);
  }

  public final void setShowErrorDialog(final boolean b) {
    put(ERROR, b);
  }

  public final boolean shouldHandleKeys() {
    return get(HANDLE_KEYS, true);
  }

  public final void setHandleKeys(final boolean b) {
    put(HANDLE_KEYS, b);
  }
}
