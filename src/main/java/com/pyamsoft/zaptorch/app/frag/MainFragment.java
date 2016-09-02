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

package com.pyamsoft.zaptorch.app.frag;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.View;
import com.pyamsoft.pydroid.base.app.PersistLoader;
import com.pyamsoft.pydroid.base.fragment.ActionBarSettingsPreferenceFragment;
import com.pyamsoft.pydroid.tool.PersistentCache;
import com.pyamsoft.pydroid.util.AppUtil;
import com.pyamsoft.zaptorch.R;
import timber.log.Timber;

public class MainFragment extends ActionBarSettingsPreferenceFragment
    implements MainFragmentPresenter.MainFragmentView {

  @NonNull private static final String KEY_PRESENTER = "key_main_fragment_presenter";
  MainFragmentPresenter presenter;
  private long loadedKey;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loadedKey = PersistentCache.load(KEY_PRESENTER, savedInstanceState,
        new PersistLoader.Callback<MainFragmentPresenter>() {
          @NonNull @Override public PersistLoader<MainFragmentPresenter> createLoader() {
            return new MainFragmentPresenterLoader(getContext());
          }

          @Override public void onPersistentLoaded(@NonNull MainFragmentPresenter persist) {
            presenter = persist;
          }
        });
  }

  @Override public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.preferences);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    final Preference zapTorchExplain = findPreference(getString(R.string.zaptorch_explain_key));
    zapTorchExplain.setOnPreferenceClickListener(preference -> {
      AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), new HowToDialog(), "howto");
      return true;
    });

    final Preference resetAll = findPreference(getString(R.string.clear_all_key));
    resetAll.setOnPreferenceClickListener(preference -> {
      Timber.d("Reset settings onClick");
      presenter.confirmSettingsClear();
      return true;
    });

    final Preference upgradeInfo = findPreference(getString(R.string.upgrade_info_key));
    upgradeInfo.setOnPreferenceClickListener(preference -> showChangelog());

    final SwitchPreferenceCompat showAds =
        (SwitchPreferenceCompat) findPreference(getString(R.string.adview_key));
    showAds.setOnPreferenceChangeListener((preference, newValue) -> toggleAdVisibility(newValue));
  }

  @Override public void onStart() {
    super.onStart();
    presenter.bindView(this);
  }

  @Override public void onStop() {
    super.onStop();
    presenter.unbindView();
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    PersistentCache.saveKey(KEY_PRESENTER, outState, loadedKey);
    super.onSaveInstanceState(outState);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (!getActivity().isChangingConfigurations()) {
      PersistentCache.unload(loadedKey);
    }
  }

  @Override public void onConfirmAttempt() {
    AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), ConfirmationDialog.newInstance(),
        "confirm_dialog");
  }
}
