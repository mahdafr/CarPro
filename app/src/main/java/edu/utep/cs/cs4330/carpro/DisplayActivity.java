package edu.utep.cs.cs4330.carpro;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.control.PendingTroubleCodesCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.engine.RuntimeCommand;

public class DisplayActivity extends AppCompatActivity {
    private final String LOG_TAG = "woof";
    private ListView mListView;
    private ConnectThread connection;
    private ReadingItemAdapter adapter;
    private ReadingItem RPMitem;
    private ReadingItem speedItem;
    private ReadingItem codeItem;
    private ReadingItem timeItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        mListView = (ListView) findViewById(R.id.recipe_list_view);
        ArrayList<ReadingItem> displayList = new ArrayList<ReadingItem>();
        String titleItem1 = "Engine RPM";
        String subtitleItem1 = "Engine Revolutions Per Minute";
        String detailItem1 = "RPM: ";
        RPMitem = new ReadingItem(titleItem1, subtitleItem1, detailItem1);
        displayList.add(RPMitem);

        String titleItem2 = "Speed";
        String subtitleItem2 = "Kilometers Per Hour";
        String detailItem2 = "km/h: ";
        speedItem = new ReadingItem(titleItem2, subtitleItem2, detailItem2);
        displayList.add(speedItem);

        String titleItem3 = "Error Code";
        String subtitleItem3 = "Possible Error Code:";
        String detailItem3 = "";
        codeItem = new ReadingItem(titleItem3, subtitleItem3, detailItem3);
        displayList.add(codeItem);

        String titleItem4 = "Engine runtime";
        String subtitleItem4 = "How long engine has been running";
        String detailItem4 = "";
        timeItem = new ReadingItem(titleItem4, subtitleItem4, detailItem4);
        displayList.add(timeItem);
        adapter = new ReadingItemAdapter(this, displayList);

        mListView.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectionInitialization();
            }
        }).start();
        Log.d(LOG_TAG, "did thread");
    }

    private void connectionInitialization() {
        Log.d(LOG_TAG, "this is crazy");
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice d = btAdapter.getRemoteDevice(getIntent().getStringExtra("DeviceAddress"));
        connection = new ConnectThread(d);
        connection.run();
    }

    /* Close the connection on destroy */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        connection.cancel();
    }


    /* ************************************************************************************
       ************************************************************************************ */
    /*
     * The following section is the private class for maintaing the bluetooth connection.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private final InputStream input;
        private final OutputStream output;

        @TargetApi(19)
        public ConnectThread(BluetoothDevice d) {
            BluetoothSocket tmp = null;
            device = d;

            // Get the BluetoothSocket upon pairing state achieved.
            try {
                if ( d.getBondState()!=BluetoothDevice.BOND_BONDED ) {
                    d.createBond();
                    while ( d.getBondState()<BluetoothDevice.BOND_BONDED ) ;
                }
                if ( d.getUuids()[0].getUuid()!=null )
                    tmp = device.createInsecureRfcommSocketToServiceRecord(d.getUuids()[0].getUuid());
                Log.d(LOG_TAG,"Connecting Socket to: " +d.getName());
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
            Log.d(LOG_TAG,"Socket created");

            // Get the input and output streams.
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when creating input stream", e);
            }
            input = tmpIn;
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error occurred when creating output stream", e);
            }
            output = tmpOut;
        }

        /* Starts the connection to OBDII and gives configuration commands */
        public void run() {
            try {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect();
                Log.d(LOG_TAG,"Socket connected to: " +socket.getRemoteDevice().getName());

                new ObdResetCommand().run(input,output);
                new SelectProtocolCommand(ObdProtocols.AUTO).run(input, output);
                new EchoOffCommand().run(input, output);
                new LineFeedOffCommand().run(input, output);
                new TimeoutCommand(225).run(input, output);
//
                RPMCommand engineRpmCommand = new RPMCommand();

                SpeedCommand speedCommand = new SpeedCommand();
                PendingTroubleCodesCommand codeCommand = new PendingTroubleCodesCommand();
                RuntimeCommand timeCommand = new RuntimeCommand();

                while (!Thread.currentThread().isInterrupted()) {
                    try{
                        engineRpmCommand.run(input, output);
                        speedCommand.run(input, output);
                        codeCommand.run(input, output);
                        timeCommand.run(input, output);

                        RPMitem.setDetails(engineRpmCommand.getFormattedResult());
                        speedItem.setDetails(speedCommand.getFormattedResult());
                        codeItem.setDetails(codeCommand.getFormattedResult());
                        timeItem.setDetails(timeCommand.getFormattedResult());

                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                adapter.notifyDataSetChanged();

                            }
                        });
                        // TODO handle commands result
                        Log.d(LOG_TAG, "RPM: " + engineRpmCommand.getFormattedResult());
//                    Log.d(LOG_TAG, "Speed: " + speedCommand.getFormattedResult());

                    }
                    catch (com.github.pires.obd.exceptions.UnknownErrorException e){
                        Log.d(LOG_TAG, "ignoring unknown error");
                    }


                }
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(LOG_TAG, "Could not close the client socket", closeException);
                }
                return;
            } catch ( java.lang.InterruptedException e ) { }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            //manageMyConnectedSocket(socket); //todo
        }

        /* Closes the client socket and causes the thread to finish */
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Could not close the client socket", e);
            }
        }
    }
}
