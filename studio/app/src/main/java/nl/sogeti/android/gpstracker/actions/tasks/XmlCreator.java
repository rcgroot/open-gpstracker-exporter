/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) Apr 24, 2011 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.actions.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;

import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks;
import nl.sogeti.android.log.Log;

/**
 * Async XML creation task Execute without parameters (Void) Update posted with single Integer And result is a filename
 * in a String
 *
 * @author rene (c) May 29, 2011, Sogeti B.V.
 * @version $Id$
 */
public abstract class XmlCreator extends AsyncTask<Void, Integer, Uri> {
    public ProgressAdmin mProgressAdmin;
    protected Context mContext;
    protected Uri mTrackUri;
    private ProgressListener mProgressListener;
    private String mErrorText;
    private Exception mException;
    private String mTask;

    XmlCreator(Context context, Uri trackUri, ProgressListener listener) {
        mContext = context;
        mTrackUri = trackUri;
        mProgressListener = listener;
        mProgressAdmin = new ProgressAdmin();
    }

    /**
     * Removes all non-word chars (\W) from the text
     *
     * @param fileName
     * @param defaultName
     * @return a string larger then 0 with either word chars remaining from the input or the default provided
     */
    public static String cleanFilename(String fileName, String defaultName) {
        if (fileName == null || "".equals(fileName)) {
            fileName = defaultName;
        } else {
            fileName = fileName.replaceAll("[^a-zA-Z0-9_\\- ]", "");
            fileName = (fileName.length() > 0) ? fileName : defaultName;
            fileName.substring(0, Math.min(250, fileName.length()));
        }
        return fileName;
    }

    public static boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteRecursive(new File(file, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return file.delete();
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        String result = "";
      /*
       * To convert the InputStream to String we use the Reader.read(char[] buffer) method. We iterate until the Reader
       * return -1 which means there's no more data to read. We use the StringWriter class to produce the string.
       */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[8192];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            result = writer.toString();
        }
        return result;
    }

    public static InputStream convertStreamToLoggedStream(String tag, InputStream is) throws IOException {
        String result = "";
      /*
       * To convert the InputStream to String we use the Reader.read(char[] buffer) method. We iterate until the Reader
       * return -1 which means there's no more data to read. We use the StringWriter class to produce the string.
       */
        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[8192];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            result = writer.toString();
        }
        InputStream in = new ByteArrayInputStream(result.getBytes("UTF-8"));
        return in;
    }

    public String getFilename() {
        Cursor trackCursor = null;
        ContentResolver resolver = mContext.getContentResolver();
        String trackName = "Untitled";
        try {
            trackCursor = resolver.query(mTrackUri, new String[]{Tracks._ID, Tracks.NAME}, null, null, null);
            if (trackCursor.moveToLast()) {
                String defaultName = "track_" + trackCursor.getString(0);
                trackName = cleanFilename(defaultName + "_" + trackCursor.getString(1), defaultName);
            }
        } finally {
            if (trackCursor != null) {
                trackCursor.close();
            }
        }

        return trackName;
    }

    public void executeOn(Executor executor) {
        if (Build.VERSION.SDK_INT >= 11) {
            executeOnExecutor(executor);
        } else {
            execute();
        }
    }

    public void quickTag(XmlSerializer serializer, String ns, String tag, String content)
            throws IllegalArgumentException, IllegalStateException, IOException {
        if (tag == null) {
            tag = "";
        }
        if (content == null) {
            content = "";
        }
        serializer.text("\n");
        serializer.startTag(ns, tag);
        serializer.text(content);
        serializer.endTag(ns, tag);
    }

    protected abstract String getMimeType();

    protected void handleError(String task, Exception e, String text) {
        Log.e(this, "Unable to save ", e);
        mTask = task;
        mException = e;
        mErrorText = text;
        cancel(false);
        throw new CancellationException(text);
    }

    @Override
    protected void onPreExecute() {
        if (mProgressListener != null) {
            mProgressListener.started(mTrackUri);
        }
    }

    @Override
    protected void onPostExecute(Uri resultFilename) {
        if (mProgressListener != null) {
            mProgressListener.finished(mTrackUri, resultFilename);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        if (mProgressListener != null) {
            mProgressListener.setProgress(mTrackUri, mProgressAdmin.getWaypointProgress());
        }
    }

    @Override
    protected void onCancelled() {
        if (mProgressListener != null) {
            mProgressListener.finished(mTrackUri, null);
            mProgressListener.showError(mTask, mErrorText, mException);
        }
    }

    public class ProgressAdmin {
        private long lastUpdate;

        public int getWaypointProgress() {
            return waypointProgress;
        }

        private int waypointProgress;

        public void considerPublishProgress() {
            long now = new Date().getTime();
            if (now - lastUpdate > 1000) {
                lastUpdate = now;
                publishProgress();
            }
        }

        public void addWaypointProgress(int i) {
            waypointProgress += i;
            considerPublishProgress();
        }
    }
}
