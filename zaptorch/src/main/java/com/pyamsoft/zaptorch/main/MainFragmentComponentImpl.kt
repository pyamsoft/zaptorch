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
import com.pyamsoft.pydroid.core.bus.EventBus
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.app.requireToolbarActivity
import com.pyamsoft.zaptorch.api.VolumeServiceInteractor
import com.pyamsoft.zaptorch.service.ServiceStatePresenterImpl
import com.pyamsoft.zaptorch.settings.SignificantScrollEvent
import com.pyamsoft.zaptorch.widget.ToolbarView

internal class MainFragmentComponentImpl internal constructor(
  private val parent: ViewGroup,
  private val imageLoader: ImageLoader,
  private val interactor: VolumeServiceInteractor,
  private val scrollBus: EventBus<SignificantScrollEvent>
) : MainFragmentComponent {

  override fun inject(fragment: MainFragment) {
    val mainPresenter = MainFragmentPresenterImpl(scrollBus)
    val actionView = MainActionView(imageLoader, parent, mainPresenter)
    val frameView = MainFrameView(parent)
    val toolbarView = ToolbarView(fragment.requireToolbarActivity())
    val serviceStatePresenter = ServiceStatePresenterImpl(interactor)

    fragment.apply {
      this.component = MainFragmentUiComponentImpl(
          mainPresenter, serviceStatePresenter, frameView, actionView
      )
      this.toolbarView = toolbarView
    }
  }

}

