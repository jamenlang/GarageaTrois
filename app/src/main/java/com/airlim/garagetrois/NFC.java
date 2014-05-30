package com.airlim.garagetrois;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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
    private String server = getResources().getString(R.string.server_URL);
    private String path = getResources().getString(R.string.script_path);
    private String script = getResources().getString(R.string.script_name);
    private String fullurl = "http://"+server+((path != "")?"/"+path+"/"+script : script);

    TextView textView;
    volatile String uid = "nfc0";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc);
        ControlTask task = new ControlTask();
        task.execute(new String[]{"door"});

        textView = (TextView) findViewById(R.id.textView02);

    }
    @Override

    public void onBackPressed() {
        finish();
    }


    private class ControlTask extends AsyncTask<String, String, String> {
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
                List<NameValuePair> params = new ArrayList<NameValuePair>();
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
            if(result.equals("Door toggled")){
                finish();
            }
            else if(result.equals("Log in")){
                finish();
            }
            else{
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
