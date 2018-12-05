package com.pyamsoft.zaptorch.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.ui.util.popHide
import com.pyamsoft.pydroid.ui.util.popShow
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.pydroid.ui.widget.HideOnScrollListener
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.databinding.FragmentMainBinding

internal class MainFragmentViewImpl internal constructor(
  private val owner: LifecycleOwner,
  private val inflater: LayoutInflater,
  private val container: ViewGroup?,
  private val imageLoader: ImageLoader
) : MainFragmentView, LifecycleObserver {

  private lateinit var binding: FragmentMainBinding

  init {
    owner.lifecycle.addObserver(this)
  }

  @Suppress("unused")
  @OnLifecycleEvent(ON_DESTROY)
  internal fun destroy() {
    owner.lifecycle.removeObserver(this)

    binding.unbind()
  }

  override fun create() {
    binding = FragmentMainBinding.inflate(inflater, container, false)
  }

  override fun root(): View {
    return binding.root
  }

  override fun setFabFromServiceState(
    running: Boolean,
    onClick: (running: Boolean) -> Unit
  ) {
    binding.apply {
      mainSettingsFab.setOnDebouncedClickListener {
        onClick(running)
      }

      val icon: Int
      if (running) {
        icon = R.drawable.ic_help_24dp
      } else {
        icon = R.drawable.ic_service_start_24dp
      }
      imageLoader.load(icon)
          .into(mainSettingsFab)
          .bind(owner)
    }
  }

  override fun createFabScrollListener(onCreate: (listener: OnScrollListener) -> Unit) {
    val fab = binding.mainSettingsFab
    val listener = HideOnScrollListener.withView(fab) {
      if (it) {
        fab.popShow()
      } else {
        fab.popHide()
      }
    }

    onCreate(listener)
  }

}