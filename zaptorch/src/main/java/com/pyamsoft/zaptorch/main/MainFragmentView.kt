package com.pyamsoft.zaptorch.main

import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.pydroid.ui.app.BaseScreen

interface MainFragmentView : BaseScreen {

  fun setFabFromServiceState(
    running: Boolean,
    onClick: (running: Boolean) -> Unit
  )

  fun createFabScrollListener(onCreate: (listener: RecyclerView.OnScrollListener) -> Unit)

}