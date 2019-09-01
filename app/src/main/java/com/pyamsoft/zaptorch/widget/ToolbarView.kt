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

package com.pyamsoft.zaptorch.widget

import android.os.Bundle
import com.pyamsoft.pydroid.arch.UiSavedState
import com.pyamsoft.pydroid.arch.UiView
import com.pyamsoft.pydroid.arch.UiViewEvent
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.pydroid.ui.app.ToolbarActivity
import com.pyamsoft.pydroid.ui.arch.InvalidIdException
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.zaptorch.R
import javax.inject.Inject

internal class ToolbarView<S : UiViewState, E : UiViewEvent> @Inject internal constructor(
    private val toolbarActivity: ToolbarActivity
) : UiView<S, E>() {

    override fun id(): Int {
        throw InvalidIdException
    }

    override fun doInflate(savedInstanceState: Bundle?) {
        toolbarActivity.withToolbar { toolbar ->
            toolbar.setTitle(R.string.app_name)
            toolbar.setUpEnabled(false)
        }
    }

    override fun render(
        state: S,
        savedState: UiSavedState
    ) {
    }
}
