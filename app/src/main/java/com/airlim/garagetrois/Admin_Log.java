package com.airlim.garagetrois;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
//import java.util.HashMap;
import java.util.List;
//view log
public class Admin_Log extends Activity {
    private String jsonResult;
    volatile String uid = "0000";
    volatile String did = "";
    volatile String number = "";
    volatile String admind = "";
    volatile String devicename = "";
    volatile String geofence = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        admind = intent.getStringExtra("admind");
        did = intent.getStringExtra("did");
        devicename = intent.getStringExtra("devicename");
        number = intent.getStringExtra("number");

        //SparseArray<Group> groups = new SparseArray<Group>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);
        Button b3 = (Button) findViewById(R.id.button3);
        ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
        CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
        LinearLayout actionView = (LinearLayout) findViewById(R.id.actionView);
        actionView.setVisibility(View.GONE);
        c1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView checkTextView = (TextView) findViewById(R.id.checkTextView);
                CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
                if (c1.isChecked()){
                    checkTextView.setText("Remote Access will be denied.");
                }
                else{
                    checkTextView.setText("Remote Access will be allowed.");
                }
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) findViewById(R.id.textView);
                TextView toggleTextView = (TextView) findViewById(R.id.toggleTextView);
                CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
                TextView checkTextView = (TextView) findViewById(R.id.checkTextView);
                ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
                if (textView.getText().toString().length() > 5) {
                    if (b2.isChecked()) {
                        toggleTextView.setText("This device will be denied.");
                        checkTextView.setVisibility(View.GONE);
                        c1.setVisibility(View.GONE);
                        checkTextView.setText("Remote Access will be allowed.");
                        c1.setChecked(false);
                    } else {
                        toggleTextView.setText("This device will be allowed.");
                        checkTextView.setVisibility(View.VISIBLE);
                        checkTextView.setText("Remote Access will be allowed.");
                        c1.setChecked(false);
                        c1.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    if (b2.isChecked()) {
                        toggleTextView.setText("This user will be denied.");
                    } else {
                        toggleTextView.setText("This user will be allowed.");
                    }
                }
            }
        });

        b3.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) findViewById(R.id.textView);
                EditText editText = (EditText) findViewById(R.id.editText);
                ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
                CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
                String cnfc = (c1.isChecked()) ? "exclusive" : "nonexclusive";
                String adminaction = (b2.isChecked() ? b2.getTextOff().toString() : b2.getTextOn().toString());
                if (adminaction.length() > 5)
                    adminaction = adminaction.substring(0, adminaction.length()-1);
                //if edittext is invisible then we don't care what the value of it is.
                //if textview is empty then stop the button from being pressed.
                if (editText.getVisibility() == View.INVISIBLE){
                    //edittext is not shown, there has to be a DID to do anything from here.
                    if (!textView.getText().toString().contains(" ") && !textView.getText().toString().equals(""))
                    {
                        if (textView.getText().toString().length() > 5){
                            //there is a DID set in textview so this is ok.
                            String cname = "";
                            String cdid = textView.getText().toString();


                            //textView.setText(adminaction + "ing privileges for " + did + " (NFC " + nfc + ")");
                            AdminTask task = new AdminTask();
                            task.execute(cname, cnfc, cdid, adminaction);
                            c1.setChecked(false);

                            Toast.makeText(Admin_Log.this, adminaction + "ing privileges for " + cdid + " (NFC " + cnfc + ")",
                                    Toast.LENGTH_LONG).show();
                        }
                        else if(textView.getText().toString().length() < 1){
                            //this doesn't look like a DID...
                            Toast.makeText(Admin_Log.this, "Select an item from the list first.",
                                    Toast.LENGTH_LONG).show();
                        }
                        else{
                            //this doesn't look like a DID...
                            Toast.makeText(Admin_Log.this, "What the hell?",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(Admin_Log.this, "Select another item from the list.",
                                Toast.LENGTH_LONG).show();
                    }
                }
                else if (editText.getVisibility() == View.VISIBLE){
                    //edittext is visible. it has to have something in it to proceed.
                    if(editText.getText().toString().length() > 0 && textView.getText().toString().length() > 0 && textView.getText().toString().length() < 5){
                        //there is text in the edittext and it's not a DID, we should be ok.
                        String cname = editText.getText().toString();
                        String cdid = textView.getText().toString();

                        //textView.setText(adminaction + "ing privileges for " + name + " (" + did + ")");
                        AdminTask task = new AdminTask();
                        task.execute(cname, cnfc, cdid, adminaction);
                        Toast.makeText(Admin_Log.this, adminaction + "ing privileges for " + cname + " (" + cdid + ")" ,
                                Toast.LENGTH_LONG).show();
                    }
                    else if (textView.getText().toString().contains(" ")){
                        textView.setText("");
                        Toast.makeText(Admin_Log.this, "Select another item from the list.",
                                Toast.LENGTH_LONG).show();
                    }
                    else{
                        //there is no text in edittext, we should not proceed.
                         Toast.makeText(Admin_Log.this, "Enter a name to identify this user",
                             Toast.LENGTH_LONG).show();
                    }

                }
                Log.v("visible", String.valueOf(editText.getVisibility()));
                Log.v("edittextlength", String.valueOf(editText.getText().toString().length()));
                Log.v("textviewlength", String.valueOf(textView.getText().toString().length()));
            }
        });
        accessWebService();

    }

    // Async Task to access the web
    private class JsonReadTask extends AsyncTask<String, Void, String> {
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

                params.add(new BasicNameValuePair("Admin", "viewlog"));
                params.add(new BasicNameValuePair("UID", uid));
                params.add(new BasicNameValuePair("DID", did));
                params.add(new BasicNameValuePair("DeviceName", devicename));

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
            ListDrawer();
        }
    }// end async task

    public void accessWebService() {
        String server = Client_Functions.getPref("server_URL", getApplicationContext());
        String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
        String script = Client_Functions.getPref("script_name", getApplicationContext());
        String fullurl = "http://"+server+((path.equals(""))? script : "/"+path+"/"+script);
        JsonReadTask task = new JsonReadTask();

        // passes values for the urls string array
        task.execute(fullurl);
    }

    public void ListDrawer() {
        SparseArray<Group> groups = new SparseArray<>();
        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("log_info");

            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                //String name = jsonChildNode.optString("name");
                Group group = new Group(jsonChildNode.optString("date"));// + ":" + jsonChildNode.optString("action")
                group.children.add("Action: " + jsonChildNode.optString("action"));
                group.children.add("DID: " + jsonChildNode.optString("did"));
                if (jsonChildNode.optString("latitude") != null && !jsonChildNode.optString("latitude").isEmpty()) {
                    Log.v("latitude",jsonChildNode.optString("latitude"));
                    group.children.add("Geo: " + jsonChildNode.optString("latitude") + ',' + jsonChildNode.optString("longitude"));
                }
                group.children.add("User: " + jsonChildNode.optString("name") + "(" + jsonChildNode.optString("uid") + ")");
                group.children.add("Phone: " + jsonChildNode.optString("number"));
                groups.append(i, group);
            }
        } catch (JSONException e) {
            Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
            if(debug)
            Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);

        MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,
                groups);
        listView.setAdapter(adapter);

    }

    private class AdminTask extends AsyncTask<String, String, String> {

        TextView textView = (TextView) findViewById(R.id.textView);
        EditText editText = (EditText) findViewById(R.id.editText);
        LinearLayout actionView = (LinearLayout) findViewById(R.id.actionView);
        String server = Client_Functions.getPref("server_URL", getApplicationContext());
        String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
        String script = Client_Functions.getPref("script_name", getApplicationContext());
        String fullurl = "http://"+server+((path.equals(""))? script : "/"+path+"/"+script);
        
        protected String doInBackground(String... urls) {
            String response = "";

            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost httpPOST = new HttpPost(fullurl);
                List<NameValuePair> params = new ArrayList<>();
                if (urls[0].length() > 0 && urls[2].length() < 5){
                    params.add(new BasicNameValuePair("CName", urls[0]));
                }
                if (urls[1].contains("exclusive")){
                    params.add(new BasicNameValuePair("CNFC", urls[1]));
                }
                if (urls[2].length() > 5){
                    params.add(new BasicNameValuePair("CDID", urls[2]));
                }
                else{
                    params.add(new BasicNameValuePair("CUID", urls[2]));
                }

                params.add(new BasicNameValuePair("AdminAction", urls[3]));
                params.add(new BasicNameValuePair("UID", uid));
                params.add(new BasicNameValuePair("DID", did));
                params.add(new BasicNameValuePair("DeviceName", devicename));
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
            editText.setText("");
            textView.setText("");
            actionView.setVisibility(View.GONE);
            Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
            if(debug)
            Toast.makeText(Admin_Log.this, result,
                    Toast.LENGTH_LONG).show();
            accessWebService();
        }
    }

}
