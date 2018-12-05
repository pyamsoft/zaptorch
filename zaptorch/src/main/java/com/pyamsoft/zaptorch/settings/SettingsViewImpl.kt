package com.pyamsoft.zaptorch.settings

import androidx.annotation.CheckResult
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import androidx.recyclerview.widget.RecyclerView
import com.pyamsoft.zaptorch.R

internal class SettingsViewImpl internal constructor(
  private val owner: LifecycleOwner,
  private val preferenceScreen: PreferenceScreen
) : SettingsView, LifecycleObserver {

  private val context = preferenceScreen.context

  private lateinit var zaptorchExplain: Preference

  private var scrollListener: RecyclerView.OnScrollListener? = null
  private var listView: RecyclerView? = null

  init {
    owner.lifecycle.addObserver(this)
  }

  @Suppress("unused")
  @OnLifecycleEvent(ON_DESTROY)
  internal fun destroy() {
    owner.lifecycle.removeObserver(this)

    scrollListener?.also { listView?.removeOnScrollListener(it) }
    scrollListener = null
  }

  @CheckResult
  private fun findPreference(@StringRes id: Int): Preference {
    return preferenceScreen.findPreference(context.getString(id))
  }

  override fun create() {
    zaptorchExplain = findPreference(R.string.zaptorch_explain_key)
  }

  override fun onExplainClicked(onClick: () -> Unit) {
    zaptorchExplain.setOnPreferenceClickListener {
      onClick()
      return@setOnPreferenceClickListener true
    }
  }

  override fun addScrollListener(
    listView: RecyclerView,
    listener: RecyclerView.OnScrollListener
  ) {
    listView.addOnScrollListener(listener)
    scrollListener = listener
  }

}