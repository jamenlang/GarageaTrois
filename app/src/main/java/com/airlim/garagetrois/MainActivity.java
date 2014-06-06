package com.airlim.garagetrois;

import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
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
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.airlim.garagetrois.MainActivity;

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

public class MainActivity extends ActionBarActivity implements NumberPicker.OnValueChangeListener{


    private TextView textView;
    //volatile String a, a2, a3, a4;

    volatile String authd = "false";
    volatile String admind = "false";
    volatile String geofence = "false";
    GPSTracker gps;
    public void onBackPressed() {
        finish();
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
                //a = String.valueOf(np.getValue());
            }
        });
        np2.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //a2 = String.valueOf(np2.getValue());
            }
        });
        np3.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //a3 = String.valueOf(np3.getValue());
            }
        });
        np4.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                //a4 = String.valueOf(np4.getValue());
            }
        });

        gps = new GPSTracker(MainActivity.this);
        if(gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            // \n is for new line
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            gps.showSettingsAlert();
        }
    }
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal)
    {
        Log.i(picker + "value is", "" + newVal);
    }

    private class LogInTask extends AsyncTask<String, String, String> {
        volatile String android_id = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        String server = getResources().getString(R.string.server_URL);
        String path = getResources().getString(R.string.script_path);
        String script = getResources().getString(R.string.script_name);
        String fullurl = "http://"+server+((path != "")?"/"+path+"/"+script : script);
        String userresult = getResources().getString(R.string.userresult);
        String adminresult = getResources().getString(R.string.adminresult);

        volatile String nfc_support = String.valueOf(getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC));
        TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        public String getDeviceName() {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                return model;
            } else {
                return manufacturer + " " + model;
            }
        }
        String number = telMgr.getLine1Number();
        protected String doInBackground(String... urls) {

            String response = "";

                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost httpPOST = new HttpPost(fullurl);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("DID", android_id));
                    params.add(new BasicNameValuePair("TelNum", number));
                    params.add(new BasicNameValuePair("DeviceName", getDeviceName()));
                    params.add(new BasicNameValuePair("Latitude", String.valueOf(latitude)));
                    params.add(new BasicNameValuePair("Longitude", String.valueOf(longitude)));
                    params.add(new BasicNameValuePair("hasNFC", nfc_support));
                    params.add(new BasicNameValuePair("UID", urls[0]));
                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params,HTTP.UTF_8);
                    httpPOST.setEntity(ent);
                    HttpResponse execute = client.execute(httpPOST);
                    InputStream content = execute.getEntity().getContent();

                    BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
                    String s = "";
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
                String[] geofence_var = result.split(",");
                geofence = geofence_var[1];
                Log.v("geofence",geofence);
                Log.v("result",result);
                authd = "true";
                admind = "true";
                show();
                finish();
            }
            else if(result.startsWith(userresult)){
                String[] geofence_var = result.split(",");
                geofence = geofence_var[1];
                Log.v("geofence",geofence);
                Log.v("result",result);
                authd = "true";
                show();
                finish();
            }
            else{
                textView.setText(result);
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
        if (admind.equals("true")){
            intent.putExtra("admind", "true");
            intent.putExtra("geofence", geofence);
        }
        else {
            intent.putExtra("admind", "false");
            intent.putExtra("geofence", geofence);
        }
        startActivity(intent);
    }
}
