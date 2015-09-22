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
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TransformationMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

//view users
public class Admin_Users extends Activity {
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
        ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
        Button b3 = (Button) findViewById(R.id.button3);
        CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
        EditText editText = (EditText) findViewById(R.id.editText);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout actionView = (LinearLayout) findViewById(R.id.actionView);
        actionView.setVisibility(View.GONE);
        c1.setText("Edit Name");
        c1.setVisibility(View.VISIBLE);

        c1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            String temp1 = null;
            String temp2 = null;
            TextView textView = (TextView) findViewById(R.id.textView);
            EditText editText = (EditText) findViewById(R.id.editText);
            CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);

            public void onCheckedChanged(CompoundButton buttonView,
            boolean isChecked) {
                temp1 = editText.getText().toString();
                temp2 = textView.getText().toString();
                if(editText.getText().toString().length() == 4 && isNumeric(editText.getText().toString()))
                {
                    //edittext has the uid in it. the checkbox should be unchecked and say 'edit name' next to it.
                    if (buttonView.isChecked()) {

                        Toast.makeText(Admin_Users.this, "Modify Name where UID=" + temp1,
                                Toast.LENGTH_SHORT).show();
                        textView.setText(temp1);
                        editText.setText(temp2);
                        editText.setInputType(InputType.TYPE_CLASS_TEXT);
                        c1.setText("Edit UID");

                    }
                }
                else
                {
                    if (!buttonView.isChecked())
                    {
                        Toast.makeText(Admin_Users.this, "Modify UID where Name=" + temp1,
                                Toast.LENGTH_SHORT).show();
                        textView.setText(temp1);
                        editText.setText(temp2);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        c1.setText("Edit Name");
                        c1.setChecked(false);
                    }
                }
                // TODO Auto-generated method stub
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView toggleTextView = (TextView) findViewById(R.id.toggleTextView);
                ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
                if (b2.isChecked()){
                    toggleTextView.setText("This user will be denied.");
                }
                else{
                    toggleTextView.setText("This user will be allowed.");
                }
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView textView = (TextView) findViewById(R.id.textView);
                EditText editText = (EditText) findViewById(R.id.editText);
                ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
                CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
                String change = (c1.isChecked()) ? "change_name" : "change_uid";
                String adminaction = (b2.isChecked() ? b2.getTextOff().toString() : b2.getTextOn().toString());
                if (adminaction.length() > 5)
                    adminaction = adminaction.substring(0, adminaction.length() - 1);
                //if edittext is visible then we care what the value of it is.
                //if textview is empty then stop the button from being pressed.
                if (editText.getVisibility() == View.VISIBLE) {
                    if (!textView.getText().toString().equals("")) {
                        Log.v("alright", change);
                        //edittext is shown, there has to be a UID to do anything from here.
                        if (change.equals("change_uid")){
                            String cuid = editText.getText().toString();
                            String cname = textView.getText().toString();
                            Log.v("alright", "we're changing the uid");
                            //we're changing the uid
                            if (cuid.length() == 4){
                                if (!cname.contains(" ")) {
                                    if (!cname.equals("")){
                                        //the uid is 4 chars, textview is not empty and it doesn't contain spaces
                                        AdminTask task = new AdminTask();
                                        task.execute(cname, cuid, change, adminaction);

                                        Toast.makeText(Admin_Users.this, adminaction + "ing privileges for " + cname + " (" + cuid + ")",
                                            Toast.LENGTH_LONG).show();
                                        if (editText.getText().length() == 4 && isNumeric(editText.getText().toString())){
                                            c1.setChecked(false);
                                            c1.setText("Edit Name");
                                            Log.v("alright", "edit name");
                                        }
                                        else{
                                            c1.setChecked(false);
                                            Log.v("alright", "edit UID");
                                            c1.setText("Edit UID");
                                        }
                                    } else {
                                        Log.v("notalright", "textview is fancy. kill it");
                                        //text in textview is fancy. kill it.
                                        Toast.makeText(Admin_Users.this, "User names cannot cannot be blank.",
                                            Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    //text in textview is fancy. kill it.
                                    Log.v("notalright", "textview is fancy. kill it");
                                    Toast.makeText(Admin_Users.this, "User names cannot contain spaces.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                //this doesn't look like a UID...
                                Log.v("notalright", "this doesn't look like a UID");
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                                Toast.makeText(Admin_Users.this, "UIDs must be 4 digits",
                                        Toast.LENGTH_LONG).show();
                             }

                        } else if (change.equals("change_name")){
                            String cname = editText.getText().toString();
                            String cuid = textView.getText().toString();
                            Log.v("alright", "we're changing the name");
                            //we're changing the name
                            if (cuid.length() == 4){
                                if (!cname.contains(" ")) {
                                    if (!cname.equals("")){
                                        //the uid is 4 chars, textview is not empty and it doesn't contain spaces
                                        AdminTask task = new AdminTask();
                                        task.execute(cname, cuid, change, adminaction);

                                        Toast.makeText(Admin_Users.this, adminaction + "ing privileges for " + cname + " (" + cuid + ")",
                                                Toast.LENGTH_LONG).show();
                                        if (editText.getText().length() == 4 && isNumeric(editText.getText().toString())){
                                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                                            c1.setChecked(false);
                                            Log.v("alright", "edit name");
                                            c1.setText("Edit Name");
                                        }
                                        else{
                                            c1.setChecked(false);
                                            Log.v("alright", "edit uid");
                                            c1.setText("Edit UID");
                                        }
                                    } else {
                                        //text in textview is fancy. kill it.
                                        Log.v("notalright", "textview is fancy");
                                        Toast.makeText(Admin_Users.this, "User names cannot cannot be blank.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    //text in textview is fancy. kill it.
                                    Log.v("changename", "textview is fancy 2");
                                    Toast.makeText(Admin_Users.this, "User names cannot contain spaces.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                                //this doesn't look like a UID...
                                Log.v("changename", "this doesn't look like a UID");
                                Toast.makeText(Admin_Users.this, "UIDs must be 4 digits",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                Log.v("visible", String.valueOf(editText.getVisibility()));
                Log.v("change", change);
                Log.v("edittextlength", String.valueOf(editText.getText().toString().length()));
                Log.v("textviewlength", String.valueOf(textView.getText().toString().length()));
            }
        });
        accessWebService();
    }

    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }
    private class JsonReadTask extends AsyncTask<String, Void, String> {
        String server = Client_Functions.getPref("server_URL", getApplicationContext());
        String path = Client_Functions.cleanPath(Client_Functions.getPref("script_path", getApplicationContext()));
        String script = Client_Functions.getPref("script_name", getApplicationContext());
        String fullurl = "http://"+server+((path.equals(""))? script : "/"+path+"/"+script);
        @Override
        protected String doInBackground(String... urls) {

            try{
                HttpClient client = new DefaultHttpClient();
                HttpPost httpPOST = new HttpPost(fullurl);
                List<NameValuePair> params = new ArrayList<>();

                params.add(new BasicNameValuePair("Admin", "viewusers"));
                params.add(new BasicNameValuePair("UID", uid));
                params.add(new BasicNameValuePair("DID", did));
                params.add(new BasicNameValuePair("DeviceName", devicename));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPOST.setEntity(ent);
                HttpResponse response = client.execute(httpPOST);
                jsonResult = inputStreamToString(
                        response.getEntity().getContent()).toString();
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

        task.execute(fullurl);
    }

    public void ListDrawer() {
        SparseArray<Group> groups = new SparseArray<>();
        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("auth_info");

            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                Group group = new Group(jsonChildNode.optString("name"));
                group.children.add("Allowed: " + jsonChildNode.optString("allowed"));
                group.children.add("UID: " + jsonChildNode.optString("uid"));
                group.children.add("Last updated: " + jsonChildNode.optString("date"));
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
        //CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
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
                params.add(new BasicNameValuePair("CName", urls[0]));
                params.add(new BasicNameValuePair("CUID", urls[1]));
                params.add(new BasicNameValuePair("Change", urls[2]));
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
            textView.setText("");
            editText.setText("");
            actionView.setVisibility(View.GONE);
            Boolean debug = Client_Functions.getPrefBool("debug", getApplicationContext());
            if(debug)
            Toast.makeText(Admin_Users.this, result,
                    Toast.LENGTH_LONG).show();
            accessWebService();
        }
    }
}
