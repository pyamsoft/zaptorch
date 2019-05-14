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

package com.pyamsoft.zaptorch.settings

import com.pyamsoft.pydroid.arch.UiControllerEvent
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState

data class SettingsViewState(val throwable: Throwable?) : UiViewState

sealed class SettingsViewEvent : UiViewEvent {

  object ShowExplanation : SettingsViewEvent()

  data class SignificantScroll(val visible: Boolean) : SettingsViewEvent()

}

sealed class SettingsControllerEvent : UiControllerEvent {

  object Explain : SettingsControllerEvent()

  object ClearAll : SettingsControllerEvent()

}

