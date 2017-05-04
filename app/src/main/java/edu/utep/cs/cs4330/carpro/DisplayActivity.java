package edu.utep.cs.cs4330.carpro;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;
import com.github.pires.obd.commands.engine.RPMCommand;

public class DisplayActivity extends AppCompatActivity {
    private final String LOG_TAG = "woof";
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        String address = getIntent().getStringExtra("DeviceAddress");

        mListView = (ListView) findViewById(R.id.recipe_list_view);

// 1
        ArrayList<ReadingItem> displayList = new ArrayList<ReadingItem>();
        String titleItem1 = "Engine RPM";
        String subtitleItem1 = "Engine Revolutions Per Minute";
        String detailItem1 = "RPM: ";
        ReadingItem item1 = new ReadingItem(titleItem1, subtitleItem1, detailItem1);
        displayList.add(item1);

        String titleItem2 = "Speed";
        String subtitleItem2 = "Miles Per Hour";
        String detailItem2 = "m/h: ";
        ReadingItem item2 = new ReadingItem(titleItem2, subtitleItem2, detailItem2);
        displayList.add(item2);


// 4
        ReadingItemAdapter adapter = new ReadingItemAdapter(this, displayList);
        mListView.setAdapter(adapter);

        // Create socket between device
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        UUID uuid = UUID.randomUUID();
        BluetoothSocket socket = null;
        Log.d(LOG_TAG,"made the socket");

        try {
            Log.d(LOG_TAG,"in the try");
            socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
            socket.connect();

            Log.d(LOG_TAG,"connected");

            new EchoOffCommand().run(socket.getInputStream(), socket.getOutputStream());

            new LineFeedOffCommand().run(socket.getInputStream(), socket.getOutputStream());

            new TimeoutCommand(10).run(socket.getInputStream(), socket.getOutputStream());

            new SelectProtocolCommand(ObdProtocols.AUTO).run(socket.getInputStream(), socket.getOutputStream());

            RPMCommand engineRpmCommand = new RPMCommand();
            SpeedCommand speedCommand = new SpeedCommand();
            while (!Thread.currentThread().isInterrupted())
            {
                engineRpmCommand.run(socket.getInputStream(), socket.getOutputStream());
                speedCommand.run(socket.getInputStream(), socket.getOutputStream());
                // TODO handle commands result
                Log.d(LOG_TAG, "RPM: " + engineRpmCommand.getFormattedResult());
                Log.d(LOG_TAG, "Speed: " + speedCommand.getFormattedResult());
            }

        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
