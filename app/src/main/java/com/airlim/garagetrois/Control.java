package com.airlim.garagetrois;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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


public class Control extends MainActivity {
    TextView textView;
    volatile String reverse = "false";
    volatile String uid = "0000";
    GPSTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        admind = intent.getStringExtra("admind");
        geofence = intent.getStringExtra("geofence");
        Log.v("geofence",geofence);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.control);
        ToggleButton b3 = (ToggleButton) findViewById(R.id.button3);
        ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
        ToggleButton b4 = (ToggleButton) findViewById(R.id.button4);
        textView = (TextView) findViewById(R.id.textView02);

        gps = new GPSTracker(Control.this);
        if(gps.canGetLocation()) {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            if(geofence.equals("true")){
                textView.setText("Geofence Enabled: GPS Ready.");
                if(String.valueOf(latitude) == "0.0"){
                    textView.setText("Geofence Enabled: GPS Empty.");
                }
            }

            Log.v("geofence",geofence);
            // \n is for new line
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            if(geofence.equals("true")){
                textView.setText("Geofence Enabled: GPS Not Available.");
            }
            gps.showSettingsAlert();
        }
        b3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                textView.setText("Toggling light...");
                ToggleButton tb = (ToggleButton) v;
                tb.setEnabled(!tb.isEnabled());
                tb.setChecked(!tb.isChecked());
                ControlTask task = new ControlTask();
                task.execute(new String[] {"Light"});

            }
        });
        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                textView.setText("Toggling door...");
                ToggleButton tb = (ToggleButton) v;
                tb.setEnabled(!tb.isEnabled());
                tb.setChecked(!tb.isChecked());
                ControlTask task = new ControlTask();
                task.execute(new String[]{"Door"});
            }
        });
        b4.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                textView.setText("Toggling lock...");
                ToggleButton tb = (ToggleButton) v;
                tb.setEnabled(!tb.isEnabled());
                tb.setChecked(!tb.isChecked());
                ControlTask task = new ControlTask();
                task.execute(new String[]{"Lock"});
            }

        });


    }

    public void onBackPressed() {
        finish();
    }

    public class LoadData extends AsyncTask<Void, Void, Void> {
        ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
        TextView textView = (TextView) findViewById(R.id.textView02);
        ProgressBar myProgress = (ProgressBar) findViewById(R.id.progressBar);
        @Override
        protected void onPreExecute()
        {
            textView.setText("Toggling door...");
            myProgress.setMax(100);
            myProgress.setVisibility(View.VISIBLE);
        }
        protected Void doInBackground(Void... params)
        {
            int var = (reverse.equals("false")) ? 0 : 100;
            int goal = (reverse.equals("false")) ? 100 : 0;
            while(var != goal)
            {
                try {
                    Thread.sleep(114);
                    //my door takes exactly 11.4 seconds to open and close.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (reverse.equals("false"))
                    var++;
                else
                    var--;
                myProgress.setProgress(var);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        textView.setText("Moving...");
                    }
                });
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            b2.setEnabled(!b2.isEnabled());
            b2.setChecked(!b2.isChecked());
            textView.setText("Door toggled");
            if(reverse.equals("false"))
                reverse = "true";
            else
                reverse = "false";
            super.onPostExecute(result);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (admind.equals("true")){
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        else
            return false;
    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        super.onValueChange(picker, oldVal, newVal);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()){
        case R.id.action_log:
            show_log();
            return true;
        case R.id.action_users:
             show_users();
             return true;
        case R.id.action_devices:
             show_devices();
             return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private class ControlTask extends AsyncTask<String, String, String> {
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        volatile String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        volatile String nfc_support = String.valueOf(getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC));
        String server = getResources().getString(R.string.server_URL);
        String path = getResources().getString(R.string.script_path);
        String script = getResources().getString(R.string.script_name);
        String fullurl = "http://"+server+((path != "")?"/"+path+"/"+script : script);


        public String getDeviceName() {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                return model;
            } else {
                return manufacturer + " " + model;
            }
        }


        private String capitalize(String s) {
            if (s == null || s.length() == 0) {
                return "";
            }
            char first = s.charAt(0);
            if (Character.isUpperCase(first)) {
                return s;
            } else {
                return Character.toUpperCase(first) + s.substring(1);
            }
        }
        protected String doInBackground(String... urls) {
            String response = "";

            for (String url : urls) {
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost httpPOST = new HttpPost(fullurl);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("UID", uid));
                    params.add(new BasicNameValuePair("DID", android_id));
                    params.add(new BasicNameValuePair("DeviceName", getDeviceName()));
                    params.add(new BasicNameValuePair("hasNFC", nfc_support));
                    params.add(new BasicNameValuePair("Latitude", String.valueOf(latitude)));
                    params.add(new BasicNameValuePair("Longitude", String.valueOf(longitude)));
                    params.add(new BasicNameValuePair("switch", url));
                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
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
            }
            return response;
        }
        protected void onPostExecute(String result) {
            if(result.equals("Door toggled")){
                LoadData task = new LoadData();
                task.execute();
            }
            if(result.equals("Light toggled")){
                ToggleButton b3 = (ToggleButton) findViewById(R.id.button3);
                b3.setEnabled(!b3.isEnabled());
                b3.setChecked(!b3.isChecked());
                textView.setText(result);
            }
            if(result.equals("Lock toggled")){
                ToggleButton b4 = (ToggleButton) findViewById(R.id.button4);
                b4.setEnabled(!b4.isEnabled());
                b4.setChecked(!b4.isChecked());
                textView.setText(result);
            }
            if(result.equals("Log in")){
                finish();
            }
            else{
                textView.setText(result);
            }
        }

    }
    public void show_users()
    {
        final Context context = this;
        Intent intent = new Intent(context, Admin_Users.class);
        intent.putExtra("uid", uid);
        if (admind.equals("true"))
            intent.putExtra("admind", "true");
        else{
                intent.putExtra("admind", "false");
        }
        startActivity(intent);
    }
    public void show_devices()
    {
        final Context context = this;
        Intent intent = new Intent(context, Admin_Devices.class);
        intent.putExtra("uid", uid);
        if (admind.equals("true"))
            intent.putExtra("admind", "true");
        else{
            intent.putExtra("admind", "false");
        }
        startActivity(intent);
    }
    public void show_log()
    {
        final Context context = this;
        Intent intent = new Intent(context, Admin_Log.class);
        intent.putExtra("uid", uid);
        if (admind.equals("true"))
            intent.putExtra("admind", "true");
        else{
            intent.putExtra("admind", "false");
        }
        startActivity(intent);
    }
}
