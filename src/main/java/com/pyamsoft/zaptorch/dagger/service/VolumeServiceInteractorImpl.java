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

package com.pyamsoft.zaptorch.dagger.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import com.pyamsoft.pydroid.tool.AsyncOffloader;
import com.pyamsoft.pydroid.tool.Offloader;
import com.pyamsoft.zaptorch.R;
import com.pyamsoft.zaptorch.ZapTorchPreferences;
import com.pyamsoft.zaptorch.app.service.camera.CameraInterface;

class VolumeServiceInteractorImpl
    implements VolumeServiceInteractor, CameraInterface.OnStateChangedCallback {

  private static final int NOTIFICATION_ID = 1345;
  private static final int NOTIFICATION_RC = 1009;
  @NonNull private final ZapTorchPreferences preferences;
  private final int cameraApiOld;
  private final int cameraApiLollipop;
  private final int cameraApiMarshmallow;
  @NonNull private final Context appContext;
  @NonNull private final NotificationManagerCompat notificationManagerCompat;
  @NonNull private final Notification notification;

  VolumeServiceInteractorImpl(@NonNull Context context, @NonNull ZapTorchPreferences preferences) {
    this.appContext = context.getApplicationContext();
    this.preferences = preferences;
    notificationManagerCompat = NotificationManagerCompat.from(appContext);

    // KLUDGE duplication of values between preferences and java code
    cameraApiOld = 0;
    cameraApiLollipop = 1;
    cameraApiMarshmallow = 2;

    final Intent intent = new Intent(appContext, TorchOffService.class);
    notification = new NotificationCompat.Builder(appContext).setContentTitle("Torch is On")
        .setContentText("Click to turn off")
        .setContentIntent(PendingIntent.getService(appContext, NOTIFICATION_RC, intent,
            PendingIntent.FLAG_UPDATE_CURRENT))
        .setSmallIcon(R.drawable.ic_light_notification)
        .setAutoCancel(true)
        .setColor(ContextCompat.getColor(appContext, R.color.purple500))
        .setWhen(0)
        .setOngoing(false)
        .build();
  }

  @NonNull @Override public Offloader<Long> getButtonDelayTime() {
    return new AsyncOffloader<>();
  }

  @NonNull @Override public Offloader<Boolean> shouldShowErrorDialog() {
    return null;
  }

  @NonNull @Override public CameraInterface camera() {
    final int cameraApi = preferences.getCameraApi();
    final CameraInterface camera;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && cameraApi == cameraApiMarshmallow) {
      camera = new MarshmallowCamera(appContext, this);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && cameraApi == cameraApiLollipop) {
      camera = new LollipopCamera(appContext, this);
    } else if (cameraApi == cameraApiOld) {
      camera = new OriginalCamera(appContext, this);
    } else {
      throw new RuntimeException("Invalid Camera API selected: " + cameraApi);
    }

    camera.setOnStateChangedCallback(this);
    return camera;
  }

  @Override public void onOpened() {
    notificationManagerCompat.notify(NOTIFICATION_ID, notification);
  }

  @Override public void onClosed() {
    notificationManagerCompat.cancel(NOTIFICATION_ID);
  }
}
