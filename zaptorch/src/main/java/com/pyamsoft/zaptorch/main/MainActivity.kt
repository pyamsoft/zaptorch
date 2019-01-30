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

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.pyamsoft.pydroid.ui.about.AboutFragment
import com.pyamsoft.pydroid.ui.arch.destroy
import com.pyamsoft.pydroid.ui.rating.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.rating.RatingActivity
import com.pyamsoft.pydroid.ui.rating.buildChangeLog
import com.pyamsoft.pydroid.ui.theme.ThemeInjector
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowUiComponent
import com.pyamsoft.pydroid.util.hyperlink
import com.pyamsoft.zaptorch.BuildConfig
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.main.MainViewEvent.MenuItemClicked
import com.pyamsoft.zaptorch.main.MainViewEvent.ToolbarClicked
import timber.log.Timber
import kotlin.LazyThreadSafetyMode.NONE

class MainActivity : RatingActivity() {

  internal lateinit var toolbarComponent: MainToolbarUiComponent
  internal lateinit var frameComponent: MainFrameUiComponent
  internal lateinit var dropshadowComponent: DropshadowUiComponent
  internal lateinit var worker: MainWorker

  private var handleKeyPress: Boolean = false

  private val layoutRoot by lazy(NONE) {
    findViewById<ConstraintLayout>(R.id.layout_constraint)
  }

  override val versionName: String = BuildConfig.VERSION_NAME

  override val applicationIcon: Int = R.mipmap.ic_launcher

  override val snackbarRoot: View
    get() = layoutRoot

  override val fragmentContainerId: Int
    get() = frameComponent.id()

  override val changeLogLines: ChangeLogBuilder = buildChangeLog {
    change("New icon style")
    change("Better open source license viewing experience")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    if (ThemeInjector.obtain(applicationContext).isDarkTheme()) {
      setTheme(R.style.Theme_ZapTorch_Dark)
    } else {
      setTheme(R.style.Theme_ZapTorch_Light)
    }
    super.onCreate(savedInstanceState)
    setContentView(R.layout.layout_constraint)

    Injector.obtain<ZapTorchComponent>(applicationContext)
        .plusMainComponent(layoutRoot, this)
        .inject(this)

    createComponents(savedInstanceState)
    layoutComponents(layoutRoot)
    showMainFragment()

    worker.onHandleKeyPressChanged { onHandleKeyPress(it) }
        .destroy(this)
  }

  private fun createComponents(savedInstanceState: Bundle?) {
    toolbarComponent.onUiEvent {
      return@onUiEvent when (it) {
        is ToolbarClicked -> onBackPressed()
        is MenuItemClicked -> onMenuItemClicked(it.item)
      }
    }
        .destroy(this)

    toolbarComponent.create(savedInstanceState)
    frameComponent.create(savedInstanceState)
    dropshadowComponent.create(savedInstanceState)
  }

  private fun onMenuItemClicked(item: MenuItem) {
    val itemId = item.itemId
    if (itemId == R.id.menu_id_privacy_policy) {
      val hyperlink = PRIVACY_POLICY_URL.hyperlink(this)
      val error = hyperlink.navigate()
      if (error != null) {
        worker.publishPrivacyPolicyLinkError(error)
      }
    }

  }

  private fun layoutComponents(layoutRoot: ConstraintLayout) {
    ConstraintSet().apply {
      clone(layoutRoot)

      toolbarComponent.also {
        connect(it.id(), ConstraintSet.TOP, layoutRoot.id, ConstraintSet.TOP)
        connect(it.id(), ConstraintSet.START, layoutRoot.id, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, layoutRoot.id, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      frameComponent.also {
        connect(it.id(), ConstraintSet.TOP, toolbarComponent.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.BOTTOM, layoutRoot.id, ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, layoutRoot.id, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, layoutRoot.id, ConstraintSet.END)
        constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      dropshadowComponent.also {
        connect(it.id(), ConstraintSet.TOP, toolbarComponent.id(), ConstraintSet.BOTTOM)
        connect(it.id(), ConstraintSet.START, layoutRoot.id, ConstraintSet.START)
        connect(it.id(), ConstraintSet.END, layoutRoot.id, ConstraintSet.END)
        constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
      }

      applyTo(layoutRoot)
    }
  }

  private fun showMainFragment() {
    val fm = supportFragmentManager
    if (fm.findFragmentByTag(MainFragment.TAG) == null && !AboutFragment.isPresent(this)) {
      fm.beginTransaction()
          .add(fragmentContainerId, MainFragment(), MainFragment.TAG)
          .commit(this)
    }
  }

  private fun onHandleKeyPress(handle: Boolean) {
    Timber.d("Handle keypress: %s", handle)
    handleKeyPress = handle
  }

  override fun onKeyUp(
    keyCode: Int,
    event: KeyEvent
  ): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      return handleKeyPress
    } else {
      return super.onKeyUp(keyCode, event)
    }
  }

  override fun onKeyDown(
    keyCode: Int,
    event: KeyEvent
  ): Boolean {
    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      return handleKeyPress
    } else {
      return super.onKeyDown(keyCode, event)
    }
  }

  companion object {

    private const val PRIVACY_POLICY_URL =
      "https://pyamsoft.blogspot.com/p/zaptorch-privacy-policy.html"
  }
}
