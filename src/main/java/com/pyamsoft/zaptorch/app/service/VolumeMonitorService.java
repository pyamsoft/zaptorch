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
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import com.pyamsoft.pydroid.util.LogUtil;

public class VolumeMonitorService extends AccessibilityService implements VolumeServiceProvider {

  private static final String TAG = VolumeMonitorService.class.getSimpleName();
  private static VolumeMonitorService instance;

  private final VolumeServicePresenter servicePresenter;

  public VolumeMonitorService() {
    servicePresenter = new VolumeServicePresenterImpl();
  }

  private static void setInstance(VolumeMonitorService i) {
    instance = i;
  }

  public static boolean isRunning() {
    return instance != null && instance.servicePresenter.isStarted();
  }

  @Override protected boolean onKeyEvent(KeyEvent event) {
    final int action = event.getAction();
    final int keyCode = event.getKeyCode();
    servicePresenter.handleKeyEvent(action, keyCode);

    // Never consume events
    return false;
  }

  @Override public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    LogUtil.d(TAG, "onAccessibilityEvent");
  }

  @Override public void onInterrupt() {
    LogUtil.e(TAG, "onInterrupt");
  }

  @Override protected void onServiceConnected() {
    super.onServiceConnected();
    servicePresenter.bind(this);
    setInstance(this);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    setInstance(null);
    servicePresenter.unbind();
  }

  @NonNull @Override public Context getContext() {
    return this;
  }
}
