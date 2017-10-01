/*
 *     Copyright (C) 2017 Peter Kenji Yamanaka
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.pyamsoft.zaptorch.main

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.pyamsoft.zaptorch.base.preference.UIPreferences
import io.reactivex.Single

internal class MainInteractorImpl internal constructor(
    private val preferences: UIPreferences) : MainInteractor {

  private var listener: SharedPreferences.OnSharedPreferenceChangeListener? = null

  override fun register(handleKeyPressKey: String, onHandleChanged: (Boolean) -> Unit) {
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
