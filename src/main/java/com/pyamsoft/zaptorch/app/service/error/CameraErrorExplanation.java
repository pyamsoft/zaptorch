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

import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import com.pyamsoft.pydroid.util.AppUtil;

public final class CameraErrorExplanation extends AppCompatActivity {

  @NonNull public static final String DIALOG_WHICH = "dialog";
  public static final int TYPE_NONE = -1;
  public static final int TYPE_PERMISSION = 0;
  public static final int TYPE_ERROR = 1;

  @Override protected void onPostResume() {
    super.onPostResume();
    final int type = getIntent().getIntExtra(DIALOG_WHICH, TYPE_NONE);
    DialogFragment fragment;
    switch (type) {
      case TYPE_PERMISSION:
        fragment = new CameraErrorDialog();
        break;
      case TYPE_ERROR:
        fragment = new CameraErrorDialog();
        break;
      default:
        fragment = null;
    }
    if (fragment != null) {
      AppUtil.guaranteeSingleDialogFragment(getSupportFragmentManager(), fragment, "error");
    }
  }
}
