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
import com.github.pires.obd.commands.protocol.EchoOffCommand;
import com.github.pires.obd.commands.protocol.LineFeedOffCommand;
import com.github.pires.obd.commands.protocol.SelectProtocolCommand;
import com.github.pires.obd.commands.protocol.TimeoutCommand;
import com.github.pires.obd.enums.ObdProtocols;

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
        final ArrayList<ReadingItem> displayList = new ArrayList<ReadingItem>();
        for(int i = 0; i < 30; i++){
            String title = "Title of item"+i;
            String subtitle = "Subtitle of item "+i;
            String detail = "Detail of item "+i;
            ReadingItem currentItem = new ReadingItem(title, subtitle, detail);
            displayList.add(currentItem);
        }

// 4
        ReadingItemAdapter adapter = new ReadingItemAdapter(this, displayList);
        mListView.setAdapter(adapter);

        // Create socket between device
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        Log.d(LOG_TAG,"Remote: " +device.getName());
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
        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
