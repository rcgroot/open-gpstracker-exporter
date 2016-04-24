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

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import nl.renedegroot.android.opengpstracker.exporter.databinding.FragmentExportBinding
import nl.sogeti.android.log.Log

/**
 * Show progress of the export, with cancel options
 */
class ExportFragment : DialogFragment() {

    val model = ExportModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Passing in the DataBindingComponent because the Kotlin variant does go in automatically
        var binding = FragmentExportBinding.inflate(activity.layoutInflater, null, false);
        binding.model = model

        val builder = AlertDialog.Builder(activity);
        builder.setView(binding.root)
        builder.setNegativeButton(android.R.string.cancel) { dialog, which -> stopExport() };


        return builder.create();
    }

    fun stopExport() {
        Log.d(this, "TODO interrupt export")
        dismiss()
    }
}