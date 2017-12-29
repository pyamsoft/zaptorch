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

package com.pyamsoft.zaptorch.base

import android.app.IntentService
import android.content.Context
import android.support.annotation.CheckResult
import com.pyamsoft.pydroid.base.PYDroidModule
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.LoaderModule
import com.pyamsoft.zaptorch.base.preference.CameraPreferences
import com.pyamsoft.zaptorch.base.preference.ClearPreferences
import com.pyamsoft.zaptorch.base.preference.UIPreferences
import io.reactivex.Scheduler

class ZapTorchModule(private val pyDroidModule: PYDroidModule,
        private val loaderModule: LoaderModule,
        private val torchOffServiceClass: Class<out IntentService>) {

    private val preferences: ZapTorchPreferencesImpl = ZapTorchPreferencesImpl(
            pyDroidModule.provideContext())

    @CheckResult
    fun provideContext(): Context = pyDroidModule.provideContext()

    @CheckResult
    fun provideCameraPreferences(): CameraPreferences = preferences

    @CheckResult
    fun provideClearPreferences(): ClearPreferences = preferences

    @CheckResult
    fun provideUiPreferences(): UIPreferences = preferences

    @CheckResult
    fun provideTorchOffServiceClass(): Class<out IntentService> = torchOffServiceClass

    @CheckResult
    fun provideMainThreadScheduler(): Scheduler = pyDroidModule.provideMainThreadScheduler()

    @CheckResult
    fun provideIoScheduler(): Scheduler = pyDroidModule.provideIoScheduler()

    @CheckResult
    fun provideComputationScheduler(): Scheduler = pyDroidModule.provideComputationScheduler()

    @CheckResult
    fun provideImageLoader(): ImageLoader = loaderModule.provideImageLoader()
}
