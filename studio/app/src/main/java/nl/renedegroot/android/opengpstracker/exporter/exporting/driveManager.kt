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

import android.app.Activity
import android.content.IntentSender
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.drive.Drive
import nl.sogeti.android.log.Log

/**
 * Communicates with the Google Drive
 */
object driveManager : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private val REQUEST_CODE_RESOLUTION = 1
    private val REQUEST_CODE_ERROR = 2
    private var client: GoogleApiClient? = null
    private var activity: Activity? = null
    private var onConnected: (Boolean) -> Unit = {}

    fun start(activity: Activity, onConnected: (Boolean) -> Unit = {}) {
        this.activity = activity
        this.onConnected = onConnected
        if (client == null) {
            client = GoogleApiClient.Builder(activity.applicationContext)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
        }
        client?.connect()
    }

    fun processResult(requestCode: Int, resultCode: Int): Boolean {
        if (requestCode == REQUEST_CODE_RESOLUTION) {
            if (resultCode == Activity.RESULT_OK) {
                client?.connect()
            }
            return true
        }
        return false
    }

    fun stop() {
        activity = null
        onConnected = {}
        client?.disconnect()
    }

    override fun onConnected(result: Bundle?) {
        Log.d(this, "onConnectionFailed $result")
        onConnected(true)
        onConnected = {}
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        Log.d(this, "onConnectionFailed $result")
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(activity, REQUEST_CODE_RESOLUTION);
            } catch (exception: IntentSender.SendIntentException) {
                onConnected(false)
                onConnected = {}
            }

        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(activity, result.errorCode, REQUEST_CODE_ERROR).show();
            onConnected(false)
            onConnected = {}
        }
    }

    override fun onConnectionSuspended(value: Int) {
        Log.d(this, "onConnectionSuspended $value")
    }
}