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
import androidx.preference.PreferenceManager
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.util.PreferenceListener
import com.pyamsoft.pydroid.util.onChange
import com.pyamsoft.zaptorch.api.CameraPreferences
import com.pyamsoft.zaptorch.api.ClearPreferences
import com.pyamsoft.zaptorch.api.UIPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class ZapTorchPreferencesImpl @Inject internal constructor(
    context: Context
) : CameraPreferences, ClearPreferences, UIPreferences {

    private val doublePressDelayKey: String
    private val doublePressDelayDefault: String

    private val displayCameraErrorsKey: String
    private val displayCameraErrorsDefault: Boolean

    private val handleVolumeKeysKey: String
    private val handleVolumeKeysDefault: Boolean

    private val preferences by lazy {
        Enforcer.assertOffMainThread()
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

    override suspend fun getButtonDelayTime(): Long = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.getString(doublePressDelayKey, doublePressDelayDefault)
            .orEmpty()
            .toLong()
    }

    override suspend fun shouldShowErrorDialog(): Boolean =
        withContext(context = Dispatchers.Default) {
            Enforcer.assertOffMainThread()
            return@withContext preferences.getBoolean(
                displayCameraErrorsKey,
                displayCameraErrorsDefault
            )
        }

    override suspend fun shouldHandleKeys(): Boolean = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.getBoolean(handleVolumeKeysKey, handleVolumeKeysDefault)
    }

    override suspend fun watchHandleKeys(onChange: (handle: Boolean) -> Unit): PreferenceListener =
        withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()
            return@withContext preferences.onChange(handleVolumeKeysKey) {
                onChange(shouldHandleKeys())
            }
        }

    @SuppressLint("ApplySharedPref")
    override suspend fun clearAll() = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()

        // Commit because we must be sure transaction takes place before we continue
        preferences.edit()
            .clear()
            .commit()

        return@withContext
    }
}
