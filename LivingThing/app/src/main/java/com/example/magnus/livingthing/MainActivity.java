package com.example.magnus.livingthing;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.magnus.livingthing.sync.RaspberryPiSyncService;

import org.json.JSONException;

import java.text.MessageFormat;

public class MainActivity extends AppCompatActivity {

    private TextView moisture = null;
    private TextView temp = null;
    private TextView light = null;
    private TextView humidity = null;
    private TextView moistureVal = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0f);
        moisture = (TextView) findViewById(R.id.valueViewSoil);
        moistureVal = (TextView) findViewById(R.id.valueViewMoisture);
        temp = (TextView) findViewById(R.id.valueViewTemp);
        light = (TextView) findViewById(R.id.valueViewLight);
        humidity = (TextView) findViewById(R.id.valueViewHumidity);

        Button startCamBtn = (Button) findViewById(R.id.liveCamButton);
        startCamBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), VideoViewActivity.class));
            }
        });

        ImageView indicatorImage = (ImageView) findViewById(R.id.indicatorImage);
        indicatorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchPlantTask().execute();
            }
        });

        Button chartsBtn = (Button) findViewById(R.id.chartsButton);

        chartsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ChartsActivity.class));
            }
        });

        startService(new Intent(this, RaspberryPiSyncService.class));

    }


    @Override
    protected void onResume() {
        super.onResume();
        // Fetch data every time it
        // activity is resumed to reload
        new FetchPlantTask().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Fetching the data from raspberry pi!
     */
    public class FetchPlantTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchPlantTask.class.getSimpleName();


        @Override
        protected String[] doInBackground(String... params) {

            return HTTPHelper.requestDataFromRaspberryPi(getApplicationContext());
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) try {
                //Integer array is return i order: {soilMoisture,humidity,light,temp}
                int[] data = HTTPHelper.getDataFromJson(result[0]);
                int moist = data[0];
                String dirtLevel = null;
                int indicator = -1;
                Resources res = getResources();
                SharedPreferences prefs = getSharedPreferences(getString(R.string.content_authority), Context.MODE_PRIVATE);

                int dry = prefs.getInt(getString(R.string.pref_dry), res.getInteger(R.integer.dry_default));
                int veryDry = prefs.getInt(getString(R.string.pref_very_dry), res.getInteger(R.integer.very_dry_default));
                int wet = prefs.getInt(getString(R.string.pref_wet), res.getInteger(R.integer.wet_default));
                int tooMuch = prefs.getInt(getString(R.string.pref_too_much), res.getInteger(R.integer.very_wet_default));


                if (moist <= veryDry) {
                    dirtLevel = "Very dry!!";
                    indicator = R.drawable.ic_very_dry;
                } else if (moist <= dry) {
                    dirtLevel = "Dry";
                    indicator = R.drawable.ic_dry;
                } else if (moist > dry && moist < wet ) {
                    dirtLevel = "Moist";
                    indicator = R.drawable.ic_good;
                } else if (moist >= tooMuch ) {
                    dirtLevel = "Very wet!";
                    indicator = R.drawable.ic_too_much;
                } else if (moist >= wet) {
                    dirtLevel = "Wet";
                    indicator = R.drawable.ic_ok;
                }
                moisture.setText(dirtLevel);
                moistureVal.setText(Integer.toString(data[0]));
                humidity.setText(MessageFormat.format("{0} %", Integer.toString(data[1])));
                light.setText(MessageFormat.format("{0} %", Integer.toString(data[2])));
                temp.setText(MessageFormat.format("{0} °C", Integer.toString(data[3])));
                ImageView indicatorImage = (ImageView) findViewById(R.id.indicatorImage);
                if (indicator != -1) indicatorImage.setImageResource(indicator);


            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error when trying parse json", e);
            }

        }
    }
}
