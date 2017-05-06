/*
 * Copyright 2017 Peter Kenji Yamanaka
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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pyamsoft.pydroid.design.fab.HideScrollFABBehavior;
import com.pyamsoft.pydroid.design.util.FABUtil;
import com.pyamsoft.pydroid.loader.ImageLoader;
import com.pyamsoft.pydroid.loader.LoaderHelper;
import com.pyamsoft.pydroid.loader.loaded.Loaded;
import com.pyamsoft.pydroid.ui.app.fragment.ActionBarFragment;
import com.pyamsoft.pydroid.util.DialogUtil;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorch;
import com.pyamsoft.zaptorch.databinding.FragmentMainBinding;
import com.pyamsoft.zaptorch.service.VolumeMonitorService;

public class SettingsFragment extends ActionBarFragment {

  @NonNull public static final String TAG = "MainSettingsFragment";
  private FragmentMainBinding binding;
  @NonNull private Loaded fabTask = LoaderHelper.empty();

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    binding = FragmentMainBinding.inflate(inflater, container, false);
    return binding.getRoot();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setupFAB();
    displayPreferenceFragment();
  }

  private void setupFAB() {
    binding.mainSettingsFab.setOnClickListener(v -> {
      if (VolumeMonitorService.isRunning()) {
        DialogUtil.guaranteeSingleDialogFragment(getActivity(), new ServiceInfoDialog(),
            "servce_info");
      } else {
        DialogUtil.guaranteeSingleDialogFragment(getActivity(), new AccessibilityRequestDialog(),
            "accessibility");
      }
    });
    FABUtil.setupFABBehavior(binding.mainSettingsFab, new HideScrollFABBehavior(10));
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    fabTask = LoaderHelper.unload(fabTask);
    binding.unbind();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    ZapTorch.getRefWatcher(this).watch(this);
  }

  @Override public void onResume() {
    super.onResume();
    setActionBarUpEnabled(false);
    if (VolumeMonitorService.isRunning()) {
      fabTask = LoaderHelper.unload(fabTask);
      fabTask = ImageLoader.fromResource(getActivity(), R.drawable.ic_help_24dp)
          .into(binding.mainSettingsFab);
    } else {
      fabTask = LoaderHelper.unload(fabTask);
      fabTask = ImageLoader.fromResource(getActivity(), R.drawable.ic_service_start_24dp)
          .into(binding.mainSettingsFab);
    }
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
}
