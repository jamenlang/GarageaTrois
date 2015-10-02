package com.airlim.garagetrois;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
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
import android.widget.Toast;

import static java.lang.Integer.valueOf;


public class Login extends Activity implements NumberPicker.OnValueChangeListener{


    private TextView textView;
    //volatile String a, a2, a3, a4;

    volatile String authd = "false";
    volatile String admind = "false";
    volatile String geofence = "false";
    volatile String switch_array = "";
    GPSTracker gps;
    public void onBackPressed() {
        if(textView.getText() == "Log in by entering your PIN") {
            finish();
        }
        else {
            textView.setText("Log in by entering your PIN");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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
        np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                np3.requestFocus();
            }
        });
        np3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                np4.requestFocus();
            }
        });
        np4.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            }
        });
        gps = new GPSTracker(Login.this);
        if(gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            // \n is for new line
            Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
            if(debug)
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            Boolean gpsNag = Client_Functions.getPrefBool("gpsNag", getApplicationContext());
            if(gpsNag)
                gps.showSettingsAlert();
            // only do this once.
            SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
            prefs.putBoolean("gpsNag", false);
            prefs.commit();
        }
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal)
    {
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
            if(result.startsWith(adminresult)){
                String[] response_var = result.split(",");
                geofence = response_var[1];
                Log.v("geofence",geofence);
                Log.v("result",result);
                authd = "true";
                admind = "true";
                show();
                finish();
            }
            else if(result.startsWith(userresult)){
                String[] response_var = result.split(",");
                geofence = response_var[1];
                Log.v("geofence",geofence);
                Log.v("result",result);
                authd = "true";
                show();
                finish();
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


    public void onClick(View view) {
        if(authd.equals("false")){
            LogInTask task = new LogInTask();
            NumberPicker numPicker = (NumberPicker)findViewById(R.id.numberPicker1);
            int x = numPicker.getValue();
            NumberPicker numPicker2 = (NumberPicker)findViewById(R.id.numberPicker2);
            int x2 = numPicker2.getValue();
            NumberPicker numPicker3 = (NumberPicker)findViewById(R.id.numberPicker3);
            int x3 = numPicker3.getValue();
            NumberPicker numPicker4 = (NumberPicker)findViewById(R.id.numberPicker4);
            int x4 = numPicker4.getValue();
            String formatted = String.format("%d%d%d%d", x, x2, x3, x4);
            //String formatted = String.format("%s%s%s%s", a, a2, a3, a4);
            task.execute(formatted);
        }
        else
            show();
    }

    public void show()
    {
        final Context context = this;
        Intent intent = new Intent(context, Control.class);
        NumberPicker numPicker = (NumberPicker)findViewById(R.id.numberPicker1);
        int x = numPicker.getValue();
        NumberPicker numPicker2 = (NumberPicker)findViewById(R.id.numberPicker2);
        int x2 = numPicker2.getValue();
        NumberPicker numPicker3 = (NumberPicker)findViewById(R.id.numberPicker3);
        int x3 = numPicker3.getValue();
        NumberPicker numPicker4 = (NumberPicker)findViewById(R.id.numberPicker4);
        int x4 = numPicker4.getValue();
        String formatted = String.format("%d%d%d%d", x, x2, x3, x4);
        intent.putExtra("uid", formatted);
        intent.putExtra("geofence", geofence);
        intent.putExtra("nfc", getNfc_support());
        intent.putExtra("devicename", getDeviceName());
        intent.putExtra("did", getAndroid_id());
        intent.putExtra("number", getTel_number());
        intent.putExtra("switch_array", switch_array);
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
