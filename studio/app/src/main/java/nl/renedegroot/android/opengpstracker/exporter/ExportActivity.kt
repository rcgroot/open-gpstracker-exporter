/*------------------------------------------------------------------------------
 **    Author: René de Groot
 ** Copyright: (c) 2016 René de Groot All Rights Reserved.
 **------------------------------------------------------------------------------
 ** No part of this file may be reproduced
 ** or transmitted in any form or by any
 ** means, electronic or mechanical, for the
 ** purpose, without the express written
 ** permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of "Open GPS Tracker - Exporter".
 *
 *   "Open GPS Tracker - Exporter" is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   "Open GPS Tracker - Exporter" is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with "Open GPS Tracker - Exporter".  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.renedegroot.android.opengpstracker.exporter

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import nl.renedegroot.android.opengpstracker.exporter.about.AboutFragment
import nl.renedegroot.android.opengpstracker.exporter.export.ExportFragment
import nl.renedegroot.android.opengpstracker.exporter.export.ExportModel
import nl.renedegroot.android.opengpstracker.exporter.exporting.driveManager
import nl.sogeti.android.gpstracker.integration.PermissionRequestor
import nl.sogeti.android.log.Log

/**
 * Owns the export model and communicated the the Android system regarding
 * permissions.
 */
class ExportActivity : AppCompatActivity(), ExportFragment.Listener {

    private val model = ExportModel()
    private val permissionRequestor = PermissionRequestor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)
        connectToServices()
    }

    override fun onDestroy() {
        permissionRequestor.stop()
        driveManager.stop()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        driveManager.processResult(requestCode, resultCode)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        permissionRequestor.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_export, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            showAboutDialog()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun getExportModel(): ExportModel = model

    override fun connectGoogleDrive() {
        driveManager.start(this, { isConnected ->
            model?.isDriveConnected?.set(isConnected)
        })
    }

    override fun connectTracksDatabase() {
        permissionRequestor.checkTracksPermission(this, {
            model?.isTrackerConnected?.set(true)
        })
    }

    private fun connectToServices() {
        permissionRequestor.checkTracksPermission(this, {
            model?.isTrackerConnected?.set(true)
            driveManager.start(this, { isConnected ->
                model?.isDriveConnected?.set(isConnected)
                if (isConnected) {
                    Log.d(this, "Everything is connected")
                } else {
                    Log.d(this, "Drive failed")
                }
            })
        })
    }

    fun showAboutDialog() {
        AboutFragment().show(supportFragmentManager, "ABOUT")
    }
}
