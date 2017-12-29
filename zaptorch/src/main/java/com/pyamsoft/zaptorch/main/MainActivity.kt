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
import com.pyamsoft.backstack.BackStack
import com.pyamsoft.backstack.BackStacks
import com.pyamsoft.pydroid.ui.about.AboutLibrariesFragment
import com.pyamsoft.pydroid.ui.helper.DebouncedOnClickListener
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
import timber.log.Timber

class MainActivity : TamperActivity(), MainPresenter.View {

    internal lateinit var presenter: MainPresenter
    private lateinit var binding: ActivityMainBinding
    private var handleKeyPress: Boolean = false

    override val changeLogLines: Array<String> = arrayOf(
            "BUGFIX: Better support for small screen devices"
    )

    override val versionName: String = BuildConfig.VERSION_NAME

    override val applicationIcon: Int = R.mipmap.ic_launcher

    override val currentApplicationVersion: Int = BuildConfig.VERSION_CODE

    override val safePackageName: String = "com.pyamsoft.zaptorch"

    override val applicationName: String
        get() = getString(R.string.app_name)

    private lateinit var backstack: BackStack

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_ZapTorch)
        super.onCreate(savedInstanceState)
        backstack = BackStacks.create(this, R.id.main_viewport)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        PreferenceManager.setDefaultValues(applicationContext, R.xml.preferences, false)

        Injector.obtain<ZapTorchComponent>(applicationContext).plusMainComponent(
                getString(R.string.handle_volume_keys_key)).inject(this)
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
        if (fragmentManager.findFragmentByTag(MainFragment.TAG) == null
                && fragmentManager.findFragmentByTag(AboutLibrariesFragment.TAG) == null) {
            backstack.set(MainFragment.TAG) { MainFragment() }
        }
    }

    override fun onBackPressed() {
        if (!backstack.back()) {
            super.onBackPressed()
        }
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setToolbar(this)
            setTitle(R.string.app_name)
            ViewCompat.setElevation(this, AppUtil.convertToDP(context, 4f))

            setNavigationOnClickListener(DebouncedOnClickListener.create {
                onBackPressed()
            })

            inflateMenu(R.menu.menu)
            setOnMenuItemClickListener {
                if (it.itemId == R.id.menu_id_privacy_policy) {
                    NetworkUtil.newLink(applicationContext, PRIVACY_POLICY_URL)
                    return@setOnMenuItemClickListener true
                } else {
                    return@setOnMenuItemClickListener false
                }
            }
        }
    }

    companion object {

        private const val PRIVACY_POLICY_URL = "https://pyamsoft.blogspot.com/p/zaptorch-privacy-policy.html"
    }
}
