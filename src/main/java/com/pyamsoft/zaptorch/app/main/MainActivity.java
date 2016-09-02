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

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.view.KeyEvent;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.pyamsoft.pydroid.base.activity.DonationActivity;
import com.pyamsoft.pydroid.base.app.PersistLoader;
import com.pyamsoft.pydroid.support.RatingDialog;
import com.pyamsoft.pydroid.tool.PersistentCache;
import com.pyamsoft.pydroid.util.AnimUtil;
import com.pyamsoft.pydroid.util.StringUtil;
import com.pyamsoft.zaptorch.BuildConfig;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.app.accessibility.AccessibilityRequestFragment;
import com.pyamsoft.zaptorch.app.frag.MainFragment;
import com.pyamsoft.zaptorch.app.service.VolumeMonitorService;
import timber.log.Timber;

public class MainActivity extends DonationActivity
    implements MainPresenter.MainActivityView, RatingDialog.ChangeLogProvider {

  @NonNull private static final String KEY_PRESENTER = "key_main_presenter";
  @BindView(R.id.toolbar) Toolbar toolbar;
  MainPresenter presenter;
  private Unbinder unbinder;
  private long loadedKey;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
    unbinder = ButterKnife.bind(this);

    loadedKey = PersistentCache.load(KEY_PRESENTER, savedInstanceState,
        new PersistLoader.Callback<MainPresenter>() {
          @NonNull @Override public PersistLoader<MainPresenter> createLoader() {
            return new MainPresenterLoader(getApplicationContext());
          }

          @Override public void onPersistentLoaded(@NonNull MainPresenter persist) {
            presenter = persist;
          }
        });

    setupAppBar();
  }

  @Override protected int bindActivityToView() {
    setContentView(R.layout.activity_main);
    return R.id.ad_view;
  }

  @NonNull @Override protected String provideAdViewUnitId() {
    return getString(R.string.banner_ad_id);
  }

  @Override protected boolean isAdDebugMode() {
    return BuildConfig.DEBUG;
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    PersistentCache.saveKey(KEY_PRESENTER, outState, loadedKey);
    super.onSaveInstanceState(outState);
  }

  @Override protected void onStart() {
    super.onStart();
    presenter.bindView(this);
  }

  @Override protected void onStop() {
    super.onStop();
    presenter.unbindView();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (!isChangingConfigurations()) {
      PersistentCache.unload(loadedKey);
    }
    unbinder.unbind();
  }

  @Override protected void onResume() {
    super.onResume();
    AnimUtil.animateActionBarToolbar(toolbar);
  }

  @Override protected void onPostResume() {
    super.onPostResume();
    RatingDialog.showRatingDialog(this, this);

    if (VolumeMonitorService.isRunning()) {
      showMainFragment();
    } else {
      showAccessibilityRequestFragment();
    }
  }

  @Override public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
    return presenter.shouldHandleKeycode(keyCode) || super.onKeyUp(keyCode, event);
  }

  @Override public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
    return presenter.shouldHandleKeycode(keyCode) || super.onKeyDown(keyCode, event);
  }

  void showAccessibilityRequestFragment() {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_viewport, new AccessibilityRequestFragment())
        .commit();
  }

  void showMainFragment() {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.main_viewport, new MainFragment())
        .commit();
  }

  void setupAppBar() {
    setSupportActionBar(toolbar);
    toolbar.setTitle(getString(R.string.app_name));
  }

  @Override public void onClearAll() {
    Timber.d("received completed clearAll event. Kill Process");
    VolumeMonitorService.finish();
    final ActivityManager activityManager =
        (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
    activityManager.clearApplicationUserData();
    android.os.Process.killProcess(android.os.Process.myPid());
  }

  @NonNull @Override public Spannable getChangeLogText() {
    // The changelog text
    final String title = "What's New in Version " + BuildConfig.VERSION_NAME;
    final String line1 = "BUGFIX: Fix a display bug where the 'How to Use' was not fully displayed";
    final String line2 =
        "FEATURE: Display an error dialog when Android Permissions need to be granted instead of silently failing.";
    final String line3 =
        "BUGFIX: Cleaner checking for whether or not the Accessibility Service is running";

    // Turn it into a spannable
    final Spannable spannable = StringUtil.createLineBreakBuilder(title, line1, line2, line3);

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
    end += 2 + line1.length() + 2 + line2.length() + 2 + line3.length();

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
