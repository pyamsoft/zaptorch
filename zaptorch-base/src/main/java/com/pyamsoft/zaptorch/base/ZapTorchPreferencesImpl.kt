/*
 * Copyright 2019 Peter Kenji Yamanaka
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
 *
 */

package com.pyamsoft.zaptorch.base

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.annotation.CheckResult
import androidx.preference.PreferenceManager
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.ClearPreferences
import com.pyamsoft.zaptorch.api.UIPreferences
import com.pyamsoft.zaptorch.api.UIPreferences.PreferenceUnregister
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
internal class ZapTorchPreferencesImpl @Inject internal constructor(
    context: Context,
    private val enforcer: Enforcer
) : CameraPreferences, ClearPreferences, UIPreferences {

    private val doublePressDelayKey: String
    private val doublePressDelayDefault: String

    private val displayCameraErrorsKey: String
    private val displayCameraErrorsDefault: Boolean

    private val handleVolumeKeysKey: String
    private val handleVolumeKeysDefault: Boolean

    private val preferences by lazy {
        enforcer.assertNotOnMainThread()
        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    }

    init {
        context.applicationContext.resources.apply {
            doublePressDelayKey = getString(R.string.double_press_delay_key)
            doublePressDelayDefault = getString(R.string.double_press_delay_default)

            displayCameraErrorsKey = getString(R.string.display_camera_errors_key)
            displayCameraErrorsDefault = getBoolean(R.bool.display_camera_errors_default)

            handleVolumeKeysKey = getString(R.string.handle_volume_keys_key)
            handleVolumeKeysDefault = getBoolean(R.bool.handle_volume_keys_default)
        }
    }

    override suspend fun getButtonDelayTime(): Long {
        enforcer.assertNotOnMainThread()
        return preferences.getString(doublePressDelayKey, doublePressDelayDefault).orEmpty()
            .toLong()
    }

    override suspend fun shouldShowErrorDialog(): Boolean {
        enforcer.assertNotOnMainThread()
        return preferences.getBoolean(displayCameraErrorsKey, displayCameraErrorsDefault)
    }

    override suspend fun shouldHandleKeys(onChange: (handle: Boolean) -> Unit): PreferenceUnregister {
        return withContext(context = Dispatchers.Default) {
            registerPreferenceListener(OnSharedPreferenceChangeListener { _, key ->
                if (key == handleVolumeKeysKey) {
                    launch(context = Dispatchers.Default) {
                        onChange(preferences.getBoolean(handleVolumeKeysKey, handleVolumeKeysDefault))
                    }
                }
            })
        }
    }

    @CheckResult
    private fun registerPreferenceListener(l: OnSharedPreferenceChangeListener): PreferenceUnregister {
        enforcer.assertNotOnMainThread()
        preferences.registerOnSharedPreferenceChangeListener(l)

        return object : PreferenceUnregister {

            override fun unregister() {
                preferences.unregisterOnSharedPreferenceChangeListener(l)
            }
        }
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun clearAll() {
        enforcer.assertNotOnMainThread()

        // Commit because we must be sure transaction takes place before we continue
        preferences.edit()
            .clear()
            .commit()
    }
}
