package com.airlim.garagetrois;

/**
 * Created by jlang on 2/27/14.
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Layout;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.NumberPicker;
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
//view users
public class Admin_Users extends Activity {
    private String jsonResult;
    private String server = getResources().getString(R.string.server_URL);
    private String path = getResources().getString(R.string.script_path);
    private String script = getResources().getString(R.string.script_name);
    private String fullurl = "http://"+server+((path != "")?"/"+path+"/"+script : script);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SparseArray<Group> groups = new SparseArray<Group>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin);
        ToggleButton b2 = (ToggleButton) findViewById(R.id.button2);
        Button b3 = (Button) findViewById(R.id.button3);
        CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
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
                    //edittext has the pin in it. the checkbox should be unchecked and say 'edit name' next to it.
                    if (buttonView.isChecked() == true) {

                        Toast.makeText(Admin_Users.this, "Modify Name where UID=" + temp1,
                                Toast.LENGTH_SHORT).show();
                        textView.setText(temp1);
                        editText.setText(temp2);
                        c1.setText("Edit PIN");

                    }
                }
                else
                {
                    if (buttonView.isChecked() == false)
                    {
                        Toast.makeText(Admin_Users.this, "Modify UID where Name=" + temp1,
                                Toast.LENGTH_SHORT).show();
                        textView.setText(temp1);
                        editText.setText(temp2);
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
                String change = (c1.isChecked()) ? "change_name" : "change_pin";
                String adminaction = (b2.isChecked() ? b2.getTextOff().toString() : b2.getTextOn().toString());
                if (adminaction.length() > 5)
                    adminaction = adminaction.substring(0, adminaction.length() - 1);
                //if edittext is visible then we care what the value of it is.
                //if textview is empty then stop the button from being pressed.
                if (editText.getVisibility() == View.VISIBLE) {
                    if (!textView.getText().toString().equals("")) {
                        Log.v("shit", change);
                        //edittext is shown, there has to be a PIN to do anything from here.
                        if (change == "change_pin"){
                            String pin = editText.getText().toString();
                            String name = textView.getText().toString();
                            Log.v("shit", "we're changing the pin");
                            //we're changing the pin
                            if (pin.length() == 4){
                                if (!name.contains(" ")) {
                                    if (!name.equals("")){
                                        //the pin is 4 chars, textview is not empty and it doesn't contain spaces
                                        AdminTask task = new AdminTask();
                                        task.execute(name, pin, change, adminaction);

                                        Toast.makeText(Admin_Users.this, adminaction + "ing privileges for " + name + " (" + pin + ")",
                                            Toast.LENGTH_LONG).show();
                                        if (editText.getText().length() == 4 && isNumeric(editText.getText().toString())){
                                            c1.setChecked(false);
                                            c1.setText("Edit Name");
                                            Log.v("shit", "edit name");
                                        }
                                        else{
                                            c1.setChecked(false);
                                            Log.v("shit", "edit PIN");
                                            c1.setText("Edit PIN");
                                        }
                                    } else {
                                        Log.v("shit", "textview is fancy. kill it");
                                        //text in textview is fancy. kill it.
                                        Toast.makeText(Admin_Users.this, "User names cannot cannot be blank.",
                                            Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    //text in textview is fancy. kill it.
                                    Log.v("shit", "textview is fancy. kill it");
                                    Toast.makeText(Admin_Users.this, "User names cannot contain spaces.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                //this doesn't look like a PIN...
                                Log.v("shit", "this doesn't look like a PIN");
                                Toast.makeText(Admin_Users.this, "PINs must be 4 digits",
                                        Toast.LENGTH_LONG).show();
                             }

                        } else if (change == "change_name"){
                            String name = editText.getText().toString();
                            String pin = textView.getText().toString();
                            Log.v("shit changename", "we're changing the name");
                            //we're changing the name
                            if (pin.length() == 4){
                                if (!name.contains(" ")) {
                                    if (!name.equals("")){
                                        //the pin is 4 chars, textview is not empty and it doesn't contain spaces
                                        AdminTask task = new AdminTask();
                                        task.execute(name, pin, change, adminaction);

                                        Toast.makeText(Admin_Users.this, adminaction + "ing privileges for " + name + " (" + pin + ")",
                                                Toast.LENGTH_LONG).show();
                                        if (editText.getText().length() == 4 && isNumeric(editText.getText().toString())){
                                            c1.setChecked(false);
                                            Log.v("shit changename", "edit name");
                                            c1.setText("Edit Name");
                                        }
                                        else{
                                            c1.setChecked(false);
                                            Log.v("shit changename", "edit pin");
                                            c1.setText("Edit PIN");
                                        }
                                    } else {
                                        //text in textview is fancy. kill it.
                                        Log.v("shit changename", "textview is fancy");
                                        Toast.makeText(Admin_Users.this, "User names cannot cannot be blank.",
                                                Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    //text in textview is fancy. kill it.
                                    Log.v("shit changename", "textview is fancy 2");
                                    Toast.makeText(Admin_Users.this, "User names cannot contain spaces.",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                //this doesn't look like a PIN...
                                Log.v("shit changename", "this doesn't look like a PIN");
                                Toast.makeText(Admin_Users.this, "PINs must be 4 digits",
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
    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }
    private class JsonReadTask extends AsyncTask<String, Void, String> {
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

                params.add(new BasicNameValuePair("Admin", "viewusers"));
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
        JsonReadTask task = new JsonReadTask();
        // passes values for the urls string array
        task.execute(new String[] { fullurl });
    }

    // build hash set for list view
    public void ListDrwaer() {
        SparseArray<Group> groups = new SparseArray<Group>();
        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("user_info");

            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
                //String name = jsonChildNode.optString("name");
                Group group = new Group(jsonChildNode.optString("name"));// + ":" + jsonChildNode.optString("action")
                group.children.add("Allowed: " + jsonChildNode.optString("allowed"));
                group.children.add("PIN: " + jsonChildNode.optString("uid"));
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
        TextView textView = (TextView) findViewById(R.id.textView);
        EditText editText = (EditText) findViewById(R.id.editText);
        CheckBox c1 = (CheckBox) findViewById(R.id.checkBox);
        LinearLayout actionView = (LinearLayout) findViewById(R.id.actionView);

        protected String doInBackground(String... urls) {
            String response = "";
            String adminaction = "";

            try {
                HttpClient client = new DefaultHttpClient();
                HttpPost httpPOST = new HttpPost(fullurl);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("Name", urls[0]));
                params.add(new BasicNameValuePair("UID", urls[1]));
                params.add(new BasicNameValuePair("Change", urls[2]));
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
            actionView.setVisibility(View.GONE);
            Toast.makeText(Admin_Users.this, result,
                    Toast.LENGTH_LONG).show();
            accessWebService();
        }
    }

}
