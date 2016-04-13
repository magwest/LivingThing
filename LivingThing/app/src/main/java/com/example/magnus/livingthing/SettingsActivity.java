package com.example.magnus.livingthing;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private final String TAG = SettingsActivity.class.getSimpleName();

    private ListView settingListView;
    private TextView greaterThanTextView;
    private TextView lesserThanTextView;
    private SharedPreferences prefs;
    private int dry = 0;
    private int veryDry = 0;
    private int wet = 0;
    private int tooMuch = 0;
    private EditText urlInput;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        greaterThanTextView = (TextView) findViewById(R.id.setting_greaterthan);
        lesserThanTextView = (TextView) findViewById(R.id.setting_lesserthan);
        settingListView = (ListView) findViewById(R.id.settingListView);
        prefs = getSharedPreferences(getString(R.string.content_authority), Context.MODE_PRIVATE);
        urlInput = (EditText) findViewById(R.id.urlEditText);

        Button saveUrlBtn = (Button) findViewById(R.id.urlBtn);
        saveUrlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlStr = String.valueOf(urlInput.getText());
                prefs.edit().putString(getString(R.string.pref_raspberry_pi_url), urlStr).apply();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Resources res = getResources();

        dry = prefs.getInt(getString(R.string.pref_dry), res.getInteger(R.integer.dry_default));
        veryDry = prefs.getInt(getString(R.string.pref_very_dry), res.getInteger(R.integer.very_dry_default));
        wet = prefs.getInt(getString(R.string.pref_wet), res.getInteger(R.integer.wet_default));
        tooMuch = prefs.getInt(getString(R.string.pref_too_much),res.getInteger(R.integer.very_wet_default));
        greaterThanTextView.setText(Integer.toString(dry) +  " (dry)");
        lesserThanTextView.setText(Integer.toString(wet) + " (wet)");

        String[] valueTags = {
                "Dry    <   " + Integer.toString(dry),
                "Very Dry   <   " + Integer.toString(veryDry),
                "Wet    >    " + Integer.toString(wet),
                "Too Wet   >   " + Integer.toString(tooMuch)
        };

        List<String> valueList = new ArrayList<String>(Arrays.asList(valueTags));

        ArrayAdapter<String> mValueItemAdapter = new ArrayAdapter<String>(
                this,
                R.layout.list_item_setting,
                R.id.list_item_setting_textview,
                valueList
        );


        settingListView.setAdapter(mValueItemAdapter);
        settingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showPickerDialog(position);
            }
        });

        String url = prefs.getString(getString(R.string.pref_raspberry_pi_url), getString(R.string.pref_raspberry_pi_url_default));
        urlInput.setText(url);
    }




    public void showPickerDialog(int ix) {
        int val = 1;
        int lower = 0;
        int higher = 2;
        final int index = ix;

        switch (index) {
            case 0:
                lower = veryDry + 1;
                higher = wet - 1;
                val = dry;
                break;
            case 1:
                lower = 0;
                higher = dry - 1;
                val = veryDry;
                break;
            case 2:
                lower = dry + 1;
                higher = tooMuch - 1;
                val = wet;
                break;
            case 3:
                lower = wet + 1;
                higher = 1023;
                val = tooMuch;
                break;
        }


        final Dialog d = new Dialog(this);
        d.setTitle("NumberPicker");
        d.setContentView(R.layout.dialog);
        Button b1 = (Button) d.findViewById(R.id.button1);
        final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
        np.setMaxValue(higher);
        np.setMinValue(lower);

        np.setWrapSelectorWheel(false);
        np.setValue(val);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPreferenceValue(index, np.getValue());
                d.dismiss();
                onResume();
            }
        });

        d.show();
    }

    public void setPreferenceValue(int ix, int val) {

        switch (ix) {
            case 0:
                prefs.edit().putInt(getString(R.string.pref_dry), val).apply();
                break;
            case 1:
                prefs.edit().putInt(getString(R.string.pref_very_dry), val).apply();
                break;
            case 2:
                prefs.edit().putInt(getString(R.string.pref_wet), val).apply();
                break;
            case 3:
                prefs.edit().putInt(getString(R.string.pref_too_much), val).apply();
                break;
        }
    }
}
