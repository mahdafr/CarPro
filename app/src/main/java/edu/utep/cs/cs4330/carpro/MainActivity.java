/*
 * @created Mahdokht Afravi on 04-25 T
 * @modified 05-02 T
 */

package edu.utep.cs.cs4330.carpro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private EditText deviceNameField;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    android.content.IntentFilter intentFilter;
    java.util.Set<BluetoothDevice> pairedDevices;
    private final String LOG_TAG = "testme";

    /* ************************************************************************************
       ************************************************************************************ */
    /*
     * The following section is reserved for Android operations: power, discovery, and connectivity
     */
    /* Creates the MainActivity of CarPro: registers broadcast receiver for bluetooth */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceNameField = (EditText) findViewById(R.id.deviceNameField);
        setUpBluetoothAdapter();
    }

    /* Resumes the MainActivity of CarPro: registers broadcast receiver for bluetooth */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiverForBT(); //registers this receiver for bluetooth state changes
    }

    /* Pauses the MainActivity of CarPro: unregisters broadcast receiver for bluetooth */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /* Destroys the MainActivity of CarPro: unregisters broadcast receiver for bluetooth */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }


    /* ************************************************************************************
       ************************************************************************************ */
    /*
     * The following section is reserved for Bluetooth Connectivity
     * This includes: BroadcastReceiver, pairedDevices, etc.
     */
    /* The BroadcastReceiver that listens for bluetooth connection changes */
    private final android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.d(LOG_TAG,"Device name in extra: " +d.getName());
            if ( d.ACTION_FOUND.equals(action) ) {
                Log.d(LOG_TAG,"Found device " + d.getName());
            } else if ( d.ACTION_ACL_CONNECTED.equals(action) ) {
                Log.d(LOG_TAG, "connected");
            } else if ( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) ) {
                Log.d(LOG_TAG, "discovery complete");
            } else if ( d.ACTION_ACL_DISCONNECT_REQUESTED.equals(action) ) {
                Log.d(LOG_TAG, "disconnecting");
            } else if ( d.ACTION_ACL_DISCONNECTED.equals(action) ) {
                Log.d(LOG_TAG, "disconnected");
            }
        }
    };

    /* Turns on Bluetooth and Connects to a device by registering the receiver for connectivity */
    private void registerReceiverForBT() {
        //BT functionality preparation
        Log.d(LOG_TAG,"Registering receiver on d/c, c, and d/c req...");
        intentFilter = new android.content.IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(receiver, intentFilter);
    }


    /* ************************************************************************************
       ************************************************************************************ */
    /*
     * The following section is reserved for User Interactivity with the Application.
     * This includes: Toasts, Dialogs, and button clicks
     */
    /* Creates a dialog for user confirmation */
    private void createTryAgainDialog(String msg, String positive, String negative) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setPositiveButton(positive,
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface arg0, int arg1) {
                        //todo idk
                    }
                });
        alertDialogBuilder.setNegativeButton(negative,
                new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(android.content.DialogInterface arg0, int arg1) {
                        //todo idfk
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /* Show a toast message */
    protected void toast(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }

    /* Connects on User command */
    public void connect(android.view.View v) {
        Log.d(LOG_TAG,"connect pressed");
        if ( !hasBluetooth() )
            toast("No Bluetooth functionality.");
        else {
            listPairedDevices();
            String remoteDevice = deviceNameField.getText().toString();
            if ( canConnectTo(remoteDevice) ) {
                saveDeviceName(remoteDevice);
                connectToDevice(remoteDevice);
            }
        }
    }

    /* Checks if the remote device name is available and an OBDII */
    private boolean canConnectTo(String name) {
        //todo later add more devices
        return true;
    }

    /* Saves this device to connect to directly */
    private void saveDeviceName(String name) {
        //todo save to app/os
    }

    /* Builds a connection to the remote device for receiving data */
    private void connectToDevice(String name) {
        //todo build connection
    }


    /* ************************************************************************************
       ************************************************************************************ */
    /*
     * The following section is reserved for helper methods on the Bluetooth Functionality.
     */
    /* Acquires Bluetooth Adapter for the local device */
    private void setUpBluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /* Checks if the local device has bluetooth functionality */
    private boolean hasBluetooth() {
        return bluetoothAdapter!=null;
    }

    /* Displays the list of paired devices to the local device */
    private void listPairedDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        for ( BluetoothDevice d : pairedDevices )
            Log.d(LOG_TAG,"Bonded with: " +d.getName());
    }
}
