package edu.utep.cs.cs4330.carpro;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.ObdResetCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.protocol.EchoOffCommand;

public class DisplayActivity extends AppCompatActivity {
    private final String LOG_TAG = "woof";
    private ListView mListView;
    private ConnectThread connection;
    private ReadingItem item1;
    private ReadingItem item2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        mListView = (ListView) findViewById(R.id.recipe_list_view);
        ArrayList<ReadingItem> displayList = new ArrayList<ReadingItem>();
        String titleItem1 = "Engine RPM";
        String subtitleItem1 = "Engine Revolutions Per Minute";
        String detailItem1 = "RPM: ";
        item1 = new ReadingItem(titleItem1, subtitleItem1, detailItem1);
        displayList.add(item1);

        String titleItem2 = "Speed";
        String subtitleItem2 = "Miles Per Hour";
        String detailItem2 = "m/h: ";
        item2 = new ReadingItem(titleItem2, subtitleItem2, detailItem2);
        displayList.add(item2);

        ReadingItemAdapter adapter = new ReadingItemAdapter(this, displayList);
        mListView.setAdapter(adapter);

        new Thread(new Runnable() {
            public void run() {
                connectionInitialization();
            }
        }).start();
    }

    private void connectionInitialization() {
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

    /* Thread to maintain the connection to OBDII */
    private class ConnectThread extends Thread {
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        @TargetApi(19)
        public ConnectThread(BluetoothDevice d) {
            BluetoothSocket tmp = null;
            device = d;

            try {
                if (d.getBondState() != BluetoothDevice.BOND_BONDED) {
                    d.createBond();
                    while (d.getBondState() < BluetoothDevice.BOND_BONDED) ;
                }
                Log.d(LOG_TAG, "Trying to connect to: " + d.getName());
                if (d.getUuids()[0].getUuid() != null)
                    tmp = device.createInsecureRfcommSocketToServiceRecord(d.getUuids()[0].getUuid());
                else
                    Log.d(LOG_TAG, "UUIDs got errors...");
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
            Log.d(LOG_TAG, "Socket created");
        }

        /* Starts the connection to OBDII and gives configuration commands */
        public void run() {
            try {
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                socket.connect();
                Log.d(LOG_TAG, "Socket connected to: " + socket.getRemoteDevice().getName());

                new ObdResetCommand().run(socket.getInputStream(), socket.getOutputStream());
                new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());
                new TimeoutCommand(125).run(socket.getInputStream(), socket.getOutputStream());
                new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

                RPMCommand engineRpmCommand = new RPMCommand();
                SpeedCommand speedCommand = new SpeedCommand();
                while (!Thread.currentThread().isInterrupted()) {
                    engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                    speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                    // TODO handle commands result
                    item1.setDescription("rpm: " + engineRpmCommand.getFormattedResult());
                    item2.setDescription("m/p: " + speedCommand.getFormattedResult());
                    Log.d(LOG_TAG, "RPM: " + engineRpmCommand.getFormattedResult());
                    Log.d(LOG_TAG, "Speed: " + speedCommand.getFormattedResult());
                }
            } catch (IOException connectException) {
                try {
                    socket.close();
                } catch (IOException closeException) {
                    Log.e(LOG_TAG, "Could not close the client socket", closeException);
                }
                return;
            } catch (java.lang.InterruptedException e) {
            }

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
