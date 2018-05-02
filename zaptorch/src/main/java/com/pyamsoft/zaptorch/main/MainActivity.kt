/*
 * Copyright (C) 2018 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.zaptorch.main

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.preference.PreferenceManager
import android.view.KeyEvent
import android.view.View
import com.pyamsoft.pydroid.ui.about.AboutLibrariesFragment
import com.pyamsoft.pydroid.ui.rating.ChangeLogBuilder
import com.pyamsoft.pydroid.ui.rating.RatingActivity
import com.pyamsoft.pydroid.ui.rating.buildChangeLog
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.pydroid.ui.util.Snackbreak
import com.pyamsoft.pydroid.ui.util.Snackbreak.ErrorDetail
import com.pyamsoft.pydroid.ui.util.animateMenu
import com.pyamsoft.pydroid.util.hyperlink
import com.pyamsoft.pydroid.util.toDp
import com.pyamsoft.zaptorch.BuildConfig
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : RatingActivity(), MainPresenter.View {

  internal lateinit var presenter: MainPresenter
  private lateinit var binding: ActivityMainBinding
  private var handleKeyPress: Boolean = false

  override val versionName: String = BuildConfig.VERSION_NAME

  override val applicationIcon: Int = R.mipmap.ic_launcher

  override val currentApplicationVersion: Int = BuildConfig.VERSION_CODE

  override val applicationName: String
    get() = getString(R.string.app_name)

  override val rootView: View
    get() = binding.root

  override val changeLogLines: ChangeLogBuilder = buildChangeLog {
    bugfix("Smoother animations")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_ZapTorch)
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
    PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)

    Injector.obtain<ZapTorchComponent>(applicationContext)
        .plusMainComponent(getString(R.string.handle_volume_keys_key))
        .inject(this)
    setupToolbar()

    presenter.bind(this, this)
  }

  override fun onStart() {
    super.onStart()
    showMainFragment()
  }

  override fun onHandleKeyPress(handle: Boolean) {
    handleKeyPress = handle
    Timber.d("Handle keypress: %s", handle)
  }

  override fun onError(throwable: Throwable) {
    Snackbreak.short(this, rootView, ErrorDetail("Error", throwable.localizedMessage))
  }

  override fun onDestroy() {
    super.onDestroy()
    binding.unbind()
  }

  override fun onResume() {
    super.onResume()
    binding.toolbar.animateMenu()
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

  private fun showMainFragment() {
    val fragmentManager = supportFragmentManager
    if (fragmentManager.findFragmentByTag(MainFragment.TAG) == null
        && !AboutLibrariesFragment.isPresent(this)
    ) {
      fragmentManager.beginTransaction()
          .add(R.id.main_viewport, MainFragment(), MainFragment.TAG)
          .commit()
    }
  }

  private fun setupToolbar() {
    val self = this
    binding.toolbar.apply {
      setToolbar(this)
      setTitle(R.string.app_name)
      ViewCompat.setElevation(this, 4f.toDp(context).toFloat())

      setNavigationOnClickListener(DebouncedOnClickListener.create {
        onBackPressed()
      })

      inflateMenu(R.menu.menu)
      setOnMenuItemClickListener {
        if (it.itemId == R.id.menu_id_privacy_policy) {
          PRIVACY_POLICY_URL.hyperlink(context)
              .navigate {
                Snackbreak.short(self, rootView, ErrorDetail("Error", it.localizedMessage))
              }
          return@setOnMenuItemClickListener true
        } else {
          return@setOnMenuItemClickListener false
        }
      }
    }
  }

  companion object {

    private const val PRIVACY_POLICY_URL =
      "https://pyamsoft.blogspot.com/p/zaptorch-privacy-policy.html"
  }
}
