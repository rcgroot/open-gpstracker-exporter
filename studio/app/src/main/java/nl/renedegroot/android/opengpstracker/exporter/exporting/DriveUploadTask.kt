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
import android.net.Uri
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Result
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.MetadataChangeSet
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import nl.sogeti.android.gpstracker.actions.tasks.GpxCreator
import nl.sogeti.android.gpstracker.actions.tasks.ProgressListener

/**
 * Async task that uses the GpxCreate URI to Stream capability to fill a Google Drive file with said stream
 */
class DriveUploadTask(context: Context, trackUri: Uri, listener: ProgressListener, val driveApi: GoogleApiClient) : GpxCreator(context, trackUri, listener) {

    private val TASK = "Drive upload"
    private val FOLDER_NAME = "Open GPS Tracker - Exports"
    private val FOLDER_MIME = "application/vnd.google-apps.folder"

    override fun doInBackground(vararg params: Void): Uri? {
        // Step 1: Create content
        val driveContentsResult = Drive.DriveApi.newDriveContents(driveApi).await();
        processResult(driveContentsResult)
        if (isCancelled) {
            return null
        }

        // Step 2: Fill content with GPX data
        super.exportGpx(driveContentsResult.driveContents.outputStream)
        if (isCancelled) {
            return null
        }

        // Step 3: Look for the export folder
        val rootFolder = Drive.DriveApi.getRootFolder(driveApi);
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, FOLDER_NAME))
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, FOLDER_MIME))
                .build()
        val rootListResult = rootFolder.queryChildren(driveApi, query).await()
        processResult(driveContentsResult)
        if (isCancelled) {
            return null
        }

        // Step 4: Find or create the export folder
        val exportFolder: DriveFolder
        if (rootListResult.metadataBuffer.count > 0) {
            val folderId = rootListResult.metadataBuffer.get(0).driveId
            exportFolder = folderId.asDriveFolder()
        } else {
            val metadata = MetadataChangeSet.Builder()
                    .setTitle(FOLDER_NAME)
                    .build();
            val createFolderResult = rootFolder.createFolder(driveApi, metadata).await()
            processResult(createFolderResult)
            if (isCancelled) {
                return null
            }
            exportFolder = createFolderResult.driveFolder
        }
        rootListResult.metadataBuffer.release()

        // Step 5: Create file in export folder
        val metadata = MetadataChangeSet.Builder()
                .setTitle(filename)
                .setMimeType(mimeType)
                .build();
        val fileResult = exportFolder.createFile(driveApi, metadata, driveContentsResult.driveContents).await();
        processResult(fileResult)
        if (isCancelled) {
            return null
        }

        return null
    }

    internal fun processResult(result: Result): Boolean {
        val isSuccess = result.status.isSuccess
        if (!isSuccess) {
            handleError(TASK, null, result.status.statusMessage)
        }

        return isSuccess;
    }
}
