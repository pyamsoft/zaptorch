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

package com.pyamsoft.zaptorch.service

import android.app.IntentService
import android.content.Intent
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.model.ServiceEvent
import timber.log.Timber

class TorchOffService : IntentService(TorchOffService::class.java.name) {

  internal lateinit var servicePublisher: ServicePublisher

  override fun onCreate() {
    super.onCreate()
    Injector.with(this) {
      it.inject(this)
    }
  }

  override fun onHandleIntent(intent: Intent?) {
    try {
      servicePublisher.publish(ServiceEvent(ServiceEvent.Type.TORCH))
    } catch (e: IllegalStateException) {
      Timber.e(e, "onError")
    }
  }
}