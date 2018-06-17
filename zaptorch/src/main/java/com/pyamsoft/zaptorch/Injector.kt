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

package com.pyamsoft.zaptorch

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.CheckResult

object Injector {

  const val name: String = "com.pyamsoft.zaptorch.INJECTOR"

  @SuppressLint("WrongConstant")
  @CheckResult
  @JvmStatic
  fun <T : Any> obtain(context: Context): T {
    val service: Any? = context.getSystemService(name)
    if (service == null) {
      throw IllegalStateException("No service found for: $name")
    } else {
      @Suppress("UNCHECKED_CAST")
      return service as T
    }
  }
}
