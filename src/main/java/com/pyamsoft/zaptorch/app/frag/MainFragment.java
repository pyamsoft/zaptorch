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
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.pyamsoft.pydroid.util.AppUtil;
import com.pyamsoft.pydroid.util.StringUtil;
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
    setExplainHowToText(zapTorchExplain);

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

  private static void setExplainHowToText(final Preference zapTorchExplain) {
    final String text1 = "ZapTorch prides itself on being easy to use.";
    final String text2 = "\n\n";
    final String text3 = "To get going, just make sure that the screen is ";
    final String text4 = "On";
    final String text5 = ", and then click ";
    final String text6 = "twice ";
    final String text7 = "on the ";
    final String text8 = "Volume Down ";
    final String text9 = "key to turn your Flashlight ";
    final String text10 = "On or Off.";
    final Spannable spannable =
        StringUtil.createBuilder(text1, text2, text3, text4, text5, text6, text7, text8, text9,
            text10);

    int preBoldLength = text1.length() + text2.length() + text3.length();
    int postBoldLength = preBoldLength + text4.length();
    StringUtil.boldSpan(spannable, preBoldLength, postBoldLength);

    preBoldLength = postBoldLength + text5.length();
    postBoldLength = preBoldLength + text6.length();
    StringUtil.boldSpan(spannable, preBoldLength, postBoldLength);

    preBoldLength = postBoldLength + text7.length();
    postBoldLength = preBoldLength + text8.length();
    StringUtil.boldSpan(spannable, preBoldLength, postBoldLength);

    preBoldLength = postBoldLength + text9.length();
    postBoldLength = preBoldLength + text10.length();
    StringUtil.boldSpan(spannable, preBoldLength, postBoldLength);

    zapTorchExplain.setSummary(spannable);
  }

  @Override public void onConfirmAttempt() {
    AppUtil.guaranteeSingleDialogFragment(getFragmentManager(), ConfirmationDialog.newInstance(),
        "confirm_dialog");
  }
}
