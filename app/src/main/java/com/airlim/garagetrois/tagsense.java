package com.airlim.garagetrois;


import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.widget.Toast;

public class tagsense extends MainActivity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
    NfcAdapter mNfcAdapter;
    private static final int MESSAGE_SENT = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this); // Check for available NFC Adapter
        if (mNfcAdapter == null)
        {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
        }
        else
        {
            mNfcAdapter.setNdefPushMessageCallback(this, this); // Register callback to set NDEF message
            mNfcAdapter.setOnNdefPushCompleteCallback(this, this); // Register callback to listen for message-sent success
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }


    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String mac = wm.getConnectionInfo().getMacAddress();
        String newMac = mac.substring(0, 2);
        mac = mac.substring(2);
        int hex = Integer.parseInt(newMac, 16) + 0x2;
        newMac = Integer.toHexString(hex);
        String text = newMac + mac;

        return new NdefMessage(NdefRecord.createMime(
                "application/com.airlim.garagetrois", text.getBytes()));
    }


    /**
     * Implementation for the OnNdefPushCompleteCallback interface
     */
    @Override
    public void onNdefPushComplete(NfcEvent arg0) {
        // A handler is needed to send messages to the activity when this
        // callback occurs, because it happens from a binder thread
        mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
    }


    /** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SENT:
                    Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    //TODO Currently overwrites any previously saved mac addresses.  Get FB ID as value.  Auto end activity.
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        //textView.setText(new String(msg.getRecords()[0].getPayload()));
        String payload = new String(msg.getRecords()[0].getPayload());
        Toast.makeText(this, new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();

        SharedPreferences appData = getSharedPreferences("appData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = appData.edit();
        String addresses = appData.getString("mac_address", null);
        if(addresses==null)
        {
            JSONArray addressArray = new JSONArray();
            addressArray.put(payload);
            addresses = addressArray.toString();
        }
        else
        {
            try {
                if(!addresses.contains(payload))
                {
                    JSONArray addressArray = new JSONArray(addresses);
                    addressArray.put(payload);
                    addresses = addressArray.toString();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Error adding new friend. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }
        editor.putString("mac_address", addresses);
        editor.apply();
    }
}