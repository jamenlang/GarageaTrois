package com.airlim.garagetrois;

/**
 * Created by jlang on 2/27/14.
 */

import android.app.Activity;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private final SparseArray<Group> groups;
    public LayoutInflater inflater;
    public Activity activity;
    public Integer allexpanded = 0;
    public MyExpandableListAdapter(Activity act, SparseArray<Group> groups) {
        activity = act;
        this.groups = groups;
        inflater = act.getLayoutInflater();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).children.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String children = (String) getChild(groupPosition, childPosition);
        final Group group = (Group) getGroup(groupPosition);
        final String username = group.string;
        final TextView toggleTextView = (TextView) activity.findViewById(R.id.toggleTextView);
        TextView text = null;
        final ToggleButton toggleButton = (ToggleButton) activity.findViewById(R.id.button2);
        final EditText editText = (EditText) activity.findViewById(R.id.editText);
        final TextView textView = (TextView) activity.findViewById(R.id.textView);
        final CheckBox checkBox = (CheckBox) activity.findViewById(R.id.checkBox);
        final LinearLayout actionView = (LinearLayout) activity.findViewById(R.id.actionView);
        final TextView checkTextView = (TextView) activity.findViewById(R.id.checkTextView);

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_details, null);
        }

        text = (TextView) convertView.findViewById(R.id.textView1);

        if(children.endsWith("Denied")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.denied, 0, 0, 0);
        }
        else if(children.contains("Denied (")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.denied, 0, 0, 0);
        }
        else if(children.endsWith("Granted")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.granted, 0, 0, 0);
        }
        else if(children.equals("Allowed: 1")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.granted, 0, 0, 0);
        }
        else if(children.equals("Allowed: 0")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.denied, 0, 0, 0);
        }
        else if(children.equals("Has NFC: true")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.nfc, 0, 0, 0);
        }
        else if(children.equals("Has NFC: false")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.denied, 0, 0, 0);
        }
        else if(children.contains("null")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.question, 0, 0, 0);
        }
        else if(children.equals("NFC allowed: 1")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.nfc, 0, 0, 0);
        }
        else if(children.equals("NFC allowed: 0")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.denied, 0, 0, 0);
        }
        else if(children.equals("Force NFC: 1")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.nfc, 0, 0, 0);
        }
        else if(children.equals("Force NFC: 0")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.world, 0, 0, 0);
        }
        else if(children.contains("Last updated: ")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.calendar, 0, 0, 0);
        }
        else if(children.startsWith("PIN: ")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.pin, 0, 0, 0);
        }
        else if(children.startsWith("User: (")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.question, 0, 0, 0);
        }
        else if(children.startsWith("User:")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.user, 0, 0, 0);
        }
        else if(children.endsWith("nfc0)")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.nfc, 0, 0, 0);
        }
        else if(children.startsWith("DID:")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.device, 0, 0, 0);
        }
        else if(children.equals("Action: light")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light, 0, 0, 0);
        }
        else if(children.equals("Action: door")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.door, 0, 0, 0);
        }
        else if(children.equals("Action: lock")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        }
        else if(children.startsWith("Phone: ")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.phone, 0, 0, 0);
        }
        else{
            text.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        text.setText(children);

        /*
        convertView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(activity, "Long Press at "+children, Toast.LENGTH_SHORT).show();
                //ChangeText
                Log.v("bname", "group long press");
                return false;
            }
        });
        */

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String result;
                Toast.makeText(activity, children,
                        Toast.LENGTH_SHORT).show();
                //editText.setText(children);
                if(children.startsWith("User: ")){
                    toggleButton.setChecked(false);
                    String regex ="[0-9]{4}";
                    Matcher matcher = Pattern.compile(regex).matcher(children);
                    if (matcher.find( ))
                    {
                        result = matcher.group();
                        String name_regex ="^User:\\s([A-Za-z0-9]+)\\(";
                        Matcher name_matcher = Pattern.compile(name_regex).matcher(children);
                        String name_result = null;
                        String aname_result = null;
                        String bname_result = null;
                        if (name_matcher.find( ))
                        {
                            name_result = name_matcher.group(1);
                            aname_result = name_result.replaceAll("^User:\\s", "");
                            Log.v("aname", aname_result);
                            bname_result = aname_result.replaceAll("(d{4})", "");
                            Log.v("bname", bname_result);
                            editText.setText(bname_result);
                            actionView.setVisibility(View.VISIBLE);
                        }
                        //Toast.makeText(activity, "Matches",1000 ).show();
                        System.out.println("number="+result);
                        textView.setText(result);
                        editText.setText(name_result);
                        checkBox.setVisibility(View.INVISIBLE);
                        toggleTextView.setText("This user will be allowed.");
                        editText.setVisibility(View.VISIBLE);
                        checkBox.setChecked(false);
                        checkTextView.setVisibility(View.INVISIBLE);
                        checkTextView.setVisibility(View.INVISIBLE);
                        actionView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        //Toast.makeText(activity, " No Matches",1000 ).show();
                        System.out.println("no match found");
                        textView.setText("");
                        checkBox.setVisibility(View.VISIBLE);
                        checkTextView.setVisibility(View.VISIBLE);
                        editText.setVisibility(View.INVISIBLE);
                        checkBox.setChecked(false);
                        checkBox.setChecked(false);
                        actionView.setVisibility(View.VISIBLE);
                    }
                }
                else if(children.startsWith("DID: ")){
                    toggleButton.setChecked(false);
                    String regex ="[a-z0-9]{10,}$";
                    Matcher matcher = Pattern.compile(regex).matcher(children);
                    if (matcher.find( ))
                    {
                        result = matcher.group();
                        //Toast.makeText(activity, "Matches",1000 ).show();
                        System.out.println("number="+result);
                        textView.setText(result);
                        editText.setVisibility(View.INVISIBLE);
                        checkBox.setChecked(false);
                        checkBox.setVisibility(View.VISIBLE);
                        toggleTextView.setText("This device will be allowed.");
                        checkTextView.setVisibility(View.VISIBLE);
                        checkTextView.setText("Remote access will be allowed.");
                        actionView.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        //Toast.makeText(activity, " No Matches",1000 ).show();
                        System.out.println("no match found");
                        textView.setText("");
                        editText.setVisibility(View.INVISIBLE);
                        checkBox.setChecked(false);
                        checkBox.setVisibility(View.VISIBLE);
                        checkTextView.setVisibility(View.VISIBLE);
                        actionView.setVisibility(View.GONE);

                    }
                }
                else if(children.startsWith("PIN: ")){
                    toggleButton.setChecked(false);
                    toggleTextView.setText("This user will be allowed");
                    String pin_regex ="^PIN:\\s([0-9]{4})$";
                    String pin_result = null;
                    Matcher pin_matcher = Pattern.compile(pin_regex).matcher(children);
                    if (pin_matcher.find( ))
                    {
                        pin_result = pin_matcher.group(1);
                        Log.v("pin", pin_result);
                    }
                    else{
                        Log.v("pinnotfound", pin_result);
                    }
                    //textView.setText("Select an action to perform or select another item");
                    editText.setText(pin_result);
                    textView.setText(username);
                    editText.setVisibility(View.VISIBLE);
                    checkTextView.setVisibility(View.INVISIBLE);
                    checkBox.setText("Edit Name");
                    checkBox.setChecked(false);
                    actionView.setVisibility(View.VISIBLE);
                }
            }
        });


        return convertView;

    }
    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }
    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).children.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        allexpanded --;
        Log.v("expanded decremented", String.valueOf(allexpanded));
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        allexpanded ++;
        Log.v("expanded incremented", String.valueOf(allexpanded));
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final EditText editText = (EditText) activity.findViewById(R.id.editText);
        final TextView toggleTextView = (TextView) activity.findViewById(R.id.toggleTextView);
        final TextView checkTextView = (TextView) activity.findViewById(R.id.checkTextView);
        final ToggleButton b2 = (ToggleButton) activity.findViewById(R.id.button2);
        final LinearLayout actionView = (LinearLayout) activity.findViewById(R.id.actionView);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listrow_group, null);
        }


        Group group = (Group) getGroup(groupPosition);
        ((CheckedTextView) convertView).setText(group.string);
        ((CheckedTextView) convertView).setChecked(isExpanded);
        if(allexpanded == 0){
            actionView.setVisibility(View.GONE);
        }
///////i'm trying to hide the adminview linear layout hererererererere
        if(group.children.equals("Action: ")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.bad, 0, 0, 0);
        }
        else if(group.children.contains("Action: Denied")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.bad, 0, 0, 0);
        }
        else if(group.children.contains("Action: Granted")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.good, 0, 0, 0);
        }
        else if(group.children.contains("null")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.question, 0, 0, 0);
        }
        else if(group.children.contains("Action: Denied (Device)")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.bad, 0, 0, 0);
        }
        else if(group.children.contains("Action: Denied (Device listed as NFC Only)")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.bad, 0, 0, 0);
        }
        else if(group.children.contains("Action: Denied ()")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.bad, 0, 0, 0);
        }
        else if(group.children.contains("Action: Admin Granted")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.good, 0, 0, 0);
        }
        else if(group.children.contains("Allowed: 1")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.good, 0, 0, 0);
        }
        else if(group.children.contains("Allowed: 0")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.bad, 0, 0, 0);
        }
        else if(group.children.contains("Action: door")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.door, 0, 0, 0);
        }
        else if(group.children.contains("Action: light")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.light, 0, 0, 0);
        }
        else if(group.children.contains("Action: lock")){
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(R.drawable.lock, 0, 0, 0);
        }
        else{
            ((CheckedTextView) convertView).setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        /*
        if(group.string.endsWith(":Denied")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.light, 0, 0, 0);
            group.string = group.string.replace(":Denied", "");
        }
        else if(group.string.endsWith(":Granted")){
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.calendar, 0, 0, 0);
            group.string = group.string.replace(":Granted", "");
        }
        else{
            text.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        */
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}