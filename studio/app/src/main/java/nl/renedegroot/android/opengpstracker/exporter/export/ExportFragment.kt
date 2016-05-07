/*
 * ------------------------------------------------------------------------------
 *  **    Author: René de Groot
 *  ** Copyright: (c) 2016 René de Groot All Rights Reserved.
 *  **------------------------------------------------------------------------------
 *  ** No part of this file may be reproduced
 *  ** or transmitted in any form or by any
 *  ** means, electronic or mechanical, for the
 *  ** purpose, without the express written
 *  ** permission of the copyright holder.
 *  *------------------------------------------------------------------------------
 *  *
 *  *   This file is part of "Open GPS Tracker - Exporter".
 *  *
 *  *   "Open GPS Tracker - Exporter" is free software: you can redistribute it and/or modify
 *  *   it under the terms of the GNU General Public License as published by
 *  *   the Free Software Foundation, either version 3 of the License, or
 *  *   (at your option) any later version.
 *  *
 *  *   "Open GPS Tracker - Exporter" is distributed in the hope that it will be useful,
 *  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *   GNU General Public License for more details.
 *  *
 *  *   You should have received a copy of the GNU General Public License
 *  *   along with "Open GPS Tracker - Exporter".  If not, see <http://www.gnu.org/licenses/>.
 *  *
 *
 */
package nl.renedegroot.android.opengpstracker.exporter.export

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nl.renedegroot.android.opengpstracker.exporter.R
import nl.renedegroot.android.opengpstracker.exporter.databinding.FragmentExportBinding
import nl.renedegroot.android.opengpstracker.exporter.exporting.driveManager
import nl.renedegroot.android.opengpstracker.exporter.exporting.exporterManager

/**
 * A placeholder fragment containing a simple view.
 */
class ExportFragment : Fragment(), ExportHandlers.Listener {

    private val handlers = ExportHandlers(this)
    private var binding: FragmentExportBinding? = null
    private var model: ExportModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var createdBinding = DataBindingUtil.inflate<FragmentExportBinding>(inflater, R.layout.fragment_export, container, false)
        model = (activity as Listener).getExportModel()
        createdBinding.model = model
        createdBinding.handlers = handlers
        binding = createdBinding

        return createdBinding.root
    }

    override fun onResume() {
        super.onResume()
        exporterManager.addListener(model as exporterManager.ProgressListener)
    }

    override fun onPause() {
        exporterManager.removeListener(model as exporterManager.ProgressListener)
        super.onPause()
    }

    override fun onDestroyView() {
        model = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        exporterManager.stopExport()
        super.onDestroy()
    }

    override fun startTracksConnect() {
        (activity as Listener).connectTracksDatabase()
    }

    override fun startDriveConnect() {
        (activity as Listener).connectGoogleDrive()
    }

    override fun startExport() {
        val client = driveManager.driveClient;
        if (client != null) {
            exporterManager.startExport(activity, client)
        }
    }

    interface Listener {
        fun getExportModel(): ExportModel

        fun connectTracksDatabase()

        fun connectGoogleDrive()
    }
}
