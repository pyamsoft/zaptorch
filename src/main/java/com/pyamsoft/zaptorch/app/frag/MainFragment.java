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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.text.Spannable;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.pyamsoft.pydroid.util.StringUtil;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorch;
import com.pyamsoft.zaptorch.dagger.frag.DaggerMainFragmentComponent;
import com.pyamsoft.zaptorch.dagger.frag.MainFragmentModule;
import javax.inject.Inject;
import timber.log.Timber;

public final class MainFragment extends Fragment implements MainFragmentView {

  private static final String TAG = MainFragment.class.getSimpleName();

  @SuppressWarnings({ "WeakerAccess", "unused" }) @BindView(R.id.main_explain_howto) TextView
      explainHowTo;

  @SuppressWarnings({ "WeakerAccess", "unused" }) @BindView(R.id.main_display_camera_errors)
  SwitchCompat displayErrors;

  @SuppressWarnings({ "WeakerAccess", "unused" }) @BindView(R.id.main_handle_keys) SwitchCompat
      handleKeys;

  @SuppressWarnings({ "WeakerAccess", "unused" }) @BindView(R.id.main_buttondelay_short) RadioButton
      delayShort;
  @SuppressWarnings({ "WeakerAccess", "unused" }) @BindView(R.id.main_buttondelay_default)
  RadioButton delayDefault;
  @SuppressWarnings({ "WeakerAccess", "unused" }) @BindView(R.id.main_buttondelay_long) RadioButton
      delayLong;
  @Inject MainFragmentPresenter mainFragmentPresenter;
  private Unbinder unbinder;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    DaggerMainFragmentComponent.builder()
        .zapTorchComponent(ZapTorch.zapTorchComponent(this))
        .mainFragmentModule(new MainFragmentModule())
        .build()
        .inject(this);

    mainFragmentPresenter.create();
  }

  @Override public void onDestroy() {
    super.onDestroy();
    mainFragmentPresenter.destroy();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_main, container, false);
    unbinder = ButterKnife.bind(this, view);
    mainFragmentPresenter.bind(this);
    return view;
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    mainFragmentPresenter.unbind();
    if (unbinder != null) {
      unbinder.unbind();
    }
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setExplainHowToText();
    setDisplayErrorsText();
    setHandleKeysText();

    mainFragmentPresenter.setDisplayErrorsFromPreference();
    mainFragmentPresenter.setDelayFromPreference();
    mainFragmentPresenter.setHandleKeysFromPreference();

    setOnSwitchListenerDisplayErrors();
    setOnSwitchListenerHandleKeys();
    setOnCheckedListenerSmall();
    setOnCheckedListenerDefault();
    setOnCheckedListenerLarge();
  }

  private void setExplainHowToText() {
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

    explainHowTo.setText(spannable);
  }

  private void setDisplayErrorsText() {
    final Spannable spannable = StringUtil.createBuilder("Display Camera Errors:", "\n",
        "Shows a dialog if an error has occurred while trying to access the device's camera");
    final int index = spannable.toString().indexOf(':');
    if (index != -1) {
      Timber.d("New line at: %d", index);
      final int largeTextSize =
          StringUtil.getTextSizeFromAppearance(getContext(), android.R.attr.textAppearanceLarge);
      final int largeTextColor =
          StringUtil.getTextColorFromAppearance(getContext(), android.R.attr.textAppearanceLarge);
      if (largeTextColor != 0 && largeTextSize != 0) {
        StringUtil.multiSpannable(spannable, 0, index + 1, new AbsoluteSizeSpan(largeTextSize),
            new ForegroundColorSpan(largeTextColor));
      }

      final int smallTextSize =
          StringUtil.getTextSizeFromAppearance(getContext(), android.R.attr.textAppearanceSmall);
      final int smallTextColor =
          StringUtil.getTextColorFromAppearance(getContext(), android.R.attr.textAppearanceSmall);
      if (smallTextColor != 0 && smallTextSize != 0) {
        StringUtil.multiSpannable(spannable, index + 1, spannable.length(),
            new AbsoluteSizeSpan(smallTextSize), new ForegroundColorSpan(smallTextColor));
      }
    }
    displayErrors.setText(spannable);
  }

  private void setHandleKeysText() {
    final Spannable spannable = StringUtil.createBuilder("Handle Volume Key Press:", "\n",
        "Pressing the volume keys while ZapTorch is open will not change the volume");
    final int index = spannable.toString().indexOf(':');
    if (index != -1) {
      Timber.d("New line at: %d", index);
      final int largeTextSize =
          StringUtil.getTextSizeFromAppearance(getContext(), android.R.attr.textAppearanceLarge);
      final int largeTextColor =
          StringUtil.getTextColorFromAppearance(getContext(), android.R.attr.textAppearanceLarge);
      if (largeTextColor != 0 && largeTextSize != 0) {
        StringUtil.multiSpannable(spannable, 0, index + 1, new AbsoluteSizeSpan(largeTextSize),
            new ForegroundColorSpan(largeTextColor));
      }

      final int smallTextSize =
          StringUtil.getTextSizeFromAppearance(getContext(), android.R.attr.textAppearanceSmall);
      final int smallTextColor =
          StringUtil.getTextColorFromAppearance(getContext(), android.R.attr.textAppearanceSmall);
      if (smallTextColor != 0 && smallTextSize != 0) {
        StringUtil.multiSpannable(spannable, index + 1, spannable.length(),
            new AbsoluteSizeSpan(smallTextSize), new ForegroundColorSpan(smallTextColor));
      }
    }
    handleKeys.setText(spannable);
  }

  private void setOnCheckedListenerSmall() {
    delayShort.setOnCheckedChangeListener((btn, b) -> {
      if (b) {
        mainFragmentPresenter.setDelayShort();
      }
    });
  }

  private void setOnCheckedListenerDefault() {
    delayDefault.setOnCheckedChangeListener((btn, b) -> {
      if (b) {
        mainFragmentPresenter.setDelayDefault();
      }
    });
  }

  private void setOnCheckedListenerLarge() {
    delayLong.setOnCheckedChangeListener((btn, b) -> {
      if (b) {
        mainFragmentPresenter.setDelayLong();
      }
    });
  }

  private void setOnSwitchListenerDisplayErrors() {
    displayErrors.setOnCheckedChangeListener((btn, b) -> {
      if (b) {
        mainFragmentPresenter.setDisplayErrors();
      } else {
        mainFragmentPresenter.unsetDisplayErrors();
      }
    });
  }

  private void setOnSwitchListenerHandleKeys() {
    handleKeys.setOnCheckedChangeListener((btn, b) -> {
      if (b) {
        mainFragmentPresenter.setHandleKeys();
      } else {
        mainFragmentPresenter.unsetHandleKeys();
      }
    });
  }

  @Override public void setDelayShort() {
    delayShort.setOnCheckedChangeListener(null);
    delayShort.setChecked(true);
    setOnCheckedListenerSmall();
  }

  @Override public void setDelayDefault() {
    delayDefault.setOnCheckedChangeListener(null);
    delayDefault.setChecked(true);
    setOnCheckedListenerDefault();
  }

  @Override public void setDelayLong() {
    delayLong.setOnCheckedChangeListener(null);
    delayLong.setChecked(true);
    setOnCheckedListenerLarge();
  }

  @Override public void setHandleKeys() {
    handleKeys.setOnCheckedChangeListener(null);
    handleKeys.setChecked(true);
    setOnSwitchListenerHandleKeys();
  }

  @Override public void unsetHandleKeys() {
    handleKeys.setOnCheckedChangeListener(null);
    handleKeys.setChecked(false);
    setOnSwitchListenerHandleKeys();
  }

  @Override public void setDisplayErrors() {
    displayErrors.setOnCheckedChangeListener(null);
    displayErrors.setChecked(true);
    setOnSwitchListenerDisplayErrors();
  }

  @Override public void unsetDisplayErrors() {
    displayErrors.setOnCheckedChangeListener(null);
    displayErrors.setChecked(false);
    setOnSwitchListenerDisplayErrors();
  }
}
