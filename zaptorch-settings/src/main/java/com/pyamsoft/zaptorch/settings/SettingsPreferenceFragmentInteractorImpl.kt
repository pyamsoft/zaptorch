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

package com.pyamsoft.zaptorch.settings

import com.pyamsoft.zaptorch.api.ClearPreferences
import com.pyamsoft.zaptorch.api.SettingsPreferenceFragmentInteractor
import io.reactivex.Single

internal class SettingsPreferenceFragmentInteractorImpl internal constructor(
  private val clearPreferences: ClearPreferences
) :
    SettingsPreferenceFragmentInteractor {

  override fun clearAll(): Single<Boolean> {
    return Single.fromCallable {
      clearPreferences.clearAll()
      return@fromCallable true
    }
  }
}
