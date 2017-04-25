/*
 * @created Mahdokht Afravi on 04-25 T
 * @modified 04-25 T
 */

package edu.utep.cs.cs4330.carpro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private boolean hasBluetooth;
    private BluetoothAdapter bluetoothAdapter;
    android.content.IntentFilter intentFilter;
    java.util.Set<BluetoothDevice> pairedDevices;

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

        setUpBluetoothAdapter();
        hasBluetooth = hasBluetooth();
    }

    /* Resumes the MainActivity of CarPro: registers broadcast receiver for bluetooth */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
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
     */
    /* The BroadcastReceiver that listens for bluetooth connection changes */
    private final android.content.BroadcastReceiver receiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if ( d.ACTION_FOUND.equals(action) ) {
                Log.d("MAINACT","Found device " + d.getName());
            } else if ( d.ACTION_ACL_CONNECTED.equals(action) ) {
                Log.d("MAINACT", "connected");
            } else if ( bluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) ) {
                Log.d("MAINACT", "discovery complete");
            } else if ( d.ACTION_ACL_DISCONNECT_REQUESTED.equals(action) ) {
                Log.d("MAINACT", "disconnecting");
            } else if ( d.ACTION_ACL_DISCONNECTED.equals(action) ) {
                Log.d("MAINACT", "disconnected");
            }
        }
    };

    /* Acquires Bluetooth Adapter */
    private void setUpBluetoothAdapter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /* Checks if the local device has bluetooth functionality */
    private boolean hasBluetooth() {
        return bluetoothAdapter!=null;
    }

    /* Turns on Bluetooth and Connects to a device by registering the receiver for connectivity */
    private void connectToBluetooth() {
        //BT functionality preparation
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
        if ( hasBluetooth )
            connectToBluetooth();
        else
            toast("No Bluetooth functionality.");
    }
}
