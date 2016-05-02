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

package com.pyamsoft.zaptorch.app.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.pyamsoft.pydroid.base.PreferenceBase;

public final class VolumeServiceInteractorImpl implements VolumeServiceInteractor {

  @Nullable private volatile ServicePreferences preferences;

  @NonNull private ServicePreferences getPreferences(@NonNull final Context context) {
    if (preferences == null) {
      synchronized (this) {
        if (preferences == null) {
          preferences = new ServicePreferences(context);
        }
      }
    }

    // This should be non-null as the double check handles above
    //noinspection ConstantConditions
    return preferences;
  }

  @Override public long getButtonDelayTime(@NonNull Context context) {
    return getPreferences(context).getButtonDelayTime();
  }

  @Override public void setButtonDelayTime(@NonNull Context context, long time) {
    getPreferences(context).setButtonDelayTime(time);
  }

  @Override public boolean shouldShowErrorDialog(@NonNull Context context) {
    return getPreferences(context).shouldShowErrorDialog();
  }

  @Override public void setShowErrorDialog(@NonNull Context context, boolean b) {
    getPreferences(context).setShowErrorDialog(b);
  }

  public static class ServicePreferences extends PreferenceBase {

    private static final String TAG = ServicePreferences.class.getSimpleName();
    private static final String DELAY = TAG + ".delay";
    private static final String ERROR = TAG + ".error";

    public static final long DELAY_SHORT = 360L;
    public static final long DELAY_DEFAULT = 500L;
    public static final long DELAY_LONG = 1000L;

    ServicePreferences(Context context) {
      super(context);
    }

    public final long getButtonDelayTime() {
      return getLong(DELAY, DELAY_DEFAULT);
    }

    public final void setButtonDelayTime(final long l) {
      putLong(DELAY, l);
    }

    public final boolean shouldShowErrorDialog() {
      return getBoolean(ERROR, true);
    }

    public final void setShowErrorDialog(final boolean b) {
      putBoolean(ERROR, b);
    }
  }
}
