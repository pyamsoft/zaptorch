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

package com.pyamsoft.zaptorch.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.CheckResult
import android.support.v7.preference.PreferenceManager
import com.pyamsoft.zaptorch.base.preference.CameraPreferences
import com.pyamsoft.zaptorch.base.preference.ClearPreferences
import com.pyamsoft.zaptorch.base.preference.UIPreferences

internal class ZapTorchPreferencesImpl(
        context: Context) : CameraPreferences, ClearPreferences, UIPreferences {

    private val doublePressDelayKey: String
    private val displayCameraErrorsKey: String
    private val handleVolumeKeysKey: String
    private val doublePressDelayDefault: String
    private val cameraApiKey: String
    private val cameraApiDefault: String
    private val preferences: SharedPreferences
    private val displayCameraErrorsDefault: Boolean
    private val handleVolumeKeysDefault: Boolean

    init {
        val appContext = context.applicationContext
        preferences = PreferenceManager.getDefaultSharedPreferences(appContext)
        val res = appContext.resources
        doublePressDelayKey = appContext.getString(R.string.double_press_delay_key)
        displayCameraErrorsKey = appContext.getString(R.string.display_camera_errors_key)
        handleVolumeKeysKey = appContext.getString(R.string.handle_volume_keys_key)
        doublePressDelayDefault = appContext.getString(R.string.double_press_delay_default)
        displayCameraErrorsDefault = res.getBoolean(R.bool.display_camera_errors_default)
        handleVolumeKeysDefault = res.getBoolean(R.bool.handle_volume_keys_default)
        cameraApiKey = appContext.getString(R.string.camera_api_key)
        cameraApiDefault = appContext.getString(R.string.camera_api_default)
    }

    override val buttonDelayTime: Long
        @CheckResult get() = java.lang.Long.parseLong(
                preferences.getString(doublePressDelayKey, doublePressDelayDefault))

    @CheckResult override fun shouldShowErrorDialog(): Boolean =
            preferences.getBoolean(displayCameraErrorsKey, displayCameraErrorsDefault)

    @CheckResult override fun shouldHandleKeys(): Boolean =
            preferences.getBoolean(handleVolumeKeysKey, handleVolumeKeysDefault)

    override val cameraApi: Int
        @CheckResult get() = Integer.parseInt(preferences.getString(cameraApiKey, cameraApiDefault))

    @SuppressLint("CommitPrefEdits")
    override fun clearAll() {
        // Commit because we must be sure transaction takes place before we continue
        preferences.edit().clear().commit()
    }

    override fun register(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregister(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        preferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
