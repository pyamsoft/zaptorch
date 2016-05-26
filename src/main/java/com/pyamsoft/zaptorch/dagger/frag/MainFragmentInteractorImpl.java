package com.pyamsoft.zaptorch.dagger.frag;

import android.content.Context;
import android.support.annotation.NonNull;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import javax.inject.Inject;
import rx.Observable;
import timber.log.Timber;

final class MainFragmentInteractorImpl implements MainFragmentInteractor {

  @NonNull private final ZapTorchPreferences preferences;
  @NonNull private final Context appContext;

  @Inject MainFragmentInteractorImpl(@NonNull Context context,
      @NonNull ZapTorchPreferences preferences) {
    this.appContext = context.getApplicationContext();
    this.preferences = preferences;
  }

  @NonNull @Override public Observable<Boolean> clearAll() {
    return Observable.defer(() -> {
      Timber.d("Clear all preferences");
      preferences.clear(true);
      return Observable.just(true);
    });
  }
}
