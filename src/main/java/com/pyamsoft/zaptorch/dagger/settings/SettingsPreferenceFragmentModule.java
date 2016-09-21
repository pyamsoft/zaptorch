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

package com.pyamsoft.zaptorch.dagger.settings;

import com.pyamsoft.pydroid.ActivityScope;
import com.pyamsoft.zaptorch.app.settings.SettingsFragmentPresenter;
import com.pyamsoft.zaptorch.app.settings.SettingsPreferenceFragmentPresenter;
import dagger.Module;
import dagger.Provides;
import javax.inject.Named;
import rx.Scheduler;

@Module public class SettingsPreferenceFragmentModule {

  @ActivityScope @Provides SettingsFragmentPresenter provideSettingsFragmentPresenter(
      @Named("main") Scheduler mainScheduler, @Named("io") Scheduler ioScheduler) {
    return new SettingsFragmentPresenterImpl(mainScheduler, ioScheduler);
  }

  @ActivityScope @Provides
  SettingsPreferenceFragmentPresenter provideSettingsPreferenceFragmentPresenter(
      final SettingsPreferenceFragmentInteractor interactor, @Named("main") Scheduler mainScheduler,
      @Named("io") Scheduler ioScheduler) {
    return new SettingsPreferenceFragmentPresenterImpl(interactor, mainScheduler, ioScheduler);
  }

  @ActivityScope @Provides
  SettingsPreferenceFragmentInteractor provideSettingsPreferenceFragmentInteractor(
      final SettingsPreferenceFragmentInteractorImpl interactor) {
    return interactor;
  }
}
