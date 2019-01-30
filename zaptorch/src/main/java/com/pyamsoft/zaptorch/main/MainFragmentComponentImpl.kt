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

package com.pyamsoft.zaptorch.main

import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.loader.LoaderModule
import com.pyamsoft.zaptorch.service.ServiceStateWorker
import com.pyamsoft.zaptorch.service.VolumeServiceModule
import com.pyamsoft.zaptorch.settings.SettingsStateEvent

internal class MainFragmentComponentImpl internal constructor(
  private val parent: ViewGroup,
  private val owner: LifecycleOwner,
  private val loaderModule: LoaderModule,
  private val serviceModule: VolumeServiceModule,
  private val actionViewBus: EventBus<ActionViewEvent>,
  private val settingsStateBus: EventBus<SettingsStateEvent>,
  private val mainStateBus: EventBus<MainStateEvent>
) : MainFragmentComponent {

  override fun inject(fragment: MainFragment) {
    val actionView = MainActionView(loaderModule.provideImageLoader(), owner, parent, actionViewBus)
    val frame = MainFrameView(parent, owner)
    val serviceStateWorker = ServiceStateWorker(serviceModule.interactor)

    fragment.frameComponent = MainFrameUiComponent(mainStateBus, frame, owner)
    fragment.actionComponent = MainActionUiComponent(
        settingsStateBus, serviceStateWorker, actionView, owner
    )
  }

}

