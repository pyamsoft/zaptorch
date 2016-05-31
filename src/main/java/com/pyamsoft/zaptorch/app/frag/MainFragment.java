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
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pyamsoft.pydroid.util.AppUtil;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorch;
import com.pyamsoft.zaptorch.dagger.frag.DaggerMainFragmentComponent;
import com.pyamsoft.zaptorch.dagger.frag.MainFragmentModule;
import javax.inject.Inject;
import timber.log.Timber;

public final class MainFragment extends PreferenceFragmentCompat
    implements MainFragmentPresenter.MainFragmentView {

  @Nullable @Inject MainFragmentPresenter presenter;

  @Override public void onCreatePreferences(Bundle bundle, String s) {
    addPreferencesFromResource(R.xml.preferences);

    final Preference zapTorchExplain = findPreference(getString(R.string.zaptorch_explain_key));
    zapTorchExplain.setOnPreferenceClickListener(preference -> {
      AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), new HowToDialog(), "howto");
      return true;
    });

    final Preference resetAll = findPreference(getString(R.string.clear_all_key));
    resetAll.setOnPreferenceClickListener(preference -> {
      Timber.d("Reset settings onClick");
      if (presenter == null) {
        throw new NullPointerException("Presenter is NULL");
      }
      presenter.confirmSettingsClear();
      return true;
    });
  }

  @Nullable @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    DaggerMainFragmentComponent.builder()
        .zapTorchComponent(ZapTorch.zapTorchComponent(this))
        .mainFragmentModule(new MainFragmentModule())
        .build()
        .inject(this);
    if (presenter == null) {
      throw new NullPointerException("Presenter is NULL");
    }
    presenter.onCreateView(this);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override public void onResume() {
    super.onResume();
    if (presenter == null) {
      throw new NullPointerException("Presenter is NULL");
    }
    presenter.onResume();
  }

  @Override public void onPause() {
    super.onPause();
    if (presenter == null) {
      throw new NullPointerException("Presenter is NULL");
    }
    presenter.onPause();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    if (presenter != null) {
      presenter.onDestroyView();
    }
  }

  @Override public void onConfirmAttempt() {
    AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), ConfirmationDialog.newInstance(),
        "confirm_dialog");
  }
}
