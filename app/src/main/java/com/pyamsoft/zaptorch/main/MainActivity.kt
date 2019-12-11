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
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProvider
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.about.AboutFragment
import com.pyamsoft.pydroid.ui.arch.factory
import com.pyamsoft.pydroid.ui.rating.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.rating.RatingActivity
import com.pyamsoft.pydroid.ui.rating.buildChangeLog
import com.pyamsoft.pydroid.ui.theme.ThemeProvider
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.commit
import com.pyamsoft.pydroid.ui.util.layout
import com.pyamsoft.pydroid.ui.widget.shadow.DropshadowView
import com.pyamsoft.zaptorch.BuildConfig
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.main.ToolbarControllerEvent.HandleKeypress
import timber.log.Timber
import javax.inject.Inject
import kotlin.LazyThreadSafetyMode.NONE

class MainActivity : RatingActivity() {

    @JvmField
    @Inject
    internal var factory: ViewModelProvider.Factory? = null
    @JvmField
    @Inject
    internal var toolbar: MainToolbarView? = null
    @JvmField
    @Inject
    internal var mainView: MainFrameView? = null
    @JvmField
    @Inject
    internal var theming: Theming? = null

    private val viewModel by factory<MainToolbarViewModel> { factory }

    private var handleKeyPress: Boolean = false

    override val versionName: String = BuildConfig.VERSION_NAME

    override val applicationIcon: Int = R.mipmap.ic_launcher

    override val snackbarRoot: ViewGroup by lazy(NONE) {
        findViewById<CoordinatorLayout>(R.id.snackbar_root)
    }

    override val fragmentContainerId: Int
        get() = requireNotNull(mainView).id()

    override val changeLogLines: ChangeLogBuilder = buildChangeLog {
        change("Lower memory consumption and faster operation")
        bugfix("Fixed a memory leak in the license viewing experience")
        feature("Added links to Terms of Service and Privacy Policy")
    }

    override val changeLogTheme: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ZapTorch)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.snackbar_screen)

        val layoutRoot = findViewById<ConstraintLayout>(R.id.content_root)
        Injector.obtain<ZapTorchComponent>(applicationContext)
            .plusMainComponent()
            .create(
                layoutRoot,
                this,
                ThemeProvider { requireNotNull(theming).isDarkTheme(this) }
            )
            .inject(this)

        val component = requireNotNull(mainView)
        val toolbarComponent = requireNotNull(toolbar)
        val dropshadow = DropshadowView.create(layoutRoot)

        createComponent(
            savedInstanceState, this,
            viewModel,
            component,
            toolbarComponent,
            dropshadow
        ) {
            return@createComponent when (it) {
                is HandleKeypress -> onHandleKeyPressChanged(it.isHandling)
            }
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
                connect(
                    it.id(),
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM
                )
                connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constrainHeight(it.id(), ConstraintSet.MATCH_CONSTRAINT)
                constrainWidth(it.id(), ConstraintSet.MATCH_CONSTRAINT)
            }
        }

        showMainFragment()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        toolbar?.saveState(outState)
        mainView?.saveState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainView = null
        toolbar = null
        factory = null
    }

    private fun onHandleKeyPressChanged(handle: Boolean) {
        Timber.d("Handle keypress: $handle")
        handleKeyPress = handle
    }

    private fun showMainFragment() {
        val fm = supportFragmentManager
        if (fm.findFragmentByTag(MainFragment.TAG) == null && !AboutFragment.isPresent(this)) {
            fm.commit(this) {
                add(fragmentContainerId, MainFragment(), MainFragment.TAG)
            }
        }
    }

    override fun onKeyUp(
        keyCode: Int,
        event: KeyEvent
    ): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            handleKeyPress
        } else {
            super.onKeyUp(keyCode, event)
        }
    }

    override fun onKeyDown(
        keyCode: Int,
        event: KeyEvent
    ): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            handleKeyPress
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}
