package com.example.magnus.livingthing;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.magnus.livingthing.data.DataProvider;
import com.example.magnus.livingthing.data.ThingContract;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class ChartsActivity extends AppCompatActivity {

    private final String LOG_TAG = ChartsActivity.class.getSimpleName();
    private SectionsPagerAdapter mSectionsPagerAdapter;

    static List<PointValue> moistureValues;
    static List<PointValue> tempValues;
    static List<PointValue> humidityValues;
    static List<PointValue> lightValues;
    static List<AxisValue> axisValuesDays;
    static List<AxisValue> axisValuesHours;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_charts);
        getSupportActionBar().setElevation(0f);


        moistureValues = new ArrayList<>();
        tempValues = new ArrayList<>();
        humidityValues = new ArrayList<>();
        lightValues = new ArrayList<>();
        axisValuesDays = new ArrayList<>();
        axisValuesHours = new ArrayList<>();
        //Get data from SQLite db through the Data Provider
        getDataFromSQLite();


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPagerWithException) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_charts, menu);
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


    public void getDataFromSQLite() {

        DataProvider db = new DataProvider();

        String[] projection = {
                ThingContract.DaylyEntry.COLUMN_MOISTURE,
                ThingContract.DaylyEntry.COLUMN_HUMIDITY,
                ThingContract.DaylyEntry.COLUMN_TIMESTAMP
        };

        Cursor cursor = db.query(ThingContract.DaylyEntry.CONTENT_URI, projection, null, null, ThingContract.DaylyEntry.COLUMN_TIMESTAMP);

        Calendar cal = Calendar.getInstance();

        cursor.moveToFirst();
        int i = 0;
        while(cursor.isAfterLast() == false) {
            int y_m = cursor.getInt(cursor.getColumnIndex(ThingContract.DaylyEntry.COLUMN_MOISTURE));
            int y_h = cursor.getInt(cursor.getColumnIndex(ThingContract.DaylyEntry.COLUMN_HUMIDITY));
            long timestamp = cursor.getLong(cursor.getColumnIndex(ThingContract.DaylyEntry.COLUMN_TIMESTAMP));
            cal.setTimeInMillis(timestamp);
            String date = Integer.toString(cal.get(Calendar.DAY_OF_MONTH)) + "/" + Integer.toString(cal.get(Calendar.MONTH));

            axisValuesDays.add(new AxisValue(i).setLabel(date));
            moistureValues.add(new PointValue(i, y_m));
            humidityValues.add(new PointValue(i, y_h));
            i++;
            cursor.moveToNext();

        }

        String[] projectionLight = {
                ThingContract.HourlyEntry.COLUMN_LIGHT,
                ThingContract.HourlyEntry.COLUMN_TEMP,
                ThingContract.HourlyEntry.COLUMN_TIMESTAMP
        };


        Cursor cursorDay = db.query(ThingContract.HourlyEntry.CONTENT_URI, projectionLight, null, null, ThingContract.HourlyEntry.COLUMN_TIMESTAMP);


        cursorDay.moveToFirst();
        i = 0;
        while(cursorDay.isAfterLast() == false) {

            int y = cursorDay.getInt(cursorDay.getColumnIndex(ThingContract.HourlyEntry.COLUMN_LIGHT));
            int y_t = cursorDay.getInt(cursorDay.getColumnIndex(ThingContract.HourlyEntry.COLUMN_TEMP));
            long timestamp = cursorDay.getLong(cursorDay.getColumnIndex(ThingContract.HourlyEntry.COLUMN_TIMESTAMP));
            cal.setTimeInMillis(timestamp);
            String hour = Integer.toString(cal.get(Calendar.HOUR_OF_DAY));
            if (hour.length() == 1) {
                hour = "0" + hour;
            }


            axisValuesHours.add(new AxisValue(i).setLabel(hour ));
            lightValues.add(new PointValue(i, y));
            tempValues.add(new PointValue(i, y_t));
            i++;
            cursorDay.moveToNext();

        }

        cal.setTimeInMillis(Calendar.getInstance().getTimeInMillis());


    }



    /**
     * A fragment with a linechart
     */
    public static class PlaceholderFragment extends Fragment {


        private static final String LOG_TAG = PlaceholderFragment.class.getSimpleName();
        private static final String ARG_SECTION_NUMBER = "section_number";


        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_charts, container, false);
            int sectionNr = getArguments().getInt(ARG_SECTION_NUMBER); //Use section nr

            //Create the line chart
            LineChartView lineChartView = new LineChartView(getContext());


            String axisXname = "Day";
            String axisYname = null;
            List<PointValue> values = null;
            List<AxisValue> axisVals = axisValuesDays;
            float max = -1;
            float min = -1;


            switch(sectionNr) {
                case 1:
                    axisYname = "Moisture value";
                    values = moistureValues;
                    break;


                case 2:
                    axisYname = "Humididty %";
                    values = humidityValues;
                    max = 100;
                    min = 0;
                    break;

                case 3:
                    axisXname = "Hour";
                    axisYname = "Temp °C";
                    values = tempValues;
                    max = 50;
                    min = 0;
                    axisVals = axisValuesHours;
                    break;

                case 4:
                    axisXname = "Hour";
                    axisYname = "Light %";
                    max = 100;
                    min = 0;
                    values = lightValues;
                    axisVals = axisValuesHours;
                    break;
            }

            Log.v(LOG_TAG + axisXname, values.toString());

            Line line = new Line(values).setColor(Color.GREEN).setCubic(false);
            List<Line> lines = new ArrayList<>();
            lines.add(line);

            LineChartData data = new LineChartData();
            Axis axisX = new Axis();
            axisX.setName(axisXname);
            axisX.setValues(axisVals);

            Axis axisY = new Axis();
            axisY.setHasLines(true);
            axisY.setName(axisYname);
            //axisY.setFormatter(new SimpleAxisValueFormatter(1));

            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
            data.setLines(lines);


            lineChartView.setLineChartData(data);
            lineChartView.setZoomType(ZoomType.HORIZONTAL);

            if (min != -1 && max != -1) {
                final Viewport v = new Viewport(lineChartView.getMaximumViewport());
                v.top = max;
                v.bottom = min;
                lineChartView.setMaximumViewport(v);
                lineChartView.setCurrentViewport(v);
            }

            lineChartView.setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL);
            ((RelativeLayout) rootView).addView(lineChartView);



            return rootView;
        }



    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Soil Moisture";
                case 1:
                    return "Humidity";
                case 2:
                    return "Temperature";
                case 3:
                    return "Light";
            }
            return null;
        }





    }
}
