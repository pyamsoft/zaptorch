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

package com.pyamsoft.zaptorch.service;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import com.pyamsoft.pydroid.bus.EventBus;
import com.pyamsoft.zaptorch.Injector;
import com.pyamsoft.zaptorch.model.ServiceEvent;
import com.pyamsoft.zaptorch.service.error.CameraErrorExplanation;
import timber.log.Timber;

public class VolumeMonitorService extends AccessibilityService
    implements VolumeServicePresenter.VolumeServiceView {

  private static boolean running;
  VolumeServicePresenter presenter;

  @CheckResult public static boolean isRunning() {
    return running;
  }

  private static void setRunning(boolean running) {
    VolumeMonitorService.running = running;
  }

  public static void finish() {
    EventBus.get().publish(ServiceEvent.create(ServiceEvent.Type.FINISH));
  }

  public static void forceToggle() {
    EventBus.get().publish(ServiceEvent.create(ServiceEvent.Type.TORCH));
  }

  public static void changeCameraApi() {
    EventBus.get().publish(ServiceEvent.create(ServiceEvent.Type.CHANGE_CAMERA));
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
      Injector.get().provideComponent().plusVolumeServiceComponent().inject(this);
    }

    VolumeServicePresenter.VolumeServiceView view = this;
    presenter.bindView(view);

    presenter.registerOnBus(new VolumeServicePresenter.ServiceCallback() {
      @Override public void onToggleTorch() {
        Timber.d("Toggle Torch");
        presenter.toggleTorch();
      }

      @Override public void onFinishService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
          disableSelf();
        }
      }

      @Override public void onChangeCameraApi() {
        // Simulate the lifecycle for destroying and re-creating the presenter
        Timber.d("Change setupCamera API");
        presenter.unbindView();
        presenter.bindView(view);
      }
    });
    setRunning(true);
  }

  @Override public boolean onUnbind(Intent intent) {
    presenter.unbindView();
    setRunning(false);
    return super.onUnbind(intent);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    presenter.destroy();
  }

  @Override public void onCameraOpenError(@NonNull Intent errorIntent) {
    errorIntent.setClass(getApplicationContext(), CameraErrorExplanation.class);
    getApplication().startActivity(errorIntent);
  }
}
