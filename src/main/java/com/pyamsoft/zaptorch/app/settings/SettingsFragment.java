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

package com.pyamsoft.zaptorch.app.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pyamsoft.pydroid.app.PersistLoader;
import com.pyamsoft.pydroid.app.fragment.ActionBarFragment;
import com.pyamsoft.pydroid.tool.AsyncDrawable;
import com.pyamsoft.pydroid.tool.AsyncMap;
import com.pyamsoft.pydroid.tool.AsyncMapHelper;
import com.pyamsoft.pydroid.util.AppUtil;
import com.pyamsoft.pydroid.util.PersistentCache;
import com.pyamsoft.pydroiddesign.fab.HideScrollFABBehavior;
import com.pyamsoft.pydroiddesign.util.FABUtil;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.databinding.FragmentMainBinding;

public class SettingsFragment extends ActionBarFragment implements SettingsFragmentPresenter.View {

  @NonNull public static final String TAG = "MainSettingsFragment";
  @NonNull private static final String KEY_PRESENTER = "key_settings_presenter";
  @SuppressWarnings("WeakerAccess") SettingsFragmentPresenter presenter;
  private FragmentMainBinding binding;
  private long loadedKey;
  @Nullable private AsyncMap.Entry fabTask;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    loadedKey = PersistentCache.get()
        .load(KEY_PRESENTER, savedInstanceState,
            new PersistLoader.Callback<SettingsFragmentPresenter>() {

              @NonNull @Override public PersistLoader<SettingsFragmentPresenter> createLoader() {
                return new SettingsFragmentPresenterLoader();
              }

              @Override public void onPersistentLoaded(@NonNull SettingsFragmentPresenter persist) {
                presenter = persist;
              }
            });
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentMainBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupFAB();
  }

  private void setupFAB() {
    binding.mainSettingsFab.setOnClickListener(view -> presenter.clickFAB());
    FABUtil.setupFABBehavior(binding.mainSettingsFab, new HideScrollFABBehavior(10));
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    AsyncMapHelper.unsubscribe(fabTask);
    binding.unbind();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    if (!getActivity().isChangingConfigurations()) {
      PersistentCache.get().unload(loadedKey);
    }
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    PersistentCache.get()
        .saveKey(outState, KEY_PRESENTER, loadedKey, SettingsFragmentPresenter.class);
    super.onSaveInstanceState(outState);
  }

  @Override public void onResume() {
    super.onResume();
    setActionBarUpEnabled(false);
    presenter.loadFABFromState();
    displayPreferenceFragment();
  }

  @Override public void onStop() {
    super.onStop();
    presenter.unbindView();
  }

  @Override public void onStart() {
    super.onStart();
    presenter.bindView(this);
  }

  private void displayPreferenceFragment() {
    final FragmentManager fragmentManager = getChildFragmentManager();
    if (fragmentManager.findFragmentByTag(SettingsPreferenceFragment.TAG) == null) {
      fragmentManager.beginTransaction()
          .replace(R.id.main_container, new SettingsPreferenceFragment(),
              SettingsPreferenceFragment.TAG)
          .commit();
    }
  }

  @Override public void onFABEnabled() {
    AsyncMapHelper.unsubscribe(fabTask);
    fabTask = AsyncDrawable.load(R.drawable.ic_help_24dp).into(binding.mainSettingsFab);
  }

  @Override public void onFABDisabled() {
    AsyncMapHelper.unsubscribe(fabTask);
    fabTask = AsyncDrawable.load(R.drawable.ic_service_start_24dp).into(binding.mainSettingsFab);
  }

  @Override public void onCreateAccessibilityDialog() {
    AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), new AccessibilityRequestDialog(),
        "accessibility");
  }

  @Override public void onDisplayServiceInfo() {
    AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), new ServiceInfoDialog(),
        "servce_info");
  }
}
