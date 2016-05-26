package com.pyamsoft.zaptorch.dagger.frag;

import android.content.Context;
import android.support.annotation.NonNull;
import com.pyamsoft.pydroid.base.ApplicationPreferences;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import javax.inject.Inject;
import rx.Observable;
import timber.log.Timber;

final class MainFragmentInteractorImpl implements MainFragmentInteractor {

  @NonNull private final ZapTorchPreferences preferences;
  @NonNull private final Context appContext;
  @NonNull private final String cameraApiKey;

  @Inject MainFragmentInteractorImpl(@NonNull Context context,
      @NonNull ZapTorchPreferences preferences) {
    this.appContext = context.getApplicationContext();
    this.preferences = preferences;
    this.cameraApiKey = appContext.getString(R.string.camera_api_key);
  }

  @NonNull @Override public Observable<Boolean> clearAll() {
    return Observable.defer(() -> {
      Timber.d("Clear all preferences");
      preferences.clear(true);
      return Observable.just(true);
    });
  }

  @Override public void registerCameraApiListener(
      @NonNull ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener) {
    unregisterCameraApiListener(cameraApiListener);
    cameraApiListener.register(preferences);
  }

  @Override public void unregisterCameraApiListener(
      @NonNull ApplicationPreferences.OnSharedPreferenceChangeListener cameraApiListener) {
    cameraApiListener.unregister(preferences);
  }

  @NonNull @Override public String getCameraApiKey() {
    return cameraApiKey;
  }
}
