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

import android.content.ActivityNotFoundException
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.arch.BaseUiComponent
import com.pyamsoft.pydroid.arch.doOnDestroy
import com.pyamsoft.pydroid.ui.navigation.FailedNavigationPresenter
import com.pyamsoft.zaptorch.main.MainUiComponent.Callback

internal class MainUiComponentImpl internal constructor(
  private val frameView: MainFrameView,
  private val presenter: MainPresenter,
  private val failedNavigationPresenter: FailedNavigationPresenter
) : BaseUiComponent<MainUiComponent.Callback>(),
    MainUiComponent,
    MainPresenter.Callback {

  override fun id(): Int {
    return frameView.id()
  }

  override fun onBind(
    owner: LifecycleOwner,
    savedInstanceState: Bundle?,
    callback: Callback
  ) {
    owner.doOnDestroy {
      frameView.teardown()
      presenter.unbind()
    }

    frameView.inflate(savedInstanceState)
    presenter.bind(this)
  }

  override fun saveState(outState: Bundle) {
    frameView.saveState(outState)
  }

  override fun layout(
    constraintLayout: ConstraintLayout,
    aboveId: Int
  ) {
    ConstraintSet().apply {
      clone(constraintLayout)

      frameView.also {
        connect(it.id(), ConstraintSet.TOP, aboveId, ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.BOTTOM, constraintLayout.id, ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, constraintLayout.id, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, constraintLayout.id, ConstraintSet.END)
        constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      applyTo(constraintLayout)
    }
  }

  override fun failedNavigation(error: ActivityNotFoundException) {
    failedNavigationPresenter.failedNavigation(error)
  }

  override fun onHandleKeyPressChanged(handle: Boolean) {
    callback.onHandleKeyPressChanged(handle)
  }

}