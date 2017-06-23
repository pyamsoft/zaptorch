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

package com.pyamsoft.zaptorch.main

import android.support.annotation.CheckResult
import android.view.KeyEvent
import com.pyamsoft.zaptorch.base.preference.UIPreferences
import io.reactivex.Single
import timber.log.Timber

internal class MainInteractor internal constructor(private val preferences: UIPreferences) {

  @CheckResult fun shouldHandleKeys(keyCode: Int): Single<Boolean> {
    return Single.fromCallable { preferences.shouldHandleKeys() }.filter {
      val handled: Boolean
      when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_DOWN -> {
          Timber.d("Detected a Volume Down event.")
          handled = true
        }
        KeyEvent.KEYCODE_VOLUME_UP -> {
          Timber.d("Detected a Volume Up event.")
          handled = true
        }
        else -> handled = false
      }
      return@filter it && handled
    }.toSingle(false)
  }
}
