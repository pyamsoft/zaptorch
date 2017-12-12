/*
 *     Copyright (C) 2017 Peter Kenji Yamanaka
 *
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.pyamsoft.zaptorch.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pyamsoft.backstack.BackStack
import com.pyamsoft.pydroid.design.fab.HideScrollFABBehavior
import com.pyamsoft.pydroid.design.util.FABUtil
import com.pyamsoft.pydroid.loader.ImageLoader
import com.pyamsoft.pydroid.loader.LoaderHelper
import com.pyamsoft.pydroid.presenter.Presenter
import com.pyamsoft.pydroid.ui.helper.setOnDebouncedClickListener
import com.pyamsoft.pydroid.ui.util.DialogUtil
import com.pyamsoft.pydroid.ui.util.setUpEnabled
import com.pyamsoft.zaptorch.Injector
import com.pyamsoft.zaptorch.R
import com.pyamsoft.zaptorch.ZapTorchComponent
import com.pyamsoft.zaptorch.databinding.FragmentMainBinding
import com.pyamsoft.zaptorch.service.VolumeMonitorService
import com.pyamsoft.zaptorch.settings.AccessibilityRequestDialog
import com.pyamsoft.zaptorch.settings.ServiceInfoDialog
import com.pyamsoft.zaptorch.settings.SettingPublisher
import com.pyamsoft.zaptorch.settings.SettingsFragment
import com.pyamsoft.zaptorch.settings.TorchPreferenceFragment
import com.pyamsoft.zaptorch.uicode.WatchedFragment

class MainFragment : WatchedFragment() {

    internal lateinit var imageLoader: ImageLoader
    internal lateinit var publisher: SettingPublisher
    private lateinit var binding: FragmentMainBinding
    private var fabTask = LoaderHelper.empty()
    private lateinit var backstack: BackStack

    override fun provideBoundPresenters(): List<Presenter<*>> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Injector.obtain<ZapTorchComponent>(context!!.applicationContext).inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        backstack = BackStack.create(this, R.id.main_container)
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFAB()
        displayPreferenceFragment()
    }

    private fun setupFAB() {
        FABUtil.setupFABBehavior(binding.mainSettingsFab, HideScrollFABBehavior(10))
        binding.mainSettingsFab.setOnDebouncedClickListener {
            if (VolumeMonitorService.isRunning) {
                DialogUtil.guaranteeSingleDialogFragment(activity,
                        ServiceInfoDialog(),
                        "servce_info")
            } else {
                DialogUtil.guaranteeSingleDialogFragment(activity,
                        AccessibilityRequestDialog(),
                        "accessibility")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fabTask = LoaderHelper.unload(fabTask)
        binding.unbind()
    }

    override fun onResume() {
        super.onResume()
        toolbarActivity.withToolbar {
            it.setTitle(R.string.app_name)
            it.setUpEnabled(false)
        }

        if (VolumeMonitorService.isRunning) {
            fabTask = LoaderHelper.unload(fabTask)
            fabTask = imageLoader.fromResource(R.drawable.ic_help_24dp).into(
                    binding.mainSettingsFab)
        } else {
            fabTask = LoaderHelper.unload(fabTask)
            fabTask = imageLoader.fromResource(R.drawable.ic_service_start_24dp).into(
                    binding.mainSettingsFab)
        }
    }

    private fun displayPreferenceFragment() {
        val fragmentManager = childFragmentManager
        if (fragmentManager.findFragmentByTag(TorchPreferenceFragment.TAG) == null) {
            backstack.set(SettingsFragment.TAG) { SettingsFragment() }
        }
    }

    override fun onBackPressed(): Boolean = backstack.back()

    companion object {

        const val TAG = "MainFragment"
    }
}
