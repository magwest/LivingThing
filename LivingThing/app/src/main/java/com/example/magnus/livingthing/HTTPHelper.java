package com.example.magnus.livingthing;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPHelper {

    public static
    final String LOG_TAG = HTTPHelper.class.getSimpleName();


    public static int[] getDataFromJson(String jsonStr) throws JSONException {

        JSONObject jsonData = new JSONObject(jsonStr);
        int soilMoisture = jsonData.getInt("soil moisture");
        int humidity = jsonData.getInt("humidity");
        int light = jsonData.getInt("light");
        int temp = jsonData.getInt("temperature");
        return new int[]{soilMoisture,humidity,light,temp};

    }

    public static String[] requestDataFromRaspberryPi(Context context) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr = null;

        try {
            SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.content_authority), Context.MODE_PRIVATE);
            String urlStr = prefs.getString(context.getString(R.string.pref_raspberry_pi_url), context.getString(R.string.pref_raspberry_pi_url_default));
            URL raspberryPiUrl = new URL("http://" + urlStr);

            urlConnection = (HttpURLConnection) raspberryPiUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read data
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            jsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {

                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }

            }
        }


        String[] returnArr = {jsonStr};

        //Return json data if not empty
        return (jsonStr != null) ? returnArr : null;
    }

}
