package com.pyamsoft.zaptorch.dagger.frag;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import rx.Observable;

interface MainFragmentInteractor {

  @CheckResult @NonNull Observable<Boolean> clearAll();
}
