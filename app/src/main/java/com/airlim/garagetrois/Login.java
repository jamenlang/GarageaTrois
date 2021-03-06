package com.airlim.garagetrois;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import android.widget.Toast;

import static java.lang.Integer.valueOf;


public class Login extends Activity implements NumberPicker.OnValueChangeListener{


    private TextView textView;
    //volatile String a, a2, a3, a4;
    volatile String uid = "";
    public String tryuid = "gps0";
    volatile String authd = "false";
    volatile String admind = "false";
    volatile String geofence = "false";
    GPSTracker gps;
    public void onBackPressed() {
        if(textView.getText() == "Log in by entering your PIN") {
            finish();
        }
        else {
            textView.setText("Log in by entering your PIN");
        }
    }

    private EditText findInput(ViewGroup np) {
        int count = np.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = np.getChildAt(i);
            if (child instanceof ViewGroup) {
                findInput((ViewGroup) child);
            } else if (child instanceof EditText) {
                return (EditText) child;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if(authd.equals("false")){
            gps = new GPSTracker(this);
            if(gps.canGetLocation()) {
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                LogInTask task = new LogInTask();

                task.execute(tryuid);
                // \n is for new line
                Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
                if(debug)
                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            } else {
                // Can't get location.
                // GPS or network is not enabled.
                // Ask user to enable GPS/network in settings.
                Boolean gpsNag = Client_Functions.getPrefBool("gpsNag", getApplicationContext());
                Log.v("gpsnagstatus", gpsNag.toString());
                if (gpsNag)
                    gps.showSettingsAlert();
                // only do this once.
                SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
                prefs.putBoolean("gpsNag", false);
                prefs.commit();
            }
        }

        setContentView(R.layout.activity_main);
        //a = a2 = a3 = a4 = "0";
        textView = (TextView) findViewById(R.id.TextView01);

        final NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker1);
        final NumberPicker np2 = (NumberPicker) findViewById(R.id.numberPicker2);
        final NumberPicker np3 = (NumberPicker) findViewById(R.id.numberPicker3);
        final NumberPicker np4 = (NumberPicker) findViewById(R.id.numberPicker4);
        np.setMaxValue(9); // max value 100
        np.setMinValue(0);   // min value 0
        np.setWrapSelectorWheel(true);
        np.setOnValueChangedListener(this);
        np2.setMaxValue(9); // max value 100
        np2.setMinValue(0);   // min value 0
        np2.setWrapSelectorWheel(true);
        np2.setOnValueChangedListener(this);
        np3.setMaxValue(9); // max value 100
        np3.setMinValue(0);   // min value 0
        np3.setWrapSelectorWheel(true);
        np3.setOnValueChangedListener(this);
        np4.setMaxValue(9); // max value 100
        np4.setMinValue(0);   // min value 0
        np4.setWrapSelectorWheel(true);
        np4.setOnValueChangedListener(this);
        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                np2.requestFocus();
            }
        });

        EditText input = findInput(np);
        TextWatcher tw = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() != 0) {
                    Integer value = Integer.parseInt(s.toString());
                    if (value >= np.getMinValue()) {
                        np.setValue(value);
                        np2.requestFocus();
                    }
                }
            }
        };
        input.addTextChangedListener(tw);

        np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                np3.requestFocus();
            }
        });

        EditText input2 = findInput(np2);
        TextWatcher tw2 = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() != 0) {
                    Integer value = Integer.parseInt(s.toString());
                    if (value >= np2.getMinValue()) {
                        np2.setValue(value);
                        np3.requestFocus();
                    }
                }
            }
        };
        input2.addTextChangedListener(tw2);

        np3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                np4.requestFocus();
            }
        });

        EditText input3 = findInput(np3);
        TextWatcher tw3 = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() != 0) {
                    Integer value = Integer.parseInt(s.toString());
                    if (value >= np3.getMinValue()) {
                        np3.setValue(value);
                        np4.requestFocus();
                    }
                }
            }
        };
        input3.addTextChangedListener(tw3);

        np4.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            }
        });
        EditText input4 = findInput(np4);
        TextWatcher tw4 = new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() != 0) {
                    Integer value = Integer.parseInt(s.toString());
                    if (value >= np4.getMinValue()) {
                        np4.setValue(value);
                        np4.clearFocus();
                        LogInTask task = new LogInTask();
                        //String formatted = String.format("%s%s%s%s", a, a2, a3, a4);
                        task.execute(createuid());
                    }
                }
            }
        };
        input4.addTextChangedListener(tw4);
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        Log.i(picker + "value is", "" + newVal);
    }

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    public String getNfc_support() {
        return String.valueOf(getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC));

    }

    public String getAndroid_id() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public String getTel_number() {
        TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        return telMgr.getLine1Number();
    }

    private class LogInTask extends AsyncTask<String, String, String> {

        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        String server = Client_Functions.getPref("server_URL", getApplicationContext());
        String path = Client_Functions.getPref("script_path", getApplicationContext());
        String script = Client_Functions.getPref("script_name", getApplicationContext());

        String fullurl = "http://"+server+((path.equals(""))? script : "/"+path+"/"+script);
        String userresult = Client_Functions.getPref("userresult", getApplicationContext());
        String adminresult = Client_Functions.getPref("adminresult", getApplicationContext());

        protected String doInBackground(String... urls) {

            String response = "";

                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost httpPOST = new HttpPost(fullurl);
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("DID", getAndroid_id()));
                    params.add(new BasicNameValuePair("TelNum", getTel_number()));
                    params.add(new BasicNameValuePair("DeviceName", getDeviceName()));
                    params.add(new BasicNameValuePair("Latitude", String.valueOf(latitude)));
                    params.add(new BasicNameValuePair("Longitude", String.valueOf(longitude)));
                    params.add(new BasicNameValuePair("hasNFC", getNfc_support()));
                    params.add(new BasicNameValuePair("UID", urls[0]));
                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
                    httpPOST.setEntity(ent);
                    HttpResponse execute = client.execute(httpPOST);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s;
                    while ((s = buffer.readLine()) != null) {
                        response += s;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            return response;
        }
        protected void onPostExecute(String result) {

            if(result.startsWith(Hash.md5(adminresult))){
                String[] response_var = result.split(",");
                geofence = response_var[1];
                Log.v("result",result);
                Log.v("geofence",geofence);

                authd = "true";
                admind = "true";
                show();
                //finish();
            }
            else if(result.startsWith(Hash.md5(userresult))){
                String[] response_var = result.split(",");
                geofence = response_var[1];
                Log.v("geofence",geofence);
                Log.v("result",result);
                authd = "true";
                show();
                //finish();
            }
            else{
                Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
                if(debug)
                   textView.setText(result);
                else
                  textView.setText("Invalid Response From Server");
            }
        }

    }
    public String createuid(){
        NumberPicker numPicker = (NumberPicker)findViewById(R.id.numberPicker1);
        numPicker.clearFocus();
        int x = numPicker.getValue();
        NumberPicker numPicker2 = (NumberPicker)findViewById(R.id.numberPicker2);
        numPicker2.clearFocus();
        int x2 = numPicker2.getValue();
        NumberPicker numPicker3 = (NumberPicker)findViewById(R.id.numberPicker3);
        numPicker3.clearFocus();
        int x3 = numPicker3.getValue();
        NumberPicker numPicker4 = (NumberPicker)findViewById(R.id.numberPicker4);
        numPicker4.clearFocus();
        int x4 = numPicker4.getValue();
        String formatted = String.format("%d%d%d%d", x, x2, x3, x4);
        return formatted;

    }

    public void onClick(View view) {
            LogInTask task = new LogInTask();
            //String formatted = String.format("%s%s%s%s", a, a2, a3, a4);
            task.execute(createuid());
    }

    public void show()
    {
        final Context context = this;
        Intent intent = new Intent(context, Control.class);
        if(tryuid.equals("gps0") && Objects.equals(createuid(), "0000")) {
            tryuid = "";
            intent.putExtra("uid", "gps0");
        }
        else{
            intent.putExtra("uid", createuid());
        }
        intent.putExtra("authd", "true");
        intent.putExtra("geofence", geofence);
        intent.putExtra("nfc", getNfc_support());
        intent.putExtra("devicename", getDeviceName());
        intent.putExtra("did", getAndroid_id());
        intent.putExtra("number", getTel_number());
        if (admind.equals("true")){
            intent.putExtra("admind", "true");
        }
        else {
            intent.putExtra("admind", "false");
        }
        startActivity(intent);
    }
    public void showgatsettings()
    {
        final Context context = this;
        Intent intent = new Intent(context, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Boolean disablesettings = Client_Functions.getPrefBool("disablesettings", getApplicationContext());
        if(!disablesettings) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem item1 = menu.findItem(R.id.action_log);
        item1.setVisible(false);
        MenuItem item2 = menu.findItem(R.id.action_users);
        item2.setVisible(false);
        MenuItem item3 = menu.findItem(R.id.action_devices);
        item3.setVisible(false);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        switch (item.getItemId()){
            case R.id.action_settings:
                showgatsettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
