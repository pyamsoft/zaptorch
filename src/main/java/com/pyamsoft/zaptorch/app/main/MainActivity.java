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

package com.pyamsoft.zaptorch.app.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.view.KeyEvent;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.pyamsoft.pydroid.base.DonationActivityBase;
import com.pyamsoft.pydroid.support.RatingDialog;
import com.pyamsoft.pydroid.util.StringUtil;
import com.pyamsoft.zaptorch.BuildConfig;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorch;
import com.pyamsoft.zaptorch.app.frag.MainFragment;
import com.pyamsoft.zaptorch.app.service.VolumeMonitorService;
import com.pyamsoft.zaptorch.dagger.main.DaggerMainComponent;
import javax.inject.Inject;

public class MainActivity extends DonationActivityBase
    implements MainActivityPresenter.MainActivityView, RatingDialog.ChangeLogProvider {

  @BindView(R.id.toolbar) Toolbar toolbar;
  @Inject MainActivityPresenter mainActivityPresenter;
  private Unbinder unbinder;
  private boolean serviceEnabled = false;

  @NonNull @Override protected String getPlayStoreAppPackage() {
    return getPackageName();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
    unbinder = ButterKnife.bind(this);

    DaggerMainComponent.builder()
        .zapTorchComponent(ZapTorch.zapTorchComponent(this))
        .build()
        .inject(this);

    mainActivityPresenter.onCreateView(this);

    setupAppBar();
    if (VolumeMonitorService.isRunning()) {
      showMainFragment();
    } else {
      showAccessibilityRequestFragment();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    mainActivityPresenter.onDestroyView();

    if (unbinder != null) {
      unbinder.unbind();
    }
  }

  @Override protected void onResume() {
    super.onResume();
    mainActivityPresenter.onResume();
    animateActionBarToolbar(toolbar);
  }

  @Override protected void onPause() {
    super.onPause();
    mainActivityPresenter.onPause();
  }

  @Override protected void onPostResume() {
    super.onPostResume();
    RatingDialog.showRatingDialog(this, this);

    if (VolumeMonitorService.isRunning()) {
      if (!serviceEnabled) {
        showMainFragment();
      }
    } else {
      if (serviceEnabled) {
        showAccessibilityRequestFragment();
      }
    }
  }

  @Override public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
    return mainActivityPresenter.shouldHandleKeycode(keyCode) || super.onKeyUp(keyCode, event);
  }

  @Override public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
    return mainActivityPresenter.shouldHandleKeycode(keyCode) || super.onKeyDown(keyCode, event);
  }

  private void showAccessibilityRequestFragment() {
    serviceEnabled = false;
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_viewport, new AccessibilityRequestFragment())
        .commit();
  }

  private void showMainFragment() {
    serviceEnabled = true;
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_viewport, new MainFragment())
        .commit();
  }

  private void setupAppBar() {
    setSupportActionBar(toolbar);
    toolbar.setTitle(getString(R.string.app_name));
  }

  @NonNull @Override public Spannable getChangeLogText() {
    // The changelog text
    final String title = "What's New in Version " + BuildConfig.VERSION_NAME;
    final String line1 =
        "BUGFIX: Fix a crash that occurred occasionally when stopping the Torch on devices below Lollipop";
    final String line2 =
        "FEATURE: Select between different available Camera APIs at runtime. By default, the Camera API is set to the newest for the platform";
    final String line3 =
        "BUGFIX: Properly clear all application when using the 'Reset All Settings' option";
    final String line4 =
        "CHANGE: Lower the 'small' camera delay period from 360 to 300 milliseconds";

    // Turn it into a spannable
    final Spannable spannable =
        StringUtil.createBuilder(title, "\n\n", line1, "\n\n", line2, "\n\n", line3, "\n\n", line4);

    int start = 0;
    int end = title.length();
    final int largeSize =
        StringUtil.getTextSizeFromAppearance(this, android.R.attr.textAppearanceLarge);
    final int largeColor =
        StringUtil.getTextColorFromAppearance(this, android.R.attr.textAppearanceLarge);
    final int smallSize =
        StringUtil.getTextSizeFromAppearance(this, android.R.attr.textAppearanceSmall);
    final int smallColor =
        StringUtil.getTextColorFromAppearance(this, android.R.attr.textAppearanceSmall);

    StringUtil.boldSpan(spannable, start, end);
    StringUtil.sizeSpan(spannable, start, end, largeSize);
    StringUtil.colorSpan(spannable, start, end, largeColor);

    start += end + 2;
    end += 2 + line1.length() + 2 + line2.length() + 2 + line3.length() + 2 + line4.length();

    StringUtil.sizeSpan(spannable, start, end, smallSize);
    StringUtil.colorSpan(spannable, start, end, smallColor);

    return spannable;
  }

  @Override public int getChangeLogIcon() {
    return R.mipmap.ic_launcher;
  }

  @NonNull @Override public String getChangeLogPackageName() {
    return getPackageName();
  }

  @Override public int getChangeLogVersion() {
    return BuildConfig.VERSION_CODE;
  }
}
