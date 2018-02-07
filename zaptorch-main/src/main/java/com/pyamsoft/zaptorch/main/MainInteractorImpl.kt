/*
 * Copyright (C) 2018 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.zaptorch.main

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.pyamsoft.zaptorch.api.MainInteractor
import com.pyamsoft.zaptorch.api.UIPreferences
import io.reactivex.Single

internal class MainInteractorImpl internal constructor(
  private val preferences: UIPreferences
) : MainInteractor {

  private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

  override fun register(
    handleKeyPressKey: String,
    onHandleChanged: (Boolean) -> Unit
  ) {
    if (listener == null) {
      listener = OnSharedPreferenceChangeListener { _, key ->
        if (key == handleKeyPressKey) {
          onHandleChanged(preferences.shouldHandleKeys())
        }
      }
    }

    val obj = listener
    if (obj != null) {
      preferences.register(obj)
    }
  }

  override fun unregister() {
    val obj = listener
    if (obj != null) {
      preferences.unregister(obj)
    }
    listener = null
  }

  override fun shouldHandleKeys(): Single<Boolean> =
    Single.fromCallable { preferences.shouldHandleKeys() }
}
