/*
 *     Copyright (C) 2017 Peter Kenji Yamanaka
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.pyamsoft.zaptorch.main

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.preference.PreferenceManager
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import com.pyamsoft.pydroid.presenter.Presenter
import com.pyamsoft.pydroid.ui.about.AboutLibrariesFragment
import com.pyamsoft.pydroid.ui.helper.Toasty
import com.pyamsoft.pydroid.ui.sec.TamperActivity
import com.pyamsoft.pydroid.ui.util.AnimUtil
import com.pyamsoft.pydroid.util.AppUtil
import com.pyamsoft.pydroid.util.NetworkUtil
import com.pyamsoft.zaptorch.BuildConfig
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.databinding.ActivityMainBinding
import com.pyamsoft.zaptorch.main.MainPresenter.Callback
import com.pyamsoft.zaptorch.settings.SettingsFragment
import timber.log.Timber

class MainActivity : TamperActivity(), Callback {

  internal lateinit var presenter: MainPresenter
  private lateinit var binding: ActivityMainBinding
  private var handleKeyPress: Boolean = false

  override val safePackageName: String = "com.pyamsoft.zaptorch"

  override fun provideBoundPresenters(): List<Presenter<*>> =
      super.provideBoundPresenters() + listOf(presenter)

  override fun onCreate(savedInstanceState: Bundle?) {
    setTheme(R.style.Theme_ZapTorch)
    super.onCreate(savedInstanceState)
    binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
    PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)

    (Injector.obtain(applicationContext) as ZapTorchComponent).plusMainComponent(getString(R.string.handle_volume_keys_key)).inject(this)
    setupAppBar()

    presenter.bind(this)
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
    Toasty.makeText(this, "Failed to handle volume keypress, please try again",
        Toasty.LENGTH_SHORT).show()
  }

  override fun onDestroy() {
    super.onDestroy()
    binding.unbind()
  }

  override fun onResume() {
    super.onResume()
    AnimUtil.animateActionBarToolbar(binding.toolbar)
  }

  override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
    return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      handleKeyPress
    } else {
      super.onKeyUp(keyCode, event)
    }
  }

  override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    return if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      handleKeyPress
    } else {
      super.onKeyDown(keyCode, event)
    }
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
      fragmentManager.popBackStackImmediate()
    } else {
      super.onBackPressed()
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    val itemId = item.itemId
    val handled: Boolean = when (itemId) {
      android.R.id.home -> {
        onBackPressed()
        true
      }
      R.id.menu_id_privacy_policy -> {
        NetworkUtil.newLink(applicationContext, PRIVACY_POLICY_URL)
        true
      }
      else -> false
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

  override val applicationName: String
    get() = getString(R.string.app_name)

  override val currentApplicationVersion: Int
    get() = BuildConfig.VERSION_CODE

  companion object {

    private const val PRIVACY_POLICY_URL = "https://pyamsoft.blogspot.com/p/zaptorch-privacy-policy.html"
  }
}
