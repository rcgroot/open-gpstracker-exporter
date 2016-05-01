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

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.google.android.gms.common.api.GoogleApiClient
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks.CONTENT_URI
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints
import nl.sogeti.android.log.Log
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Manager the exporting process
 */
object exporterManager {
    private val listeners = HashSet<ProgressListener>()
    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private val executor = ThreadPoolExecutor(1, NUMBER_OF_CORES, 10, TimeUnit.SECONDS, LinkedBlockingDeque())
    private var shouldStop = false
    private val waypointProgressPerTrack = mutableMapOf<Uri, Int>()
    private val completedTracks = mutableSetOf<Uri>()

    class Inner : nl.sogeti.android.gpstracker.actions.tasks.ProgressListener {
        override fun started(source: Uri?) {
            Log.d(this, "Started $source")
        }

        override fun setProgress(source: Uri?, value: Int) {
            if (source != null) {
                waypointProgressPerTrack.put(source, value)
            }
        }

        override fun finished(source: Uri?, result: Uri?) {
            Log.d(this, "Finished $source")
            if (source != null) {
                completedTracks.add(source)
                if (completedTracks.size == waypointProgressPerTrack.size) {
                    finished()
                } else {
                    updateProgress()
                }
            }
        }

        override fun showError(task: String?, errorMessage: String?, exception: Exception?) {
            throw UnsupportedOperationException()
        }
    }

    private val progressListener = Inner()

    fun startExport(context: Context, driveApi: GoogleApiClient) {
        shouldStop = false
        val resolver = context.contentResolver;
        var tracks: Cursor? = null
        var waypoints: Cursor? = null
        try {
            tracks = resolver.query(CONTENT_URI, arrayOf(Tracks._ID), null, null, null)
            waypoints = resolver.query(Waypoints.CONTENT_URI, arrayOf(Waypoints._ID), null, null, null)
            if (tracks?.moveToFirst() ?: false && waypoints?.moveToFirst() ?: false) {
                setTotalTrack(tracks.count)
                setTotalWaypoints(waypoints.count)
            }
            do {
                val id = tracks.getLong(0);
                val trackUri = ContentUris.withAppendedId(CONTENT_URI, id);
                waypointProgressPerTrack.put(trackUri, 0)
                val creator = DriveUploadTask(context, trackUri, progressListener, driveApi)
                creator.executeOn(executor)

            } while (tracks.moveToNext() && !shouldStop)

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

    private fun updateProgress() {
        val completedTracks = completedTracks.size
        val completedWaypoints = waypointProgressPerTrack.values.sum()
        listeners.forEach {
            it.updateExportProgress(
                    isRunning = true,
                    completedTracks = completedTracks,
                    completedWaypoints = completedWaypoints)
        }
    }

    private fun finished() {
        listeners.forEach { it.updateExportProgress(isRunning = false, isFinished = true) }
    }

    fun stopExport() {
        shouldStop = true
    }

    fun addListener(listener: ProgressListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ProgressListener) {
        listeners.remove(listener)
    }

    interface ProgressListener {
        fun updateExportProgress(isRunning: Boolean? = true, isFinished: Boolean? = false, completedTracks: Int? = null, totalTracks: Int? = null, completedWaypoints: Int? = null, totalWaypoints: Int? = null)
    }
}