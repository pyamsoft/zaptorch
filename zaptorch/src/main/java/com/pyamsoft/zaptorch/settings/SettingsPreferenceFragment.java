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

package com.pyamsoft.zaptorch.settings;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.view.View;
import com.pyamsoft.pydroid.app.PersistLoader;
import com.pyamsoft.pydroid.ui.app.fragment.ActionBarSettingsPreferenceFragment;
import com.pyamsoft.pydroid.util.AppUtil;
import com.pyamsoft.pydroid.util.PersistentCache;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorch;
import com.pyamsoft.zaptorch.service.VolumeMonitorService;
import timber.log.Timber;

public class SettingsPreferenceFragment extends ActionBarSettingsPreferenceFragment
    implements SettingsPreferenceFragmentPresenter.MainFragmentView {

  @NonNull public static final String TAG = "MainFragment";
  @NonNull private static final String KEY_PRESENTER = "key_main_fragment_presenter";
  @SuppressWarnings("WeakerAccess") SettingsPreferenceFragmentPresenter presenter;
  private long loadedKey;

  @CheckResult @NonNull SettingsPreferenceFragmentPresenter getPresenter() {
    if (presenter == null) {
      throw new NullPointerException("Presenter is NULL");
    }

    return presenter;
  }

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loadedKey = PersistentCache.get()
        .load(KEY_PRESENTER, savedInstanceState,
            new PersistLoader.Callback<SettingsPreferenceFragmentPresenter>() {
              @NonNull @Override
              public PersistLoader<SettingsPreferenceFragmentPresenter> createLoader() {
                return new SettingsPreferenceFragmentPresenterLoader();
              }

              @Override
              public void onPersistentLoaded(@NonNull SettingsPreferenceFragmentPresenter persist) {
                presenter = persist;
              }
            });
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    final Preference zapTorchExplain = findPreference(getString(R.string.zaptorch_explain_key));
    zapTorchExplain.setOnPreferenceClickListener(preference -> {
      AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), new HowToDialog(), "howto");
      return true;
    });
  }

  @Override public void onStart() {
    super.onStart();
    presenter.bindView(this);
  }

  @Override public void onStop() {
    super.onStop();
    presenter.unbindView();
  }

  @Override protected boolean onClearAllPreferenceClicked() {
    presenter.confirmSettingsClear();
    return super.onClearAllPreferenceClicked();
  }

  @Override protected int getRootViewContainer() {
    return R.id.main_viewport;
  }

  @NonNull @Override protected String getApplicationName() {
    return getString(R.string.app_name);
  }

  @Override protected int getPreferenceXmlResId() {
    return R.xml.preferences;
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    PersistentCache.get()
        .saveKey(outState, KEY_PRESENTER, loadedKey, SettingsPreferenceFragmentPresenter.class);
    super.onSaveInstanceState(outState);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (!getActivity().isChangingConfigurations()) {
      PersistentCache.get().unload(loadedKey);
    }
    ZapTorch.getRefWatcher(this).watch(this);
  }

  @Override public void onConfirmAttempt() {
    AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), new ConfirmationDialog(),
        "confirm_dialog");
  }

  @Override public void onClearAll() {
    Timber.d("received completed clearAll event. Kill Process");
    try {
      VolumeMonitorService.finish();
    } catch (IllegalStateException e) {
      Timber.e(e, "Expected exception when Service is NULL");
    }

    final ActivityManager activityManager = (ActivityManager) getContext().getApplicationContext()
        .getSystemService(Context.ACTIVITY_SERVICE);
    activityManager.clearApplicationUserData();
  }

  @Override public void onCameraApiChanged() {
    if (VolumeMonitorService.isRunning()) {
      VolumeMonitorService.changeCameraApi();
    }
  }
}
