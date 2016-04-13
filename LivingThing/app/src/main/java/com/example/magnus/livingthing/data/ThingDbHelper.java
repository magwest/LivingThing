package com.example.magnus.livingthing.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.magnus.livingthing.data.ThingContract.DaylyEntry;
import com.example.magnus.livingthing.data.ThingContract.HourlyEntry;

public class ThingDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 18;
    static final String DATABASE_NAME = "livingthing.db";

    public ThingDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_TABLE = "CREATE TABLE " + DaylyEntry.TABLE_NAME + " (" +
                DaylyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DaylyEntry.COLUMN_MOISTURE + " INT, " +
                DaylyEntry.COLUMN_HUMIDITY + " INT, " +
                DaylyEntry.COLUMN_TIMESTAMP + " INT NOT NULL );";

        final String SQL_CREATE_TABLE_LIGHT = "CREATE TABLE " + HourlyEntry.TABLE_NAME + " (" +
                HourlyEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                HourlyEntry.COLUMN_LIGHT + " INT, " +
                HourlyEntry.COLUMN_TEMP + " INT, " +
                HourlyEntry.COLUMN_TIMESTAMP + " INT NOT NULL );";

        db.execSQL(SQL_CREATE_TABLE);
        db.execSQL(SQL_CREATE_TABLE_LIGHT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v("DB", "Upgrading");
        db.execSQL("DROP TABLE IF EXISTS " + DaylyEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + HourlyEntry.TABLE_NAME);
        onCreate(db);

    }
}
