package com.airlim.garagetrois;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class QR extends Login {
    TextView textView;

    volatile String server = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        server = intent.getStringExtra("sever");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrscanned);
        textView = (TextView) findViewById(R.id.textView02);
        if(!server.equals("")){
            SaveServer task = new SaveServer();
            task.execute(server);
        }

    }

    public void onBackPressed() {
        finish();
    }

    private class SaveServer extends AsyncTask<String, String, String> {
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
            String FILENAME = "server_info.xml";
            String string = server;

            FileOutputStream fos = null;
            try {
                fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fos.write(string.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String response = "";

            HttpClient client = new DefaultHttpClient();
            HttpPost httpPOST = new HttpPost(server);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("initialtest", "true"));
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
            if(result.equals("Initial test: success")){
                alertbox("QR", result);
            }
            else{
                textView.setText("Something went wrong, try again.");
            }
        }

    }
    protected void alertbox(String title, String mymessage)
    {
        new AlertDialog.Builder(QR.this)
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