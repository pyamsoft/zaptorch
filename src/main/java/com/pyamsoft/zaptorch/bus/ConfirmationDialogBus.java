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

package com.pyamsoft.zaptorch.bus;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.android.annotations.VisibleForTesting;
import com.pyamsoft.pydroid.tool.Bus;
import com.pyamsoft.pydroid.tool.BusImpl;
import com.pyamsoft.zaptorch.model.event.ConfirmationEvent;

public class ConfirmationDialogBus implements Bus<ConfirmationEvent> {

  @NonNull private static final ConfirmationDialogBus instance = new ConfirmationDialogBus();
  @NonNull private Bus<ConfirmationEvent> delegate;

  private ConfirmationDialogBus() {
    delegate = new BusImpl<>();
  }

  @VisibleForTesting static void setInstance(@NonNull Bus<ConfirmationEvent> delegate) {
    instance.setDelegate(delegate);
  }

  @CheckResult @NonNull public static ConfirmationDialogBus get() {
    return instance;
  }

  @SuppressWarnings("WeakerAccess") @VisibleForTesting void setDelegate(
      @NonNull Bus<ConfirmationEvent> delegate) {
    this.delegate = delegate;
  }

  @Override public void post(@NonNull ConfirmationEvent event) {
    delegate.post(event);
  }

  @NonNull @Override
  public Event<ConfirmationEvent> register(@NonNull Event<ConfirmationEvent> onCall) {
    return delegate.register(onCall);
  }

  @NonNull @Override
  public Event<ConfirmationEvent> register(@NonNull Event<ConfirmationEvent> onCall,
      @Nullable Error onError) {
    return delegate.register(onCall, onError);
  }

  @Override public void unregister(@Nullable Event<ConfirmationEvent> onCall) {
    delegate.unregister(onCall);
  }
}
