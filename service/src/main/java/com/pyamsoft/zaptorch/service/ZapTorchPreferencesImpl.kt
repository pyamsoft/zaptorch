/*
 * Copyright 2020 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.service

import android.annotation.SuppressLint
import android.content.Context
import androidx.preference.PreferenceManager
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.zaptorch.core.ClearPreferences
import com.pyamsoft.zaptorch.core.TorchPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class ZapTorchPreferencesImpl @Inject internal constructor(context: Context) :
    TorchPreferences, ClearPreferences {

  private val doublePressDelayKey: String
  private val doublePressDelayDefault: String

  private val torchEnabledKey: String
  private val torchEnabledDefault: Boolean

  private val pulseEnabledKey: String
  private val pulseEnabledDefault: Boolean

  private val strobeEnabledKey: String
  private val strobeEnabledDefault: Boolean

  private val preferences by lazy {
    Enforcer.assertOffMainThread()
    PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
  }

  init {
    context.applicationContext.resources.apply {
      doublePressDelayKey = getString(R.string.double_press_delay_key)
      doublePressDelayDefault = getString(R.string.double_press_delay_default)

      torchEnabledKey = getString(R.string.torch_enabled_key)
      torchEnabledDefault = getBoolean(R.bool.torch_enabled_default)

      pulseEnabledKey = getString(R.string.pulse_enabled_key)
      pulseEnabledDefault = getBoolean(R.bool.pulse_enabled_default)

      strobeEnabledKey = getString(R.string.strobe_enabled_key)
      strobeEnabledDefault = getBoolean(R.bool.strobe_enabled_default)
    }
  }

  override suspend fun getButtonDelayTime(): Long =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences
            .getString(doublePressDelayKey, doublePressDelayDefault)
            .orEmpty()
            .toLong()
      }

  override suspend fun isTorchEnabled(): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.getBoolean(torchEnabledKey, torchEnabledDefault)
      }

  override suspend fun isPulseEnabled(): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.getBoolean(pulseEnabledKey, pulseEnabledDefault)
      }

  override suspend fun isStrobeEnabled(): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext preferences.getBoolean(strobeEnabledKey, strobeEnabledDefault)
      }

  @SuppressLint("ApplySharedPref")
  override suspend fun clearAll() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        // Commit because we must be sure transaction takes place before we continue
        preferences.edit().clear().commit()

        return@withContext
      }
}
