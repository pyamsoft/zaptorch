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

package com.pyamsoft.zaptorch.main;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import com.pyamsoft.pydroid.cache.PersistentCache;
import com.pyamsoft.pydroid.ui.about.AboutLibrariesFragment;
import com.pyamsoft.pydroid.ui.rating.RatingDialog;
import com.pyamsoft.pydroid.ui.sec.TamperActivity;
import com.pyamsoft.pydroid.util.AnimUtil;
import com.pyamsoft.pydroid.util.AppUtil;
import com.pyamsoft.pydroid.util.NetworkUtil;
import com.pyamsoft.zaptorch.BuildConfig;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.databinding.ActivityMainBinding;
import com.pyamsoft.zaptorch.settings.SettingsFragment;

public class MainActivity extends TamperActivity implements MainPresenter.MainActivityView {

  @NonNull private static final String TAG = "MainActivity";
  @NonNull private static final String KEY_PRESENTER = TAG + "key_main_presenter";
  @NonNull private static final String PRIVACY_POLICY_URL =
      "https://pyamsoft.blogspot.com/p/zaptorch-privacy-policy.html";
  @SuppressWarnings("WeakerAccess") MainPresenter presenter;
  private ActivityMainBinding binding;

  @Override protected void onCreate(Bundle savedInstanceState) {
    setTheme(R.style.Theme_ZapTorch);
    super.onCreate(savedInstanceState);
    PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);

    presenter = PersistentCache.load(this, KEY_PRESENTER, new MainPresenterLoader());
    setupAppBar();
  }

  @Override protected int bindActivityToView() {
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    return R.id.ad_view;
  }

  @Override protected void onStart() {
    super.onStart();
    presenter.bindView(this);
    showMainFragment();
  }

  @Override protected void onStop() {
    super.onStop();
    presenter.unbindView();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    binding.unbind();
  }

  @Override protected void onResume() {
    super.onResume();
    AnimUtil.animateActionBarToolbar(binding.toolbar);
  }

  @Override protected void onPostResume() {
    super.onPostResume();
    RatingDialog.showRatingDialog(this, this);
  }

  @NonNull @Override protected String getSafePackageName() {
    return "com.pyamsoft.zaptorch";
  }

  @Override public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
    return presenter.shouldHandleKeycode(keyCode) || super.onKeyUp(keyCode, event);
  }

  @Override public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
    return presenter.shouldHandleKeycode(keyCode) || super.onKeyDown(keyCode, event);
  }

  private void showMainFragment() {
    final FragmentManager fragmentManager = getSupportFragmentManager();
    if (fragmentManager.findFragmentByTag(SettingsFragment.TAG) == null
        && fragmentManager.findFragmentByTag(AboutLibrariesFragment.TAG) == null) {
      fragmentManager.beginTransaction()
          .replace(R.id.main_viewport, new SettingsFragment(), SettingsFragment.TAG)
          .commitNow();
    }
  }

  @Override public void onBackPressed() {
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final int backStackCount = fragmentManager.getBackStackEntryCount();
    if (backStackCount > 0) {
      fragmentManager.popBackStack();
    } else {
      super.onBackPressed();
    }
  }

  @Override public boolean onOptionsItemSelected(final @NonNull MenuItem item) {
    final int itemId = item.getItemId();
    boolean handled;
    switch (itemId) {
      case android.R.id.home:
        onBackPressed();
        handled = true;
        break;
      case R.id.menu_id_privacy_policy:
        NetworkUtil.newLink(getApplicationContext(), PRIVACY_POLICY_URL);
        handled = true;
        break;
      default:
        handled = false;
    }
    return handled || super.onOptionsItemSelected(item);
  }

  private void setupAppBar() {
    setSupportActionBar(binding.toolbar);
    binding.toolbar.setTitle(R.string.app_name);
    ViewCompat.setElevation(binding.toolbar, AppUtil.convertToDP(this, 4));
  }

  @Override public boolean onCreateOptionsMenu(@NonNull Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @NonNull @Override protected String[] getChangeLogLines() {
    final String line1 = "BUGFIX: Fix code related to in app billing";
    final String line2 = "BUGFIX: Smaller memory footprint";
    return new String[] { line1, line2 };
  }

  @NonNull @Override protected String getVersionName() {
    return BuildConfig.VERSION_NAME;
  }

  @Override public int getApplicationIcon() {
    return R.mipmap.ic_launcher;
  }

  @NonNull @Override public String provideApplicationName() {
    return "ZapTorch";
  }

  @Override public int getCurrentApplicationVersion() {
    return BuildConfig.VERSION_CODE;
  }
}
