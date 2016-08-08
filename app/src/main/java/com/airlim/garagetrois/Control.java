package com.airlim.garagetrois;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
//import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
//import android.view.LayoutInflater;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
//import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class Control extends Login {
    TextView textView;
    private String jsonResult;
    volatile String reverse = "false";
    volatile String uid = "0000";
    volatile String did = "0000";
    volatile String devicename = "";
    volatile String number = "";
    volatile String nfc = "";
    GPSTracker gps;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        did = intent.getStringExtra("did");
        devicename = intent.getStringExtra("devicename");
        number = intent.getStringExtra("number");
        nfc = intent.getStringExtra("nfc");
        admind = intent.getStringExtra("admind");
        authd = intent.getStringExtra("authd");
        geofence = intent.getStringExtra("geofence");
        Log.v("geofence", geofence);

        super.onCreate(savedInstanceState);
        Log.v("oncreatecontrol", uid);
        setContentView(R.layout.control);
        textView = (TextView) findViewById(R.id.textView02);

        gps = new GPSTracker(Control.this);
        if(gps.canGetLocation()) {
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            if(geofence.equals("true")){
                Log.v("geofence true", uid);
                textView.setText("Geofence Enabled: GPS Ready.");
                if(String.valueOf(latitude).equals("0.0")){
                    textView.setText("Geofence Enabled: GPS Empty.");
                }
            }

            Log.v("geofence",geofence);
            // \n is for new line
            Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
            if(debug)
                Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // Can't get location.
            // GPS or network is not enabled.
            // Ask user to enable GPS/network in settings.
            if(geofence.equals("true")){
                Log.v("geofence true", uid);
                textView.setText("Geofence Enabled: GPS Not Available.");
            }
            gps.showSettingsAlert();
            finish();
        }
        //this populates the buttons.
        JsonReadTask task = new JsonReadTask();
        task.execute();
    }

    public void onBackPressed() {
        finish();
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

    public void runTimer(final int id){
        final ToggleButton triggerBtn = (ToggleButton) findViewById(id);

        //---new task----
        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>(){

            TextView textView = (TextView) findViewById(R.id.textView02);
            ProgressBar myProgress = (ProgressBar) findViewById(R.id.progressBar);
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                myProgress.setMax(100);
                myProgress.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                //----update result---
                //textView.setText("Finished");

                //----re-enable button---
                triggerBtn.setEnabled(true);
                if(reverse.equals("false"))
                    reverse = "true";
                else
                    reverse = "false";
            }

            @Override
            protected Void doInBackground(Void... params) {
                Integer sleepytime = Client_Functions.getPrefInt("sleepytime_" + id, getApplicationContext());
                int var = (reverse.equals("false")) ? 0 : 100;
                int goal = (reverse.equals("false")) ? 100 : 0;
                //-----do time consuming stuff here ---
                while(var != goal)
                {
                    try {
                        final String dsleepytime_id = "dsleepytime_" + id;
                        String dsleepytime = Client_Functions.getPref(dsleepytime_id, getApplicationContext());
                        if(dsleepytime.equals("after")){
                            Thread.sleep(sleepytime);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (reverse.equals("false"))
                        var++;
                    else
                        var--;
                    final int finalVar = var;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myProgress.setProgress(finalVar);
                            //textView.setText("Waiting...");
                        }
                    });
                }
                return null;
            }
        };
        //----run task----
        task.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (admind.equals("true")){
            Log.d(admind, "admind");
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        else
            return false;
    }
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem item1 = menu.findItem(R.id.action_log);
        item1.setVisible(true);
        MenuItem item2 = menu.findItem(R.id.action_users);
        item2.setVisible(true);
        MenuItem item3 = menu.findItem(R.id.action_devices);
        item3.setVisible(true);

        return true;
    }
    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
        super.onValueChange(picker, oldVal, newVal);
    }

    public class BlindProgress extends AsyncTask<Void, Void, Void> {
        ProgressBar myProgress = (ProgressBar) findViewById(R.id.progressBar);
        @Override
        protected void onPreExecute() {
            myProgress.setMax(100);
            myProgress.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(Void... params) {
            int var =  0;
            int goal = 100;
            while (var != goal) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                var++;

                final int finalVar = var;
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        myProgress.setProgress(finalVar);
                    }
                });
            }

            return null;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
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
        case R.id.action_settings:
            show_settings();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    public void runControl(final String app_will_request, final int id) {
        final ToggleButton triggerBtn = (ToggleButton) findViewById(id);
        triggerBtn.setEnabled(false);

        BlindProgress rtask = new BlindProgress();
        rtask.execute();

        //---new task----
        AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            String server = Client_Functions.getPref("server_URL", getApplicationContext());
            String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
            String script = Client_Functions.getPref("script_name", getApplicationContext());
            String fullurl = "http://" + server + ((path.equals("")) ? script : "/" + path + "/" + script);

            protected String doInBackground(String... urls) {
                String response = "";

                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost httpPOST = new HttpPost(fullurl);
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("UID", uid));
                    params.add(new BasicNameValuePair("DID", getAndroid_id()));
                    params.add(new BasicNameValuePair("DeviceName", getDeviceName()));
                    params.add(new BasicNameValuePair("hasNFC", getNfc_support()));
                    params.add(new BasicNameValuePair("Latitude", String.valueOf(latitude)));
                    params.add(new BasicNameValuePair("Longitude", String.valueOf(longitude)));
                    params.add(new BasicNameValuePair("TelNum", number));
                    params.add(new BasicNameValuePair("switch", app_will_request));
                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
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
                if (result == "Log in") {
                    finish();
                }
                String[] callback_info = result.split(",");
                final int id = Integer.parseInt(callback_info[0]);
                Log.v("v",result);
                final int state = Integer.parseInt(callback_info[1]);
                ToggleButton b = (ToggleButton) findViewById(id);
                //b.setEnabled(!b.isEnabled());
                final String sleepytime_id = "sleepytime_" + callback_info[0];
                final String dsleepytime_id = "dsleepytime_" + callback_info[0];
                int sleepytime = Client_Functions.getPrefInt(sleepytime_id, getApplicationContext());
                String dsleepytime = Client_Functions.getPref(dsleepytime_id, getApplicationContext());

                switch (state) {
                    //2 is toggled
                    case 2:
                        b.setChecked(!b.isChecked());
                        break;
                    //0 is off
                    case 0:
                        b.setChecked(false);
                        break;
                    //1 is on
                    case 1:
                        b.setChecked(true);
                        break;
                    default:
                        break;
                }

                if(dsleepytime.equals("after")){
                    if (sleepytime > 0) {
                        runTimer(id);
                    }
                    else{
                        b.setEnabled(true);
                    }
                }
                else{
                    b.setEnabled(true);
                }

                textView.setText(callback_info[2]);
                JsonReadTask task = new JsonReadTask();
                task.execute();
            }
        };
        //----run task----

        task.execute();
    }

    public void webControl(final String thread, final String action, final int id) {
        final WebView web = (WebView) findViewById(id);
        final ProgressBar myProgress = (ProgressBar) findViewById(R.id.progressBar);
        web.setEnabled(false);
        //web.setOnTouchListener(null);
        BlindProgress rtask = new BlindProgress();
        rtask.execute();

        //---new task----
        AsyncTask<String, String, String> task = new AsyncTask<String, String, String>() {

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            String server = Client_Functions.getPref("server_URL", getApplicationContext());
            String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
            String script = Client_Functions.getPref("script_name", getApplicationContext());
            String fullurl = "http://" + server + ((path.equals("")) ? script : "/" + path + "/" + script);

            protected String doInBackground(String... urls) {
                String response = "";

                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpPost httpPOST = new HttpPost(fullurl);
                    List<NameValuePair> params = new ArrayList<>();
                    params.add(new BasicNameValuePair("UID", uid));
                    params.add(new BasicNameValuePair("DID", getAndroid_id()));
                    params.add(new BasicNameValuePair("DeviceName", getDeviceName()));
                    params.add(new BasicNameValuePair("hasNFC", getNfc_support()));
                    params.add(new BasicNameValuePair("Latitude", String.valueOf(latitude)));
                    params.add(new BasicNameValuePair("Longitude", String.valueOf(longitude)));
                    params.add(new BasicNameValuePair("TelNum", number));
                    params.add(new BasicNameValuePair("switch", "motion"));
                    params.add(new BasicNameValuePair("motion_thread", thread));
                    params.add(new BasicNameValuePair("motion_action", action));
                    UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
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
                if (result == "Log in") {
                    finish();
                }
                JsonReadTask task = new JsonReadTask();
                web.setEnabled(true);
                task.execute();
                textView.setText(result);
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        myProgress.setProgress(0);
                    }
                });
            }
        };
        //----run task----

        task.execute();
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        String server = Client_Functions.getPref("server_URL", getApplicationContext());
        String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
        String script = Client_Functions.getPref("script_name", getApplicationContext());
        String fullurl = "http://" + server + ((path.equals("")) ? script : "/" + path + "/" + script);

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            Bitmap mIcon11 = null;
            String imagefile = urls[0];
            try {
                HttpClient client = new DefaultHttpClient();

                HttpPost httpPOST = new HttpPost(fullurl);
                List<NameValuePair> params = new ArrayList<>();

                //params.add(new BasicNameValuePair("retrieve", "switch_info"));
                params.add(new BasicNameValuePair("UID", uid));
                params.add(new BasicNameValuePair("DID", getAndroid_id()));
                params.add(new BasicNameValuePair("DeviceName", getDeviceName()));
                params.add(new BasicNameValuePair("hasNFC", getNfc_support()));
                params.add(new BasicNameValuePair("TelNum", number));
                params.add(new BasicNameValuePair("switch", "retrieve_image"));
                params.add(new BasicNameValuePair("motion_image", imagefile));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPOST.setEntity(ent);

                HttpResponse response = client.execute(httpPOST);

                HttpEntity entity = response.getEntity();
                InputStream is = entity.getContent();

                mIcon11 = BitmapFactory.decodeStream(is);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }
    }

    private class JsonReadTask extends AsyncTask<String, Void, String> {

        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();
        String server = Client_Functions.getPref("server_URL", getApplicationContext());
        String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
        String script = Client_Functions.getPref("script_name", getApplicationContext());
        String fullurl = "http://"+server+((path.equals(""))? script : "/"+path+"/"+script);

        @Override
        protected String doInBackground(String... urls) {

            try {
                HttpClient client = new DefaultHttpClient();

                HttpPost httpPOST = new HttpPost(fullurl);
                List<NameValuePair> params = new ArrayList<>();

                //params.add(new BasicNameValuePair("retrieve", "switch_info"));
                params.add(new BasicNameValuePair("UID", uid));
                params.add(new BasicNameValuePair("DID", getAndroid_id()));
                params.add(new BasicNameValuePair("DeviceName", getDeviceName()));
                params.add(new BasicNameValuePair("hasNFC", getNfc_support()));
                params.add(new BasicNameValuePair("Latitude", String.valueOf(latitude)));
                params.add(new BasicNameValuePair("Longitude", String.valueOf(longitude)));
                params.add(new BasicNameValuePair("TelNum", number));
                params.add(new BasicNameValuePair("switch", "retrieve_switches"));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPOST.setEntity(ent);
                HttpResponse response = client.execute(httpPOST);
                jsonResult = inputStreamToString(response.getEntity().getContent()).toString();

            }

            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine;
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            }

            catch (IOException e) {
                // e.printStackTrace();
                Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
                if(debug)
                    Toast.makeText(getApplicationContext(),
                            "Error..." + e.toString(), Toast.LENGTH_LONG).show();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            CreateButtons();
        }
    }

    public void CreateButtons() {

        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("switch_info");
            LinearLayout ll = (LinearLayout) findViewById(R.id.LinearLayout1);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.weight = 1.0f;
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.setMargins(20, 20, 20, 0);

            LinearLayout.LayoutParams webparams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            webparams.weight = 1.0f;
            webparams.gravity = Gravity.CENTER_HORIZONTAL;
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;

            int dimensMargin = 160;
            float densityMargin = getResources().getDisplayMetrics().density;
            int finalDimensMargin = (int)(dimensMargin * densityMargin);
            webparams.setMargins((width/2-finalDimensMargin), 0, (width/2-finalDimensMargin), 0);

            for (int i = 0; i < jsonMainNode.length(); i++) {

                final JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                final String name = jsonChildNode.optString("name");
                final Boolean reports_current_status = jsonChildNode.optBoolean("reports_current_status", false);
                final Boolean current_status = jsonChildNode.optBoolean("current_status", false);
                final String app_will_request = jsonChildNode.optString("app_will_request");
                final String motion_thread = jsonChildNode.optString("motion_thread");

                if (jsonChildNode.optString("timeout") != "") {
                    final String sleepytime = jsonChildNode.optString("timeout");
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    prefs.putString("sleepytime_" + i, sleepytime);
                    prefs.commit();
                } else {
                    final String sleepytime = "0";
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    prefs.putString("sleepytime_" + i, sleepytime);
                    prefs.commit();
                }

                if (jsonChildNode.optString("display_progress") != "") {
                    final String dsleepytime = jsonChildNode.optString("display_progress");
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    prefs.putString("dsleepytime_" + i, dsleepytime);
                    prefs.commit();
                } else {
                    final String dsleepytime = "0";
                    SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    prefs.putString("dsleepytime_" + i, dsleepytime);
                    prefs.commit();
                }
                //ll.removeView(findViewById(i));
                if (findViewById(i) == null) {
                    Log.v("button", String.valueOf(i));
                    ToggleButton btn = new ToggleButton(this);
                    btn.setId(i);
                    final int id_ = btn.getId();
                    btn.setText(name);
                    btn.setTextOn(name);
                    btn.setTextOff(name);
                    btn.setChecked(current_status);
                    //btn.setLayoutParams();
                    ll.addView(btn, params);
                    btn = (ToggleButton) findViewById(id_);
                    btn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            textView.setText("Toggling " + name + "...");
                            ToggleButton tb = (ToggleButton) view;
                            tb.setEnabled(!tb.isEnabled());
                            tb.setChecked(!tb.isChecked());
                            runControl(app_will_request, id_);
                        }
                    });
                    /*
                    if(motion_image_file != ""){
                        ImageView img = new ImageView(this);
                        img.setId(12 + i);
                        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        img.setAdjustViewBounds(true);
                        final int iid_ = img.getId();
                        ll.addView(img);
                        new DownloadImageTask((ImageView) findViewById(iid_)).execute(motion_image_file);
                    }
                    */
                    if(motion_thread != ""){
                        String server = Client_Functions.getPref("server_URL", getApplicationContext());
                        String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
                        String script = Client_Functions.getPref("script_name", getApplicationContext());
                        String fullurl = "http://"+server+((path.equals(""))? script : "/"+path+"/"+script);

                        String postData = "UID="+uid+"&DID="+getAndroid_id()+"&DeviceName="+getDeviceName()+"&hasNFC="+getNfc_support()+"&motion_thread="+motion_thread+"&TelNum="+number+"&switch=motion&motion_action=retrieve_image";

                        WebView web = new WebView(this);
                        web.setId(12 + i);

                        LinearLayout secondlayout=(LinearLayout)this.getLayoutInflater().inflate(R.layout.webview,null);

                        //params.setMargins(300, 300, 0, 0);
                        //web.setLayoutParams(params);
                        //web.setLeft(100);

                        //web.getSettings().setUseWideViewPort(true);
                        web.postUrl(fullurl, EncodingUtils.getBytes(postData, "BASE64"));
                        //web.loadUrl(motion_image_url);1
                        final int iid_ = web.getId();
                        secondlayout.addView(web, webparams);
                        //btn.setLayoutParams();

                        ll.addView(secondlayout);
                        web = (WebView) findViewById(iid_);
                        web.setOnTouchListener(new View.OnTouchListener() {

                            public final static int FINGER_RELEASED = 0;
                            public final static int FINGER_TOUCHED = 1;
                            public final static int FINGER_DRAGGING = 2;
                            public final static int FINGER_UNDEFINED = 3;

                            private int fingerState = FINGER_RELEASED;

                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {

                                switch (motionEvent.getAction()) {

                                    case MotionEvent.ACTION_DOWN:
                                        if (fingerState == FINGER_RELEASED) fingerState = FINGER_TOUCHED;
                                        else fingerState = FINGER_UNDEFINED;
                                        break;

                                    case MotionEvent.ACTION_UP:
                                        webControl(motion_thread, "snapshot", iid_);
                                        //Toast.makeText(getBaseContext(), "Snapshot thread " + motion_thread, Toast.LENGTH_SHORT).show();
                                        if(fingerState != FINGER_DRAGGING) {
                                            fingerState = FINGER_RELEASED;
                                            // Your onClick codes
                                        }
                                        else if (fingerState == FINGER_DRAGGING) fingerState = FINGER_RELEASED;
                                        else fingerState = FINGER_UNDEFINED;
                                        break;

                                    case MotionEvent.ACTION_MOVE:
                                        if (fingerState == FINGER_TOUCHED || fingerState == FINGER_DRAGGING) fingerState = FINGER_DRAGGING;
                                        else fingerState = FINGER_UNDEFINED;
                                        break;

                                    default:
                                        fingerState = FINGER_UNDEFINED;

                                }

                                return false;
                            }
                        });

                        //web.setClickable(true);
                        //web.getSettings().setBuiltInZoomControls(false);
                        //web.setOnTouchListener(new View.OnTouchListener() {
                          //  public boolean onTouch(View v, MotionEvent event) {
                            //    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                              //      webControl(motion_thread, "snapshot", iid_);
                                //    Toast.makeText(getBaseContext(), "Snapshot thread " + motion_thread, Toast.LENGTH_SHORT).show();
                               //     return false;
                              //  }
                             //   return false;
                           // }
                        //});
                        web.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                webControl(motion_thread, "restart", iid_);
                                Toast.makeText(getBaseContext(), "Restart motion", Toast.LENGTH_SHORT).show();
                                return true;
                            }
                        });
                    }
                } else {
                    if(reports_current_status) {
                        ToggleButton b = (ToggleButton) findViewById(i);
                        b.setChecked(current_status);
                    }
                }
            }
        } catch (JSONException e) {
            if(jsonResult.contains("Geo")){
                Toast.makeText(getApplicationContext(), jsonResult,
                        Toast.LENGTH_LONG).show();
            }
            else {
                Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
                if (debug)
                    Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void show_users()
    {
        final Context context = this;
        Intent intent = new Intent(context, Admin_Users.class);
        intent.putExtra("uid", uid);
        intent.putExtra("geofence", geofence);
        intent.putExtra("nfc", nfc);
        intent.putExtra("devicename", devicename);
        intent.putExtra("did", did);
        intent.putExtra("number", number);
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
        intent.putExtra("geofence", geofence);
        intent.putExtra("nfc", nfc);
        intent.putExtra("devicename", devicename);
        intent.putExtra("did", did);
        intent.putExtra("number", number);
        if (admind.equals("true"))
            intent.putExtra("admind", "true");
        else{
            intent.putExtra("admind", "false");
        }
        startActivity(intent);
    }

    public void show_settings()
    {
        showgatsettings();
    }

    public void show_log()
    {
        final Context context = this;
        Intent intent = new Intent(context, Admin_Log.class);
        intent.putExtra("uid", uid);
        intent.putExtra("geofence", geofence);
        intent.putExtra("nfc", nfc);
        intent.putExtra("devicename", devicename);
        intent.putExtra("did", did);
        intent.putExtra("number", number);
        if (admind.equals("true"))
            intent.putExtra("admind", "true");
        else{
            intent.putExtra("admind", "false");
        }
        startActivity(intent);
    }
}
