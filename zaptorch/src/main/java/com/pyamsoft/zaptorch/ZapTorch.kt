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

package com.pyamsoft.zaptorch

import android.app.Application
import android.support.annotation.CheckResult
import android.support.v4.app.Fragment
import com.pyamsoft.pydroid.PYDroidModule
import com.pyamsoft.pydroid.about.Licenses
import com.pyamsoft.pydroid.loader.LoaderModule
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.ui.app.fragment.SettingsPreferenceFragment
import com.pyamsoft.zaptorch.base.ZapTorchModule
import com.pyamsoft.zaptorch.service.TorchOffService
import com.pyamsoft.zaptorch.uicode.WatchedDialog
import com.pyamsoft.zaptorch.uicode.WatchedFragment
import com.pyamsoft.zaptorch.uicode.WatchedPreferenceFragment
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher

class ZapTorch : Application() {

    private lateinit var refWatcher: RefWatcher
    private var component: ZapTorchComponent? = null
    private lateinit var pydroidModule: PYDroidModule
    private lateinit var loaderModule: LoaderModule

    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return
        }

        pydroidModule = PYDroidModule(this, BuildConfig.DEBUG)
        loaderModule = LoaderModule(this)
        PYDroid.init(pydroidModule, loaderModule)
        Licenses.create("Firebase", "https://firebase.google.com", "licenses/firebase")

        refWatcher = if (BuildConfig.DEBUG) {
            // Assign
            LeakCanary.install(this)
        } else {
            // Assign
            RefWatcher.DISABLED
        }
    }

    private fun buildComponent(): ZapTorchComponent =
            ZapTorchComponentImpl(
                    ZapTorchModule(pydroidModule, loaderModule, TorchOffService::class.java))

    override fun getSystemService(name: String?): Any {
        return if (Injector.name == name) {
            val zaptorch: ZapTorchComponent
            val obj = component
            if (obj == null) {
                zaptorch = buildComponent()
                component = zaptorch
            } else {
                zaptorch = obj
            }

            // Return
            zaptorch
        } else {
            // Return
            super.getSystemService(name)
        }
    }

    companion object {
        @JvmStatic
        @CheckResult
        fun getRefWatcher(fragment: WatchedDialog): RefWatcher =
                getRefWatcherInternal(fragment)

        @JvmStatic
        @CheckResult
        fun getRefWatcher(fragment: WatchedPreferenceFragment): RefWatcher =
                getRefWatcherInternal(fragment)

        @JvmStatic
        @CheckResult
        fun getRefWatcher(fragment: WatchedFragment): RefWatcher =
                getRefWatcherInternal(fragment)

        @JvmStatic
        @CheckResult
        fun getRefWatcher(
                fragment: SettingsPreferenceFragment): RefWatcher = getRefWatcherInternal(fragment)

        @JvmStatic
        @CheckResult private fun getRefWatcherInternal(fragment: Fragment): RefWatcher {
            val application = fragment.activity!!.application
            if (application is ZapTorch) {
                return application.refWatcher
            } else {
                throw IllegalStateException("Application is not ZapTorch")
            }
        }
    }
}
