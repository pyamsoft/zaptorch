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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.view.View;
import com.pyamsoft.pydroid.ui.app.fragment.ActionBarSettingsPreferenceFragment;
import com.pyamsoft.pydroid.util.DialogUtil;
import com.pyamsoft.zaptorch.Injector;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorch;
import com.pyamsoft.zaptorch.service.VolumeMonitorService;
import timber.log.Timber;

public class SettingsPreferenceFragment extends ActionBarSettingsPreferenceFragment {

  @NonNull public static final String TAG = "SettingsPreferenceFragment";
  @SuppressWarnings("WeakerAccess") SettingsPreferenceFragmentPresenter presenter;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Injector.get().provideComponent().plusSettingsComponent().inject(this);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    final Preference zapTorchExplain = findPreference(getString(R.string.zaptorch_explain_key));
    zapTorchExplain.setOnPreferenceClickListener(preference -> {
      DialogUtil.guaranteeSingleDialogFragment(getActivity(), new HowToDialog(), "howto");
      return true;
    });
  }

  @Override public void onStart() {
    super.onStart();
    presenter.listenForCameraChanges(() -> {
      if (VolumeMonitorService.isRunning()) {
        VolumeMonitorService.changeCameraApi();
      }
    });

    presenter.registerEventBus(() -> {
      Timber.d("received completed clearAll event. Kill Process");
      try {
        VolumeMonitorService.finish();
      } catch (IllegalStateException e) {
        Timber.e(e, "Expected exception when Service is NULL");
      }

      final ActivityManager activityManager = (ActivityManager) getContext().getApplicationContext()
          .getSystemService(Context.ACTIVITY_SERVICE);
      activityManager.clearApplicationUserData();
    });
  }

  @Override public void onStop() {
    super.onStop();
    presenter.stop();
  }

  @Override protected void onClearAllClicked() {
    DialogUtil.guaranteeSingleDialogFragment(getActivity(), new ConfirmationDialog(),
        "confirm_dialog");
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

  @Override public void onDestroy() {
    super.onDestroy();
    presenter.destroy();
    ZapTorch.getRefWatcher(this).watch(this);
  }
}
