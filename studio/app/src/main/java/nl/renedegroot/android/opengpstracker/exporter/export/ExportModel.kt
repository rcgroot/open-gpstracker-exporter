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

import android.databinding.ObservableBoolean
import android.databinding.ObservableInt
import nl.renedegroot.android.opengpstracker.exporter.exporting.exporterManager

/**
 * View model for the export preparation fragment
 */
class ExportModel : exporterManager.ProgressListener {
    val isDriveConnected = ObservableBoolean(false);
    val isTrackerConnected = ObservableBoolean(false);
    val isRunning = ObservableBoolean(false)

    val completedTracks = ObservableInt(0)
    val totalTracks = ObservableInt(0)
    val totalWaypoints = ObservableInt(0)
    val completedWaypoints = ObservableInt(0)

    override fun updateExportProgress(isRunning: Boolean?, completedTracks: Int?, totalTracks: Int?, completedWaypoints: Int?, totalWaypoints: Int?) {
        this.isRunning.set(isRunning ?: this.isRunning.get())
        this.completedTracks.set(completedTracks ?: this.completedTracks.get())
        this.totalTracks.set(totalTracks ?: this.totalTracks.get())
        this.completedWaypoints.set(completedWaypoints ?: this.completedWaypoints.get())
        this.totalWaypoints.set(totalWaypoints ?: this.totalWaypoints.get())
    }
}