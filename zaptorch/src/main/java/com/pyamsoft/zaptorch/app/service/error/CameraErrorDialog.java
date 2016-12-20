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

package com.pyamsoft.zaptorch.app.service.error;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import com.pyamsoft.zaptorch.ZapTorch;

public class CameraErrorDialog extends DialogFragment {

  @NonNull @Override public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialog.Builder(getActivity()).setMessage(
        "ZapTorch was unable to access your devices camera."
            + " Please make sure that your device has a camera with Flash functionality."
            + " Please make sure no other application is using the camera and try again.")
        .setPositiveButton("Okay", (dialogInterface, i) -> dialogInterface.dismiss())
        .create();
  }

  @Override public void onDestroyView() {
    super.onDestroyView();
    getActivity().finish();
    ZapTorch.getRefWatcher(this).watch(this);
  }
}
