/*
 * Copyright 2017 Peter Kenji Yamanaka
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

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.preference.PreferenceManager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import com.pyamsoft.pydroid.ui.about.AboutLibrariesFragment
import com.pyamsoft.pydroid.ui.sec.TamperActivity
import com.pyamsoft.pydroid.ui.util.AnimUtil
import com.pyamsoft.pydroid.util.AppUtil
import com.pyamsoft.pydroid.util.NetworkUtil
import com.pyamsoft.zaptorch.BuildConfig
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.databinding.ActivityMainBinding
import com.pyamsoft.zaptorch.settings.SettingsFragment

class MainActivity : TamperActivity() {
  internal lateinit var presenter: MainPresenter
  private lateinit var binding: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_ZapTorch)
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)

    Injector.with(this) {
      it.inject(this)
    }
    setupAppBar()
  }

  override fun onStart() {
    super.onStart()
    presenter.start(Unit)
    showMainFragment()
  }

  override fun onStop() {
    super.onStop()
    presenter.stop()
  }

  override fun onDestroy() {
    super.onDestroy()
    binding.unbind()
  }

  override fun onResume() {
    super.onResume()
    AnimUtil.animateActionBarToolbar(binding.toolbar)
  }

  override val safePackageName: String
    get() = "com.pyamsoft.zaptorch"

  override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
    return presenter.shouldHandleKeycode(keyCode) || super.onKeyUp(keyCode, event)
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    return presenter.shouldHandleKeycode(keyCode) || super.onKeyDown(keyCode, event)
  }

  private fun showMainFragment() {
    val fragmentManager = supportFragmentManager
    if (fragmentManager.findFragmentByTag(
        SettingsFragment.TAG) == null && fragmentManager.findFragmentByTag(
        AboutLibrariesFragment.TAG) == null) {
      fragmentManager.beginTransaction()
          .replace(R.id.main_viewport, SettingsFragment(), SettingsFragment.TAG)
          .commit()
    }
  }

  override fun onBackPressed() {
    val fragmentManager = supportFragmentManager
    val backStackCount = fragmentManager.backStackEntryCount
    if (backStackCount > 0) {
      fragmentManager.popBackStack()
    } else {
      super.onBackPressed()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val itemId = item.itemId
    val handled: Boolean
    when (itemId) {
      android.R.id.home -> {
        onBackPressed()
        handled = true
      }
      R.id.menu_id_privacy_policy -> {
        NetworkUtil.newLink(applicationContext, PRIVACY_POLICY_URL)
        handled = true
      }
      else -> handled = false
    }
    return handled || super.onOptionsItemSelected(item)
  }

  private fun setupAppBar() {
    setSupportActionBar(binding.toolbar)
    binding.toolbar.setTitle(R.string.app_name)
    ViewCompat.setElevation(binding.toolbar, AppUtil.convertToDP(this, 4f))
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override val changeLogLines: Array<String>
    get() {
      val line1 = "BUGFIX: Bugfixes and improvements"
      val line2 = "BUGFIX: Removed all Advertisements"
      val line3 = "BUGFIX: Faster loading of Open Source Licenses page"
      return arrayOf(line1, line2, line3)
    }

  override val versionName: String
    get() = BuildConfig.VERSION_NAME

  override val applicationIcon: Int
    get() = R.mipmap.ic_launcher

  override fun provideApplicationName(): String {
    return "ZapTorch"
  }

  override val currentApplicationVersion: Int
    get() = BuildConfig.VERSION_CODE

  companion object {

    private const val PRIVACY_POLICY_URL = "https://pyamsoft.blogspot.com/p/zaptorch-privacy-policy.html"
  }
}
