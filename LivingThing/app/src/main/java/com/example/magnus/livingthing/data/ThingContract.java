package com.example.magnus.livingthing.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ThingContract {

    public static final String CONTENT_AUTHOROTY = "com.example.magnus.livingthing";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHOROTY);
    public static final String PATH_PI_DATA = "pi_data";
    public static final String PATH_PI_DATA_LIGHT = "pi_light";

    public static final class DaylyEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PI_DATA).build();
        public static String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHOROTY +
                "/" + PATH_PI_DATA;
        public static final String TABLE_NAME = "data_dayly";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_MOISTURE = "moisture";
        public static final String COLUMN_HUMIDITY = "humidity";

    }

    public static final class HourlyEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PI_DATA_LIGHT).build();
        public static String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHOROTY +
                "/" + PATH_PI_DATA_LIGHT;
        public static final String TABLE_NAME = "data_hourly";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_LIGHT = "light";
        public static final String COLUMN_TEMP = "temp";

    }
}
