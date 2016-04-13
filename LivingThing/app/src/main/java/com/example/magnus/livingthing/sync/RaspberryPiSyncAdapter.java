package com.example.magnus.livingthing.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.example.magnus.livingthing.HTTPHelper;
import com.example.magnus.livingthing.MainActivity;
import com.example.magnus.livingthing.R;
import com.example.magnus.livingthing.data.DataProvider;
import com.example.magnus.livingthing.data.ThingContract;

import org.json.JSONException;

import java.util.Calendar;

public class RaspberryPiSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = RaspberryPiSyncAdapter.class.getSimpleName();
    public static final int SYNC_INTERVAL = 60 * 180; // 3 hours
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3; //1 hour flex

    private static final int NOTIFICATION_ID = 3005;



    public RaspberryPiSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        initializeSyncAdapter(getContext());

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Context context = getContext();
        String jsonStr = HTTPHelper.requestDataFromRaspberryPi(context)[0];

        if (jsonStr != null) try {

            int[] data = HTTPHelper.getDataFromJson(jsonStr);

            int moisture = data[0];
            int humidity = data[1];
            int light = data[2];
            int temp = data[3];

            Log.v(LOG_TAG, Integer.toString(moisture));
            Log.v(LOG_TAG, Integer.toString(humidity));
            Log.v(LOG_TAG, Integer.toString(light));
            Log.v(LOG_TAG, Integer.toString(temp));


            DataProvider db  = new DataProvider();
            Calendar cal = Calendar.getInstance();
            long now = cal.getTimeInMillis();
            cal.setTimeInMillis(now);

            //Store values from pi once a day
            if (updatedToday(db) == false) {

                ContentValues values = new ContentValues();
                values.put(ThingContract.DaylyEntry.COLUMN_MOISTURE, moisture);
                values.put(ThingContract.DaylyEntry.COLUMN_HUMIDITY, humidity);
                values.put(ThingContract.DaylyEntry.COLUMN_TIMESTAMP, now);
                db.insert(ThingContract.DaylyEntry.CONTENT_URI, values);
                deleteIfNecessary(db, ThingContract.DaylyEntry.CONTENT_URI, 30);
            }

            //The light values should be stored all the time.
            ContentValues valuesDayly = new ContentValues();
            valuesDayly.put(ThingContract.HourlyEntry.COLUMN_LIGHT, light);
            valuesDayly.put(ThingContract.HourlyEntry.COLUMN_TEMP, temp);
            valuesDayly.put(ThingContract.HourlyEntry.COLUMN_TIMESTAMP, now);

            db.insert(ThingContract.HourlyEntry.CONTENT_URI, valuesDayly);
            deleteIfNecessary(db, ThingContract.HourlyEntry.CONTENT_URI, 40);

            SharedPreferences prefs = getContext().getSharedPreferences(context.getString(R.string.content_authority), Context.MODE_PRIVATE);


            /**
             * Check if the plant is dry and if the user is notified, otherwise push a notification!
             */
            int lowValue = prefs.getInt(context.getString(R.string.pref_dry), context.getResources().getInteger(R.integer.dry_default));
            int veryLowValue = prefs.getInt(context.getString(R.string.pref_very_dry), context.getResources().getInteger(R.integer.very_dry_default));
            Boolean notifiedDry = prefs.getBoolean(context.getString(R.string.pref_dry_notified), false);
            Boolean notifiedVeryDry = prefs.getBoolean(context.getString(R.string.pref_very_dry_notified), false);

            if (moisture <= lowValue && !notifiedDry) {

                    notifyMoisture("Your plant is getting dry, please put some water");
                    prefs.edit().putBoolean(context.getString(R.string.pref_dry_notified), true).apply();

            } else if (moisture <= veryLowValue && !notifiedVeryDry) {

                    notifyMoisture("Hurry up! Your plant is very dry and need water!!!");
                    prefs.edit().putBoolean(context.getString(R.string.pref_very_dry_notified), true).apply();

            } else if (moisture > lowValue){
                if (notifiedDry) prefs.edit().putBoolean(context.getString(R.string.pref_dry_notified), false).apply();
                if (notifiedVeryDry) prefs.edit().putBoolean(context.getString(R.string.pref_very_dry_notified), false).apply();
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error when trying parse json", e);
        }
    }

    Boolean updatedToday(DataProvider db) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        long today = cal.getTimeInMillis();

        Cursor cursor = db.query(ThingContract.DaylyEntry.CONTENT_URI, new String[] {ThingContract.DaylyEntry.COLUMN_TIMESTAMP}, null, null, ThingContract.DaylyEntry.COLUMN_TIMESTAMP);

        if (cursor.moveToLast()) {
            long timestampFromDb = cursor.getLong(cursor.getColumnIndex(ThingContract.DaylyEntry.COLUMN_TIMESTAMP));
            Log.v("TODAY ", Long.toString(today));
            Log.v("FROM DB ", Long.toString(timestampFromDb));
            return timestampFromDb >= today;

        }

        return false;
    }

    void deleteIfNecessary(DataProvider db, Uri uri, int count) {
        String id;
        String timestamp;
        if (uri.equals(ThingContract.DaylyEntry.CONTENT_URI)) {
            id = ThingContract.DaylyEntry._ID;
            timestamp = ThingContract.DaylyEntry.COLUMN_TIMESTAMP;
        } else {
            id = ThingContract.HourlyEntry._ID;
            timestamp = ThingContract.HourlyEntry.COLUMN_TIMESTAMP;
        }

        Cursor cursor = db.query(uri, new String[] {id}, null, null, timestamp);

        Log.v("CURSOR COUNT", Integer.toString(cursor.getCount()));
        if (cursor.getCount() > count && cursor.moveToFirst()) {

            int _id = cursor.getInt(cursor.getColumnIndex(id));
            db.delete(uri, id + " LIKE ?", new String[]{Integer.toString(_id)});
        }

    }




    /**
     * Makes a notification...
     */
    void notifyMoisture(String message) {
        Context context = getContext();
        String title = context.getString(R.string.app_name);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setSound(sound);
        Intent resultIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }




    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, context.getString(R.string.content_authority), null)) {
                return null;
            }
            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }



    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * configure periodic sync
         */
        RaspberryPiSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * enable periodic syn
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);


    }

    public static void initializeSyncAdapter(Context context) {
        Log.v("SYNC", " INIT");
        getSyncAccount(context);
    }

}

