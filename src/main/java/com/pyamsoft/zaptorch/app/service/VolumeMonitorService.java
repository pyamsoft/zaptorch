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

package com.pyamsoft.zaptorch.app.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import com.pyamsoft.zaptorch.ZapTorch;
import timber.log.Timber;

public class VolumeMonitorService extends AccessibilityService
    implements VolumeServicePresenter.VolumeServiceView {

  static VolumeMonitorService instance;
  private VolumeServicePresenter presenter;

  @CheckResult @NonNull static synchronized VolumeMonitorService getInstance() {
    if (instance == null) {
      throw new NullPointerException("VolumeMonitorService instance is NULL");
    }
    return instance;
  }

  static synchronized void setInstance(@Nullable VolumeMonitorService i) {
    instance = i;
  }

  public static void finish() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      getInstance().disableSelf();
    }
  }

  @CheckResult public static boolean isRunning() {
    return instance != null;
  }

  public static void changeCameraApi() {
    final VolumeMonitorService currentInstance = getInstance();
    // Simulate the lifecycle for destroying and re-creating the presenter
    Timber.d("Change camera API");
    currentInstance.getPresenter().unbindView();
    currentInstance.getPresenter().bindView(currentInstance);
  }

  @CheckResult @NonNull VolumeServicePresenter getPresenter() {
    return presenter;
  }

  @Override protected boolean onKeyEvent(KeyEvent event) {
    final int action = event.getAction();
    final int keyCode = event.getKeyCode();
    presenter.handleKeyEvent(action, keyCode);

    // Never consume events
    return false;
  }

  @Override public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    Timber.d("onAccessibilityEvent");
  }

  @Override public void onInterrupt() {
    Timber.e("onInterrupt");
  }

  @Override protected void onServiceConnected() {
    super.onServiceConnected();

    if (presenter == null) {
      presenter = ZapTorch.get(this).provideComponent().provideVolumeServiceModule().getPresenter();
    }

    presenter.bindView(this);
    setInstance(this);
  }

  @Override public boolean onUnbind(Intent intent) {
    presenter.unbindView();
    setInstance(null);
    return super.onUnbind(intent);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    presenter.destroy();
  }
}
