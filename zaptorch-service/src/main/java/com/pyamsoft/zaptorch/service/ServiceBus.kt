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

package com.pyamsoft.zaptorch.service

import com.pyamsoft.pydroid.bus.EventBus
import com.pyamsoft.pydroid.bus.RxBus
import com.pyamsoft.zaptorch.model.ServiceEvent
import io.reactivex.Observable

internal class ServiceBus internal constructor() : EventBus<ServiceEvent> {

  private val bus = RxBus.create<ServiceEvent>()

  override fun listen(): Observable<ServiceEvent> = bus.listen()

  override fun publish(event: ServiceEvent) {
    bus.publish(event)
  }

}

