/*
 * @created Mahdokht Afravi on 04-25 T
 * @modified 04-25 T
 */

package edu.utep.cs.cs4330.carpro;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText deviceNameField;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    android.content.IntentFilter intentFilter;
    java.util.Set<BluetoothDevice> pairedDevices;
    java.util.ArrayList<BluetoothDevice> availableDevices;
    SharedPreferences.Editor editor;
    private final String LOG_TAG = "testme";

    /* ************************************************************************************
       ************************************************************************************ */
    /*
     * The following section is reserved for Android operations: power, discovery, and connectivity
     */
    /* Creates the IGIVEUPONLIFEivity of CarPro: registers broadcast receiver for bluetooth */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        deviceNameField = (EditText) findViewById(R.id.deviceNameField);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
        editor = pref.edit();

        if(pref.contains("username")){
            deviceNameField.setText(pref.getString("username", null));
        }

        setUpBluetoothAdapter();
        availableDevices = new ArrayList<BluetoothDevice>();
    }

    /* Resumes the MainActivity of CarPro: registers broadcast receiver for bluetooth */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiverForBT(); //registers this receiver for bluetooth state changes
        Log.d(LOG_TAG, "resume method");

    }

    /* Pauses the MainActivity of CarPro: unregisters broadcast receiver for bluetooth */
    @Override
    protected void onPause() {
        super.onPause();
        bluetoothAdapter.cancelDiscovery();
        unregisterReceiver(receiver);
        Log.d(LOG_TAG, "puase method");
    }

    /* Destroys the MainActivity of CarPro: unregisters broadcast receiver for bluetooth */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothAdapter.cancelDiscovery();
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
            if ( bluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action) ) {
                Log.d(LOG_TAG,"Started discovery process...");
            } else if ( bluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) ) {
                Log.d(LOG_TAG,"Discovery process finished.");
                connectToAvailableDevice();
            } else if ( bluetoothAdapter.ACTION_STATE_CHANGED.equals(action) ) {
                Log.d(LOG_TAG,bluetoothAdapter.getState()+"");
            } else {
                Log.d(LOG_TAG,"Found a device...");
                BluetoothDevice d = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.d(LOG_TAG,"Device name in extra: " +d.getName());
                if ( d.ACTION_FOUND.equals(action) ) {
                    Log.d(LOG_TAG,"Found device " + d.getName());
                    if ( d!=null )
                        if ( !availableDevices.contains(d) )
                            availableDevices.add(d); //found a device
                        else
                            Log.d(LOG_TAG,"Found duplicate device.");
                } else if ( d.ACTION_ACL_CONNECTED.equals(action) ) {
                    Log.d(LOG_TAG, "connected");
                } else if ( d.ACTION_ACL_DISCONNECT_REQUESTED.equals(action) ) {
                    Log.d(LOG_TAG, "disconnecting");
                } else if ( d.ACTION_ACL_DISCONNECTED.equals(action) ) {
                    Log.d(LOG_TAG, "disconnected");
                }
            }
        }
    };

    /* Turns on Bluetooth and Connects to a device by registering the receiver for connectivity */
    private void registerReceiverForBT() {
        //BT functionality preparation
        Log.d(LOG_TAG,"Registering receiver on state changes...");
        intentFilter = new android.content.IntentFilter();
        intentFilter.addAction(bluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(bluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(bluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
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

    ArrayList<String> deviceStrs = new ArrayList<String>();

    /* Connects on User command */
    public void connect(android.view.View v) {
        Log.d(LOG_TAG,"connect pressed");
        if ( !hasBluetooth() )
            toast("No Bluetooth functionality.");
        else {
            listPairedDevices();
            if ( canConnectTo(deviceNameField.getText().toString()) ) {
                saveDeviceName(deviceNameField.getText().toString());
                connectToDevice(deviceNameField.getText().toString());
            }
        }
    }

    /* Start the next activity */
    private void startActivity(String addr) {
        Intent intent = new Intent(this, DisplayActivity.class);
        intent.putExtra("DeviceAddress",addr);
        startActivity(intent);
    }

    /* Checks if the remote device name is available and an OBDII */
    private boolean canConnectTo(String name) {
        //todo later add more devices
        return true;
    }

    /* Saves this device to connect to directly */
    private void saveDeviceName(String name) {
        editor.putString("username", deviceNameField.getText().toString());
        editor.commit();
    }

    /* Builds a connection to the remote device for receiving data */
    private void connectToDevice(String name) {
        Log.d(LOG_TAG,"in connectToDevice...");

        //getPerm();
        if ( !bluetoothAdapter.isEnabled() ) bluetoothAdapter.enable();
        if ( bluetoothAdapter.isDiscovering() ) bluetoothAdapter.cancelDiscovery();
        bluetoothAdapter.startDiscovery(); //start it
        while ( !bluetoothAdapter.isDiscovering() ) ; //Log.d(LOG_TAG,"not discovering...");
        //while ( bluetoothAdapter.isDiscovering() ) ; //Log.d(LOG_TAG,"now discovering...");
    }

    /* Gets user permission on Android 6.x+ */
    private void getPerm() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // Only ask for these permissions on runtime when running Android 6.0 or higher
            switch (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
                case PackageManager.PERMISSION_DENIED:
                    ((TextView) new AlertDialog.Builder(this)
                            .setTitle("Runtime Permissions up ahead")
                            .setMessage(Html.fromHtml("<p>To find nearby bluetooth devices please click \"Allow\" on the runtime permissions popup.</p>" +
                                    "<p>For more info see <a href=\"http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id\">here</a>.</p>"))
                            .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this, //Maybe??
                                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                                1);
                                    }
                                }
                            })
                            .show()
                            .findViewById(android.R.id.message))
                            .setMovementMethod(LinkMovementMethod.getInstance());       // Make the link clickable. Needs to be called after show(), in order to generate hyperlinks
                    break;
                case PackageManager.PERMISSION_GRANTED:
                    break;
            }
        }

    }

    /* Got a list of available devices for user to connect to */
    private void connectToAvailableDevice() {
        // show list of available devices
        getAvailableDevices();
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.select_dialog_singlechoice,
                deviceStrs);

        alertDialog.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                int position = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                //String deviceAddress = availableDevices.get(position).getAddress();
                startActivity(availableDevices.get(position).getAddress());
            }
        });
        alertDialog.setTitle("Choose Bluetooth device");
        alertDialog.show();
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

    /* Populates the adapter for the list of paired devices for connection */
    private void getAvailableDevices() {
        Log.d(LOG_TAG,"Size of availableDevices: " +availableDevices.size());
        for ( BluetoothDevice d : availableDevices ) {
            if ( d.getName()!=null )
                deviceStrs.add(d.getName());
            else
                deviceStrs.add(d.getAddress());
        }
    }
}
