package com.airlim.garagetrois;

/**
 * Created by jlang on 2/27/14.
 */
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
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
import org.apache.http.client.ClientProtocolException;
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
import java.util.HashMap;
import java.util.List;
//view devices
public class Admin_Devices extends Activity {
    private String jsonResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        SparseArray<Group> groups = new SparseArray<Group>();
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
                TextView toggleTextView = (TextView) findViewById(R.id.toggleTextView);
                ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
                if (b2.isChecked()){
                    toggleTextView.setText("This device will be denied.");
                }
                else{
                    toggleTextView.setText("This device will be allowed.");
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
                String nfc = (c1.isChecked()) ? "exclusive" : "nonexclusive";
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
                            String name = "";
                            String did = textView.getText().toString();


                            //textView.setText(adminaction + "ing privileges for " + did + " (NFC " + nfc + ")");
                            AdminTask task = new AdminTask();
                            task.execute(name, nfc, did, adminaction);
                            c1.setChecked(false);

                            Toast.makeText(Admin_Devices.this, adminaction + "ing privileges for " + did + " (NFC " + nfc + ")",
                                    Toast.LENGTH_LONG).show();
                        }
                        else if(textView.getText().toString().length() < 1){
                            //this doesn't look like a DID...
                            Toast.makeText(Admin_Devices.this, "Select an item from the list first.",
                                    Toast.LENGTH_LONG).show();
                        }
                        else{
                            //this doesn't look like a DID...
                            Toast.makeText(Admin_Devices.this, "What the actual fuck?",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(Admin_Devices.this, "Select another item from the list.",
                                Toast.LENGTH_LONG).show();
                    }
                }
                else if (editText.getVisibility() == View.VISIBLE){
                    //edittext is visible. it has to have something in it to proceed.
                    if(editText.getText().toString().length() > 0 && textView.getText().toString().length() > 0 && textView.getText().toString().length() < 5){
                        //there is text in the edittext and it's not a DID, we should be ok.
                        String name = editText.getText().toString();
                        String did = textView.getText().toString();

                        //textView.setText(adminaction + "ing privileges for " + name + " (" + did + ")");
                        AdminTask task = new AdminTask();
                        task.execute(name, nfc, did, adminaction);
                        Toast.makeText(Admin_Devices.this, adminaction + "ing privileges for " + name + " (" + did + ")" ,
                                Toast.LENGTH_LONG).show();
                    }
                    else if (textView.getText().toString().contains(" ")){
                        textView.setText("");
                        Toast.makeText(Admin_Devices.this, "Select another item from the list.",
                                Toast.LENGTH_LONG).show();
                    }
                    else{
                        //there is no text in edittext, we should not proceed.
                         Toast.makeText(Admin_Devices.this, "Enter a name to identify this user",
                             Toast.LENGTH_LONG).show();
                    }

                }
                Log.v("visible", String.valueOf(editText.getVisibility()));
                Log.v("edittextlength", String.valueOf(editText.getText().toString().length()));
                Log.v("textviewlength", String.valueOf(textView.getText().toString().length()));
            }
        });
        //createData();
        accessWebService();
        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);
        MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,
                groups);
        //listView.setAdapter(adapter);
        //listView = (ListView) findViewById(R.id.listView1);
        //accessWebService();
    }


    // Async Task to access the web
    private class JsonReadTask extends AsyncTask<String, Void, String> {

        String server = getResources().getString(R.string.server_URL);
        String path = getResources().getString(R.string.script_path);
        String script = getResources().getString(R.string.script_name);
        String fullurl = "http://"+server+((path != "")?"/"+path+"/"+script : script);
        @Override
        protected String doInBackground(String... urls) {
            //HttpClient httpclient = new DefaultHttpClient();

            //HttpPost httppost = new HttpPost(params[0]);
            try {
                /*
                HttpResponse response = httpclient.execute(httppost);
                */
                HttpClient client = new DefaultHttpClient();
                HttpPost httpPOST = new HttpPost(fullurl);
                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("Admin", "viewdevices"));
                UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
                httpPOST.setEntity(ent);
                HttpResponse response = client.execute(httpPOST);
                jsonResult = inputStreamToString(
                        response.getEntity().getContent()).toString();
            }

            catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            }

            catch (IOException e) {
                // e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Error..." + e.toString(), Toast.LENGTH_LONG).show();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            ListDrwaer();
        }
    }// end async task

    public void accessWebService() {
        String server = getResources().getString(R.string.server_URL);
        String path = getResources().getString(R.string.script_path);
        String script = getResources().getString(R.string.script_name);
        String fullurl = "http://"+server+((path != "")?"/"+path+"/"+script : script);
        JsonReadTask task = new JsonReadTask();
        // passes values for the urls string array
        task.execute(new String[] { fullurl });
    }

    // build hash set for list view
    public void ListDrwaer() {
        SparseArray<Group> groups = new SparseArray<Group>();
        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("device_info");

            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                //String name = jsonChildNode.optString("name");
                Group group = new Group(jsonChildNode.optString("alias"));// + ":" + jsonChildNode.optString("action")
                group.children.add("DID: " + jsonChildNode.optString("did"));
                group.children.add("Allowed: " + jsonChildNode.optString("allowed"));
                group.children.add("Has NFC: " + jsonChildNode.optString("has_nfc"));
                group.children.add("NFC allowed: " + jsonChildNode.optString("nfc"));
                group.children.add("Force NFC: " + jsonChildNode.optString("force_nfc"));
                group.children.add("Phone: " + jsonChildNode.optString("number"));
                group.children.add("Last updated: " + jsonChildNode.optString("date"));
                //String outPut = did + " : " + action;
                //employeeList.add(createEmployee("employees", outPut));
                //Log.v("group", groups.toString());
                groups.append(i, group);
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Error" + e.toString(),
                    Toast.LENGTH_SHORT).show();
        }

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.listView);

        MyExpandableListAdapter adapter = new MyExpandableListAdapter(this,
                groups);
        listView.setAdapter(adapter);

        /*SimpleAdapter simpleAdapter = new SimpleAdapter(this, employeeList,
                android.R.layout.simple_list_item_1,
                new String[] { "employees" }, new int[] { android.R.id.text1 });
        listView.setAdapter(simpleAdapter);
        */
    }

    private HashMap<String, String> createEmployee(String name, String number) {
        HashMap<String, String> employeeNameNo = new HashMap<String, String>();
        employeeNameNo.put(name, number);
        return employeeNameNo;
    }

    private class AdminTask extends AsyncTask<String, String, String> {
        String server = getResources().getString(R.string.server_URL);
        String path = getResources().getString(R.string.script_path);
        String script = getResources().getString(R.string.script_name);
        String fullurl = "http://"+server+((path != "")?"/"+path+"/"+script : script);
        TextView textView = (TextView) findViewById(R.id.textView);
        EditText editText = (EditText) findViewById(R.id.editText);
        protected String doInBackground(String... urls) {
            String response = "";
            String adminaction = "";

            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost httpPOST = new HttpPost(fullurl);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                if (urls[0].length() > 0 && urls[2].length() < 5){
                    params.add(new BasicNameValuePair("Name", urls[0]));
                }
                if (urls[1].contains("exclusive")){
                    params.add(new BasicNameValuePair("NFC", urls[1]));
                }
                if (urls[2].length() > 5){
                    params.add(new BasicNameValuePair("DID", urls[2]));
                }
                else{
                    params.add(new BasicNameValuePair("UID", urls[2]));
                }

                params.add(new BasicNameValuePair("AdminAction", urls[3]));
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
            textView.setText("");
            editText.setText("");
            Toast.makeText(Admin_Devices.this, result,
                    Toast.LENGTH_LONG).show();
            accessWebService();
        }
    }

}
