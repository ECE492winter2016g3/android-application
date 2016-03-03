package com.example.corey.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Message;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    private OutputStream os;
    private InputStream is;
    private BluetoothSocket s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final MainActivity me = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        devices = new ArrayList<BluetoothDevice>();

        scan();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                scan();
            }
        });
        Button disconnect = (Button) findViewById(R.id.disconnect);
        disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Log.i("Bluetooth", "Disconnect Start!");
                try {
                    is.close();
                } catch (java.io.IOException e) {

                }
                try {
                    os.close();
                } catch (java.io.IOException e) {

                }
                try {
                    s.close();
                } catch (java.io.IOException e) {

                }
                Log.i("Bluetooth", "Disconnect End!");
            }
        });
        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.i("Bluetooth", "Send Start!");
                EditText toSend = (EditText) findViewById(R.id.toSend);
                try {
                    os.write(toSend.getText().toString().getBytes(StandardCharsets.UTF_8));
                    toSend.setText("");
                } catch (java.io.IOException e) {

                }
                Log.i("Bluetooth", "Send End!");
            }
        });

        final Handler uiThreadHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                String recvd = message.getData().getString("data");

                PacketEncoder.testLogPacket(recvd);
                if(PacketEncoder.getType(recvd) == PacketEncoder.PacketType.DATA) {
                    List<Integer> data = PacketEncoder.decodeDataPacket(recvd);
                    String testEncoded = PacketEncoder.encodeDataPacket(data);
                    if(testEncoded.equals(recvd)) {
                        Toast.makeText(me, "Encoding Worked!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(me, "Encoding Didn't Work!", Toast.LENGTH_SHORT).show();
                        Log.i("UiThreadHandler", "received: " + recvd);
                        Log.i("UiThreadHandler", "encoded: " + testEncoded);
                    }
                    recvd = "[";
                    for(int i = 0; i < data.size(); ++i) {
                        recvd += data.get(i) + ", ";
                    }
                    recvd.substring(recvd.length() - 3);
                    recvd += "]";
                }

                TextView received = (TextView) findViewById(R.id.lastReceived);
                received.setText("Last Received: |" + recvd + "|");
                Toast.makeText(me, "Received: " + recvd + " from server", Toast.LENGTH_LONG).show();
            }
        };

        final BluetoothScanner scanner = new BluetoothScanner(uiThreadHandler, is);

//        class MyScanner extends Observable implements Runnable {
//            @Override
//            public void run() {
//                byte[] buf = new byte[256];
//                byte[] data;
//                int count = 0;
//                Log.i("Bluetooth", "MyScanner :: Start!");
//                while(true) {
//                    try {
//                        count = is.read(buf);
//                        final String recvd = new String(Arrays.copyOfRange(buf, 0, count), StandardCharsets.UTF_8);
//                        Log.i("Bluetooth", "MyScanner :: Received (UTF8) : |" + recvd + "|" + count + "|");
////                        Log.i("Bluetooth", "MyScanner :: Received (ASCII): |" + new String(Arrays.copyOfRange(buf, 0, count), StandardCharsets.US_ASCII) + "|");
////                        Log.i("Bluetooth", "MyScanner :: Received (UTF16): |" + new String(Arrays.copyOfRange(buf, 0, count), StandardCharsets.UTF_16) + "|");
////                        Log.i("Bluetooth", "MyScanner :: Received (ISO)  : |" + new String(Arrays.copyOfRange(buf, 0, count), StandardCharsets.ISO_8859_1) + "|");
//                        Log.i("Bluetooth", "I have " + this.countObservers() + " observers");
////                        this.setChanged();
//                        Message m = new Message();
//                        Bundle b = new Bundle();
//                        b.putString("data", recvd);
//                        m.setData(b);
//                        uiThreadHandler.sendMessage(m);
////                        this.notifyObservers(recvd);
//                        Log.i("Bluetooth", "MyScanner :: observers notified");
//                    } catch (java.io.IOException e) {
//
//                    }
//                }
//            }
//        };
//        final MyScanner scanner = new MyScanner();

//        scanner.addObserver(new Observer() {
//            private int count = 0;
//
//            public void update(Observable o, Object data) {
//                Log.i("Bluetooth", "update! data: " + (String) data);
//                ++count;
//                final String dataString = (String) data;
//                uiThreadHandler.post(new Runnable() {
//                    public void run() {
//                        TextView received = (TextView) findViewById(R.id.lastReceived);
//                        received.setText("Last Received: |" + (String) dataString + "|" + count);
//                        Toast.makeText(me, "Received: " + (String) dataString + " from server", Toast.LENGTH_LONG).show();
//                    }
//                });
//            }
//        });

        ListView v = (ListView) findViewById(R.id.listView);
        v.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice d = devices.get(position);
                Log.i("Bluetooth", "ID: " + d.getAddress());
                Log.i("Bluetooth", "Name: " + d.getName());
                try{
                    UUID uuid = UUID.fromString(
                            "00001101-0000-1000-8000-00805F9B34FB"
                    );
                    s = d.createInsecureRfcommSocketToServiceRecord(uuid);
                    boolean success = true;
                    try {
                        Log.i("Bluetooth", "Connect with UUID: " + uuid.toString());
                        s.connect();
                    }
                    catch (Exception e) {
                        success = false;
                        Toast.makeText(me, "Connection didn't work!", Toast.LENGTH_SHORT).show();
                        Log.i("Bluetooth", "Connection didn't work!");
                        Log.i("Bluetooth", e.getMessage());
                    }
                    if(success) {
                        Toast.makeText(me, "Connected!", Toast.LENGTH_SHORT).show();
                        Log.i("Bluetooth", "Connected!");
                        Log.i("Bluetooth", "s.isConnected(): " + (s.isConnected() ? "True" : "False"));
                    }
                    os = s.getOutputStream();
                    is = s.getInputStream();

                    Thread t = new Thread(scanner);
                    t.start();

                }
                catch(java.io.IOException e) {
                    Toast.makeText(me, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected List<BluetoothDevice> devices;

    public void scan() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

            List<String> s = new ArrayList<String>();
            devices.clear();
            for (BluetoothDevice bt : pairedDevices) {
                s.add(bt.getName());
                devices.add(bt);
            }


            ListView list = (ListView) findViewById(R.id.listView);

            list.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, s));
        }
        catch(Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
