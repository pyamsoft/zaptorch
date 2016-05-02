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

package com.pyamsoft.zaptorch.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.pyamsoft.pydroid.base.ActivityBase;
import com.pyamsoft.pydroid.util.IMMLeakUtil;
import com.pyamsoft.pydroid.util.LogUtil;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.app.main.MainFragment;
import com.pyamsoft.zaptorch.app.service.VolumeMonitorService;

public class MainActivity extends ActivityBase
    implements BillingProcessor.IBillingHandler, MainActivityView {

  private static final String TAG = MainActivity.class.getSimpleName();

  public MainActivity() {
    this.mainActivityPresenter = new MainActivityPresenterImpl();
  }

  private final MainActivityPresenter mainActivityPresenter;
  private BillingProcessor billingProcessor;

  @SuppressWarnings({ "WeakerAccess", "unused" }) @Bind(R.id.toolbar) Toolbar toolbar;

  private boolean serviceEnabled = false;

  @Override protected String getPlayStoreAppPackage() {
    return getPackageName();
  }

  @Override public BillingProcessor getBillingProcessor() {
    return billingProcessor;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    IMMLeakUtil.fixFocusedViewLeak(getApplication());
    setupFakeFullscreenWindow();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    mainActivityPresenter.create();
    mainActivityPresenter.bind(this);

    billingProcessor = new BillingProcessor(this, getBillingKey(), this);

    setupAppBar();
    if (VolumeMonitorService.isRunning()) {
      showMainFragment();
    } else {
      showAccessibilityRequestFragment();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (billingProcessor != null) {
      billingProcessor.release();
    }

    mainActivityPresenter.unbind();
    mainActivityPresenter.destroy();
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (billingProcessor != null) {
      if (!billingProcessor.handleActivityResult(requestCode, resultCode, data)) {
        super.onActivityResult(requestCode, resultCode, data);
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
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

  @Override public void onProductPurchased(String productId, TransactionDetails details) {
    LogUtil.d(TAG, "onProductPurchased");
    LogUtil.d(TAG, "Details: ", details);
    if (billingProcessor != null) {
      LogUtil.d(TAG, "Consume item: ", productId);
      billingProcessor.consumePurchase(productId);
    }
  }

  @Override public void onPurchaseHistoryRestored() {
    LogUtil.d(TAG, "onPurchaseHistoryRestored");
  }

  @Override public void onBillingError(int errorCode, Throwable error) {
    LogUtil.e(TAG, "onBillingError");
    LogUtil.e(TAG, "CODE: ", errorCode);
    LogUtil.exception(TAG, error);
  }

  @Override public void onBillingInitialized() {
    LogUtil.d(TAG, "onBillingInitialized");
  }

  @NonNull @Override public Context getContext() {
    return this;
  }
}
