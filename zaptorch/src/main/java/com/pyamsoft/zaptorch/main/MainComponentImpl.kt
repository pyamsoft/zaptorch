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
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowUiComponent

internal class MainComponentImpl internal constructor(
  private val theming: Theming,
  private val parent: ViewGroup,
  private val owner: LifecycleOwner,
  private val mainModule: MainModule,
  private val mainViewBus: EventBus<MainViewEvent>,
  private val mainStateBus: EventBus<MainStateEvent>
) : MainComponent {

  override fun inject(activity: MainActivity) {
    val frame = MainFrameView(parent, owner)
    val toolbar = MainToolbarView(activity, theming, parent, mainViewBus)
    activity.frameComponent = MainFrameUiComponent(mainStateBus, frame, owner)
    activity.dropshadowComponent = DropshadowUiComponent.create(parent, owner)
    activity.toolbarComponent = MainToolbarUiComponent(toolbar, owner)
    activity.worker = MainWorker(mainModule.interactor, mainStateBus)
  }
}
