/*
 * Copyright 2016 Peter Kenji Yamanaka
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

package com.pyamsoft.zaptorch.app.main;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class KeyHandlerBus {

  private static final Subject<Event, Event> BUS = new SerializedSubject<>(PublishSubject.create());

  public static void post(final Event e) {
    if (BUS.hasObservers()) {
      BUS.onNext(e);
    }
  }

  public static Observable<Event> register() {
    return BUS.filter(event -> event != null)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
  }

  public static final class Event {
    private final boolean handle;

    public Event(boolean handle) {
      this.handle = handle;
    }

    public final boolean handle() {
      return handle;
    }
  }
}
