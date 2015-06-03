package com.airlim.garagetrois;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
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


public class NFC extends Activity {
    TextView textView;
    volatile String party3 = "";
    volatile String uid = "nfc0";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        party3 = intent.getStringExtra("party3");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc);
        ControlTask task = new ControlTask();
        task.execute("Door");

        textView = (TextView) findViewById(R.id.textView02);

    }
    @Override

    public void onBackPressed() {
        finish();
    }


    private class ControlTask extends AsyncTask<String, String, String> {

        String server = Client_Functions.getPref("server_URL", getApplicationContext());
        String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
        String script = Client_Functions.getPref("script_name", getApplicationContext());
        String fullurl = "http://"+server+((path.equals(""))? script : "/"+path+"/"+script);

        volatile String android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        TelephonyManager telMgr = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
        String number = telMgr.getLine1Number();
        public String getDeviceName() {
            String manufacturer = Build.MANUFACTURER;
            String model = Build.MODEL;
            if (model.startsWith(manufacturer)) {
                return model;
            } else {
                return manufacturer + " " + model;
            }
        }
        protected String doInBackground(String... urls) {
            String response = "";

                HttpClient client = new DefaultHttpClient();
                HttpPost httpPOST = new HttpPost(fullurl);
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("UID", uid));
                params.add(new BasicNameValuePair("DID", android_id));
                params.add(new BasicNameValuePair("DeviceName", getDeviceName()));
                params.add(new BasicNameValuePair("TelNum", number));
                params.add(new BasicNameValuePair("switch", urls[0]));
                try {
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
            switch (result){
                case "Door toggled":
                    finish();
                    break;
                case "Log in":
                    finish();
                    break;
                default:
                    alertbox("NFC",result);
            }
        }
        protected void alertbox(String title, String mymessage)
        {
            new AlertDialog.Builder(NFC.this)
                    .setMessage(mymessage)
                    .setTitle(title)
                    .setCancelable(true)
                    .setNeutralButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton){}
                            })
                    .show();
        }
    }
}
