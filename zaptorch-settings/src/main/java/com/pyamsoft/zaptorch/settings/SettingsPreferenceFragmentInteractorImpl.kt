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

package com.pyamsoft.zaptorch.settings

import android.content.SharedPreferences
import com.pyamsoft.zaptorch.base.preference.ClearPreferences
import com.pyamsoft.zaptorch.base.preference.UIPreferences
import io.reactivex.Single

internal class SettingsPreferenceFragmentInteractorImpl internal constructor(
    private val preferences: UIPreferences,
    private val clearPreferences: ClearPreferences) : SettingsPreferenceFragmentInteractor {

  override fun clearAll(): Single<Boolean> {
    return Single.fromCallable {
      clearPreferences.clearAll()
      return@fromCallable true
    }
  }

  override fun registerCameraApiListener(
      cameraApiListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    unregisterCameraApiListener(cameraApiListener)
    if (cameraApiListener != null) {
      preferences.register(cameraApiListener)
    }
  }

  override fun unregisterCameraApiListener(
      cameraApiListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
    if (cameraApiListener != null) {
      preferences.unregister(cameraApiListener)
    }
  }
}