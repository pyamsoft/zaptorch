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

package com.pyamsoft.zaptorch.app.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

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
      sendConfirmationEvent();
      dialogInterface.dismiss();
    }).setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss()).create();
  }

  void sendConfirmationEvent() {
    final FragmentManager fragmentManager = getFragmentManager();
    final Fragment settingsPreferenceFragment =
        fragmentManager.findFragmentByTag(SettingsPreferenceFragment.TAG);
    if (settingsPreferenceFragment instanceof SettingsPreferenceFragment) {
      ((SettingsPreferenceFragment) settingsPreferenceFragment).getPresenter()
          .processClearRequest();
    } else {
      throw new ClassCastException("Fragment is not SettingsPreferenceFragment");
    }
  }
}
