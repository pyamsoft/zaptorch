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
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.pyamsoft.pydroid.base.ActivityBase;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorch;
import com.pyamsoft.zaptorch.app.AccessibilityRequestFragment;
import com.pyamsoft.zaptorch.app.frag.MainFragment;
import com.pyamsoft.zaptorch.app.service.VolumeMonitorService;
import com.pyamsoft.zaptorch.dagger.main.DaggerMainComponent;
import javax.inject.Inject;

public class MainActivity extends ActivityBase implements MainActivityPresenter.MainActivityView {

  @SuppressWarnings({ "WeakerAccess", "unused" }) @BindView(R.id.toolbar) Toolbar toolbar;
  @Inject MainActivityPresenter mainActivityPresenter;
  private Unbinder unbinder;
  private boolean serviceEnabled = false;

  @Override protected String getPlayStoreAppPackage() {
    return getPackageName();
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
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
    animateActionBarToolbar(toolbar);
  }

  @Override protected void onPostResume() {
    super.onPostResume();
    if (!BillingProcessor.isIabServiceAvailable(this)) {
      showDonationUnavailableDialog();
    }

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

  @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
    return mainActivityPresenter.shouldHandleKeycode(keyCode) || super.onKeyUp(keyCode, event);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    return mainActivityPresenter.shouldHandleKeycode(keyCode) || super.onKeyDown(keyCode, event);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_bugreport_dark, menu);
    return super.onCreateOptionsMenu(menu);
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
}
