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

package com.pyamsoft.zaptorch.main

import android.view.ViewGroup
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.arch.UnitViewEvent
import com.pyamsoft.pydroid.arch.UnitViewState
import com.pyamsoft.zaptorch.databinding.MainContainerBinding
import javax.inject.Inject

internal class MainFrameView @Inject internal constructor(
    parent: ViewGroup
) : BaseUiView<UnitViewState, UnitViewEvent, MainContainerBinding>(parent) {

    override val viewBinding = MainContainerBinding::inflate

    override val layoutRoot by boundView { mainContainer }

    override fun onRender(state: UnitViewState) {
    }
}
