package com.pyamsoft.zaptorch.dagger.frag;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.base.ApplicationPreferences;
import rx.Observable;

interface MainFragmentInteractor {

  @CheckResult @NonNull Observable<Boolean> clearAll();

  void registerCameraApiListener(
      @NonNull ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener);

  void unregisterCameraApiListener(
      @NonNull ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener);

  @CheckResult @NonNull String getCameraApiKey();
}
