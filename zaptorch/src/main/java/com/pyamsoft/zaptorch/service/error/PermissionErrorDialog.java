/*
 * Copyright 2017 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.service.error;

import android.Manifest;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import com.pyamsoft.zaptorch.ZapTorch;

public class PermissionErrorDialog extends DialogFragment {

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialog.Builder(getActivity()).setMessage(
        "ZapTorch was unable to access your devices setupCamera."
            + " Please grant ZapTorch the 'Camera' permission")
        .setPositiveButton("Okay", (dialogInterface, i) -> {
          dialogInterface.dismiss();
          ActivityCompat.requestPermissions(getActivity(),
              new String[] { Manifest.permission.CAMERA }, 5000);
        })
        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
        .create();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    getActivity().finish();
    ZapTorch.getRefWatcher(this).watch(this);
  }
}
