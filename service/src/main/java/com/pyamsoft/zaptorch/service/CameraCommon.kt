/*
 * Copyright 2020 Peter Kenji Yamanaka
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

import com.pyamsoft.zaptorch.core.CameraInterface
import com.pyamsoft.zaptorch.core.TorchState

internal abstract class CameraCommon protected constructor() :
    CameraInterface, OnCameraStateChangedCallback {

  private var unavailableCallback: ((TorchState) -> Unit)? = null

  final override fun setOnUnavailableCallback(onUnavailable: (TorchState) -> Unit) {
    this.unavailableCallback = onUnavailable
  }

  override fun onUnavailable(state: TorchState) {
    unavailableCallback?.invoke(state)
  }

  final override fun destroy() {
    release()
    unavailableCallback = null
  }

  protected abstract fun release()
}
