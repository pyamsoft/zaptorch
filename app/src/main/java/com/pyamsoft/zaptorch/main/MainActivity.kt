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

import android.os.Bundle
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.UiController
import com.pyamsoft.pydroid.arch.UnitControllerEvent
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.arch.fromViewModelFactory
import com.pyamsoft.pydroid.ui.changelog.ChangeLogActivity
import com.pyamsoft.pydroid.ui.changelog.buildChangeLog
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.pydroid.util.stableLayoutHideNavigation
import com.pyamsoft.zaptorch.BuildConfig
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.ZapTorchViewModelFactory
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class MainActivity : ChangeLogActivity(), UiController<UnitControllerEvent> {

  @JvmField @Inject internal var toolbar: MainToolbarView? = null

  @JvmField @Inject internal var mainView: MainFrameView? = null

  @JvmField @Inject internal var theming: Theming? = null

  @JvmField @Inject internal var factory: ZapTorchViewModelFactory? = null
  private val viewModel by fromViewModelFactory<ToolbarViewModel> { factory?.create(this) }

  private var stateSaver: StateSaver? = null

  override val versionName: String = BuildConfig.VERSION_NAME

  override val applicationIcon: Int = R.mipmap.ic_launcher

  override val snackbarRoot: ViewGroup by lazy(NONE) {
    findViewById<CoordinatorLayout>(R.id.snackbar_root)
  }

  override val fragmentContainerId: Int
    get() = requireNotNull(mainView).id()

  override val changelog = buildChangeLog {
    bugfix("When the flashlight is unavailable, the service will not attempt to keep connecting.")
    feature("Enable and Disable the Torch command on ↓ ↓↑")
    feature("Enable and Disable the Torch command on ↑ ↑")
    feature("Enable and Disable the Torch command on ↓ ↑")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_ZapTorch)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.snackbar_screen)

    val layoutRoot = findViewById<ConstraintLayout>(R.id.content_root)
    Injector.obtainFromApplication<ZapTorchComponent>(this)
        .plusMainComponent()
        .create(this, layoutRoot, this) { requireNotNull(theming).isDarkTheme(this) }
        .inject(this)

    val component = requireNotNull(mainView)
    val toolbarComponent = requireNotNull(toolbar)
    val dropshadow = DropshadowView.create(layoutRoot)

    stableLayoutHideNavigation()

    stateSaver =
        createComponent(
            savedInstanceState, this, viewModel, this, component, toolbarComponent, dropshadow) {
          // TODO Handle any controller events
        }

    layoutRoot.layout {
      toolbarComponent.also {
        connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      dropshadow.also {
        connect(it.id(), ConstraintSet.TOP, toolbarComponent.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      component.also {
        connect(it.id(), ConstraintSet.TOP, toolbarComponent.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }
    }

    showMainFragment()
  }

  override fun onControllerEvent(event: UnitControllerEvent) {}

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    stateSaver?.saveState(outState)
  }

  override fun onDestroy() {
    super.onDestroy()
    stateSaver = null
    mainView = null
    toolbar = null
    factory = null
  }

  private fun showMainFragment() {
    val fm = supportFragmentManager
    if (fm.findFragmentByTag(MainFragment.TAG) == null) {
      fm.commit(this) { add(fragmentContainerId, MainFragment(), MainFragment.TAG) }
    }
  }
}
