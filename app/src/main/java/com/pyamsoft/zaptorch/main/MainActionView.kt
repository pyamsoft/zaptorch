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

import android.view.ViewGroup
import androidx.core.view.ViewPropertyAnimatorCompat
import com.pyamsoft.pydroid.arch.BaseUiView
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.Loaded
import com.pyamsoft.pydroid.ui.util.popHide
import com.pyamsoft.pydroid.ui.util.popShow
import com.pyamsoft.pydroid.ui.util.setOnDebouncedClickListener
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.databinding.FloatingActionButtonBinding
import com.pyamsoft.zaptorch.main.MainViewEvent.ActionClick
import javax.inject.Inject

internal class MainActionView @Inject internal constructor(
    private val imageLoader: ImageLoader,
    parent: ViewGroup
) : BaseUiView<MainViewState, MainViewEvent, FloatingActionButtonBinding>(parent) {

    override val viewBinding = FloatingActionButtonBinding::inflate

    override val layoutRoot by boundView { fabContainer }

    private var actionIconLoaded: Loaded? = null

    private var animator: ViewPropertyAnimatorCompat? = null

    init {
        doOnInflate {
            binding.fab.setOnDebouncedClickListener {
                publish(ActionClick)
            }
        }

        doOnTeardown {
            binding.fab.setOnDebouncedClickListener(null)
            actionIconLoaded?.dispose()
        }

        doOnTeardown {
            cancelAnimator()
        }
    }

    private fun cancelAnimator() {
        animator?.cancel()
        animator = null
    }

    override fun onRender(state: MainViewState) {
        toggleVisibility(state.isVisible)
        setFabState(state.isServiceRunning)
    }

    private fun setFabState(running: Boolean) {
        val icon = if (running) {
            R.drawable.ic_help_24dp
        } else {
            R.drawable.ic_service_start_24dp
        }

        actionIconLoaded?.dispose()
        actionIconLoaded = imageLoader.load(icon)
            .into(binding.fab)
    }

    private fun toggleVisibility(visible: Boolean) {
        if (animator == null) {
            val a = if (visible) binding.fab.popShow() else binding.fab.popHide()
            a.withEndAction { cancelAnimator() }
            animator = a
        }
    }
}
