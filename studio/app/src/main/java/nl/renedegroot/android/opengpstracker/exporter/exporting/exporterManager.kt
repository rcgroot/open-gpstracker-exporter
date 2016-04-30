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

package nl.renedegroot.android.opengpstracker.exporter.exporting

import android.content.Context
import android.database.Cursor
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints
import java.util.*

/**
 * Manager the exporting process
 */
object exporterManager {
    private val listeners = HashSet<ProgressListener>()

    fun startExport(context: Context) {
        val resolver = context.contentResolver;
        var tracks: Cursor? = null
        var waypoints: Cursor? = null
        try {
            tracks = resolver.query(Tracks.CONTENT_URI, arrayOf(Tracks._ID), null, null, null)
            waypoints = resolver.query(Waypoints.CONTENT_URI, arrayOf(Waypoints._ID), null, null, null)
            if (tracks != null && waypoints != null) {
                setTotalTrack(tracks.count)
                setTotalWaypoints(waypoints.count)
            }
        } finally {
            tracks?.close()
            waypoints?.close()
        }
    }

    private fun setTotalWaypoints(count: Int) {
        listeners.forEach { it.updateExportProgress(totalWaypoints = count) }
    }

    private fun setTotalTrack(count: Int) {
        listeners.forEach { it.updateExportProgress(totalTracks = count) }
    }

    fun stopExport() {

    }

    fun addListener(listener: ProgressListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ProgressListener) {
        listeners.remove(listener)
    }

    interface ProgressListener {
        fun updateExportProgress(isRunnable: Boolean? = true, completedTracks: Int? = null, totalTracks: Int? = null, completedWaypoints: Int? = null, totalWaypoints: Int? = null)
    }
}