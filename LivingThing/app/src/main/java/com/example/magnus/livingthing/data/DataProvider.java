package com.example.magnus.livingthing.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.example.magnus.livingthing.data.ThingContract.DaylyEntry;
import static com.example.magnus.livingthing.data.ThingContract.HourlyEntry;


public class DataProvider extends ContentProvider {

    private static ThingDbHelper mDbHelper;
    private static SQLiteDatabase db;
    private static final String TAG = DataProvider.class.getSimpleName();

    public DataProvider() {
    }

    @Override
    public boolean onCreate() {
        Log.v(TAG, "db helper created");
        mDbHelper = new ThingDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        db = mDbHelper.getReadableDatabase();

        String table = null;

        if (uri.equals(DaylyEntry.CONTENT_URI)) {
            table = DaylyEntry.TABLE_NAME;
        }

        if (uri.equals(HourlyEntry.CONTENT_URI)) {
            table = HourlyEntry.TABLE_NAME;
        }

        if (table != null) {

            return db.query(
                    table,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sortOrder
            );
        }

        return null;
    }

    @Override
    public String getType(Uri uri) {

        if (uri.equals(DaylyEntry.CONTENT_URI)) {
            return DaylyEntry.CONTENT_TYPE;
        }

        if (uri.equals(HourlyEntry.CONTENT_URI)) {
            return HourlyEntry.CONTENT_TYPE;
        }

        return null;

    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        db = mDbHelper.getWritableDatabase();

        String table = null;
        String nullable = null;

        if (uri.equals(DaylyEntry.CONTENT_URI)) {
            table = DaylyEntry.TABLE_NAME;
            nullable = DaylyEntry.COLUMN_MOISTURE;
        }

        if (uri.equals(HourlyEntry.CONTENT_URI)) {
            table = HourlyEntry.TABLE_NAME;
            nullable = HourlyEntry.COLUMN_LIGHT;
        }

        long _id = 0;

        if (table != null) {

             _id = db.insert(
                    table,
                    nullable,
                    values
            );
        }

        if (_id > 0)
            return uri.buildUpon().appendEncodedPath(Long.toString(_id)).build();
        else
            throw new android.database.SQLException("Failed to insert row into " + uri);
    }




    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        db = mDbHelper.getWritableDatabase();

        if (uri.equals(DaylyEntry.CONTENT_URI)) {

            return db.delete(
                    DaylyEntry.TABLE_NAME,
                    selection,
                    selectionArgs
            );
        }

        if (uri.equals(HourlyEntry.CONTENT_URI)) {

            return db.delete(
                    HourlyEntry.TABLE_NAME,
                    selection,
                    selectionArgs
            );
        }

        return 0;
    }


    @Override
    //No scenario for this in the Living thing app
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
