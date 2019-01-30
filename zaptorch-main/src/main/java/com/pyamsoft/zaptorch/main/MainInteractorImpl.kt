/*
 * Copyright 2019 Peter Kenji Yamanaka
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
 *
 */

package com.pyamsoft.zaptorch.main

import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import com.pyamsoft.pydroid.core.threads.Enforcer
import com.pyamsoft.zaptorch.api.MainInteractor
import com.pyamsoft.zaptorch.api.UIPreferences
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

internal class MainInteractorImpl internal constructor(
  private val handleKeyPressKey: String,
  private val enforcer: Enforcer,
  private val preferences: UIPreferences
) : MainInteractor {

  override fun onHandleKeyPressChanged(): Observable<Boolean> {
    return Observable.create {
      val listener = OnSharedPreferenceChangeListener { _, key ->
        if (key == handleKeyPressKey) {
          emit(it)
        }
      }

      enforcer.assertNotOnMainThread()
      preferences.register(listener)
      it.setCancellable {
        preferences.unregister(listener)
      }

      emit(it)
    }
  }

  private fun emit(emitter: ObservableEmitter<Boolean>) {
    if (!emitter.isDisposed) {
      emitter.onNext(preferences.shouldHandleKeys())
    }
  }

}
