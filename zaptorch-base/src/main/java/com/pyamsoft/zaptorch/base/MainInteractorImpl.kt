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

import com.pyamsoft.pydroid.arch.EventConsumer
import com.pyamsoft.zaptorch.api.MainInteractor
import com.pyamsoft.zaptorch.api.UIPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class MainInteractorImpl @Inject internal constructor(
    private val preferences: UIPreferences
) : MainInteractor {

    override fun onHandleKeyPressChanged(): EventConsumer<Boolean> {
        return preferences.shouldHandleKeys()
    }
}

