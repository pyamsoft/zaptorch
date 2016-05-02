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
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import com.pyamsoft.zaptorch.ZapTorch;
import com.pyamsoft.zaptorch.dagger.service.DaggerVolumeServiceComponent;
import com.pyamsoft.zaptorch.dagger.service.VolumeServiceModule;
import javax.inject.Inject;
import timber.log.Timber;

public class VolumeMonitorService extends AccessibilityService implements VolumeServiceProvider {

  private static final String TAG = VolumeMonitorService.class.getSimpleName();
  private static VolumeMonitorService instance;

  @Inject VolumeServicePresenter servicePresenter;

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
    Timber.d("onAccessibilityEvent");
  }

  @Override public void onInterrupt() {
    Timber.e("onInterrupt");
  }

  @Override protected void onServiceConnected() {
    super.onServiceConnected();

    DaggerVolumeServiceComponent.builder()
        .zapTorchComponent(ZapTorch.zapTorchComponent(this))
        .volumeServiceModule(new VolumeServiceModule())
        .build()
        .inject(this);

    servicePresenter.bind(this);
    setInstance(this);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    setInstance(null);
    servicePresenter.unbind();
  }
}
