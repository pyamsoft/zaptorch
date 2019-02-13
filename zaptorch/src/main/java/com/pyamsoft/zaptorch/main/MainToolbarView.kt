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

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
import com.pyamsoft.pydroid.ui.arch.BaseUiView
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.DebouncedOnClickListener
import com.pyamsoft.pydroid.util.toDp
import com.pyamsoft.zaptorch.R

internal class MainToolbarView internal constructor(
  private val toolbarActivityProvider: ToolbarActivityProvider,
  private val theming: Theming,
  parent: ViewGroup,
  callback: MainToolbarView.Callback
) : BaseUiView<MainToolbarView.Callback>(parent, callback) {

  private val toolbar by lazyView<Toolbar>(R.id.toolbar)

  override val layout: Int = R.layout.toolbar

  override fun id(): Int {
    return toolbar.id
  }

  override fun onInflated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    setupToolbar()
  }

  private fun setupToolbar() {
    val theme: Int
    if (theming.isDarkTheme()) {
      theme = R.style.ThemeOverlay_AppCompat
    } else {
      theme = R.style.ThemeOverlay_AppCompat_Light
    }

    toolbar.apply {
      popupTheme = theme
      toolbarActivityProvider.setToolbar(this)
      setTitle(R.string.app_name)
      ViewCompat.setElevation(this, 4F.toDp(context).toFloat())
      inflateMenu(R.menu.menu)

      setNavigationOnClickListener(DebouncedOnClickListener.create {
        callback.onToolbarNavClicked()
      })

      toolbar.setOnMenuItemClickListener { item ->
        callback.onToolbarMenuClicked(item)
        return@setOnMenuItemClickListener true
      }
    }
  }

  override fun teardown() {
    toolbar.setNavigationOnClickListener(null)
  }

  interface Callback {

    fun onToolbarMenuClicked(item: MenuItem)

    fun onToolbarNavClicked()

  }

}
