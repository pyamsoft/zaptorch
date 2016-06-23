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

package com.pyamsoft.zaptorch.app.frag;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import com.pyamsoft.zaptorch.dagger.RxBus;

public class ConfirmationDialog extends DialogFragment {

  public static ConfirmationDialog newInstance() {
    final ConfirmationDialog fragment = new ConfirmationDialog();
    final Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @NonNull @Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    return new AlertDialog.Builder(getActivity()).setMessage(
        "Really clear all application settings?").setPositiveButton("Yes", (dialogInterface, i) -> {
      dialogInterface.dismiss();
      ConfirmationDialogBus.get().post(new ConfirmationEvent(false));
    }).setNegativeButton("No", (dialogInterface, i) -> {
      dialogInterface.dismiss();
    }).create();
  }

  public static final class ConfirmationEvent {
    private final boolean complete;

    public ConfirmationEvent(boolean complete) {
      this.complete = complete;
    }

    public boolean isComplete() {
      return complete;
    }
  }

  public static final class ConfirmationDialogBus extends RxBus<ConfirmationEvent> {

    @NonNull private static final ConfirmationDialogBus instance = new ConfirmationDialogBus();

    @CheckResult @NonNull public static ConfirmationDialogBus get() {
      return instance;
    }
  }
}
