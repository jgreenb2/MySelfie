package com.example.jgreenb2.myselfie;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by jgreenb2 on 5/25/15.
 *
 * baesd on http://stephendnicholas.com/archives/974
 */


public class JpegContentProvider extends ContentProvider {

    private static final String CLASS_NAME = "JpegContentProvider";

    // The authority is the symbolic name for the provider class
    public static final String AUTHORITY = "com.jgreenb2.selfieattach.provider";

    // UriMatcher used to match against incoming requests
    private UriMatcher uriMatcher;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

         // Add a URI to the matcher which will match against the form
        // 'content://com.stephendnicholas.gmailattach.provider/*'
        // and return 1 in the case that the incoming Uri matches this pattern
        uriMatcher.addURI(AUTHORITY, "*", 1);

        return true;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode)
            throws FileNotFoundException {

        String LOG_TAG = CLASS_NAME + " - openFile";

        Log.v(LOG_TAG,
                "Called with uri: '" + uri + "'." + uri.getLastPathSegment());

        // Check incoming Uri against the matcher
        switch (uriMatcher.match(uri)) {

            // If it returns 1 - then it matches the Uri defined in onCreate
            case 1:

                // The desired file name is specified by the last segment of the
                // path
                // E.g.
                // 'content://com.stephendnicholas.gmailattach.provider/Test.txt'
                // Take this and build the path to the file
//                String fileLocation = getContext().getCacheDir() + File.separator
//                        + uri.getLastPathSegment();

                String fileLocation = getStoragePath() + File.separator + uri.getLastPathSegment();

                // Create & return a ParcelFileDescriptor pointing to the file
                // Note: I don't care what mode they ask for - they're only getting
                // read only
                ParcelFileDescriptor pfd = ParcelFileDescriptor.open(new File(
                        fileLocation), ParcelFileDescriptor.MODE_READ_ONLY);
                return pfd;

            // Otherwise unrecognised Uri
            default:
                Log.v(LOG_TAG, "Unsupported uri: '" + uri + "'.");
                throw new FileNotFoundException("Unsupported uri: "
                        + uri.toString());
        }
    }

    // //////////////////////////////////////////////////////////////
    // Not supported / used / required for this example
    // //////////////////////////////////////////////////////////////

    @Override
    public int update(Uri uri, ContentValues contentvalues, String s,
                      String[] as) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String s, String[] as) {
        return 0;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentvalues) {
        return null;
    }

    /*  Thanks Paul Dingemans comment at http://stephendnicholas.com/archives/974
        for these two methods. I couldn't figure out how to override the query
        without his help!
     */
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
        // If it returns 1 - then it matches the Uri defined in onCreate
            case 1:
                return "image/jpeg"; // Use an appropriate mime type here
            default:
                return null;
        }
    }
    @Override
    public Cursor query(Uri uri, String[] arg1, String arg2, String[] arg3,
                        String arg4) {
        switch (uriMatcher.match(uri)) {
            // If it returns 1 - then it matches the Uri defined in onCreate
            case 1:
                MatrixCursor cursor = null;
                File file = new File( getStoragePath() + File.separator
                        + uri.getLastPathSegment());
                if (file.exists()) {
                    cursor = new MatrixCursor(new String[] {
                            OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE });
                    cursor.addRow(new Object[] { uri.getLastPathSegment(), file.length() });
                }
                return cursor;
            default:
                return null;
        }
    }

    private String getStoragePath() {
        return getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    }
}