package com.pyamsoft.zaptorch.main

import com.pyamsoft.pydroid.ui.app.BaseScreen

interface MainView : BaseScreen {

  fun onToolbarNavClicked(onClick: () -> Unit)

  fun onMenuItemClicked(onClick: (itemId: Int) -> Unit)
}