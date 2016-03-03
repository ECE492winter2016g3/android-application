package com.example.corey.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    private OutputStream os;
    private InputStream is;
    private BluetoothSocket s;

    private BluetoothApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final MainActivity me = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        app = (BluetoothApplication) getApplicationContext();

        devices = new ArrayList<BluetoothDevice>();
        app.bluetooth = new BluetoothManager();

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
                app.bluetooth.disconnect();
            }
        });
        Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.i("Bluetooth", "Send Start!");
                EditText toSend = (EditText) findViewById(R.id.toSend);
                String message = toSend.getText().toString();
                if(app.bluetooth.send(message)) {
                    toSend.setText("");
                } else {
                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_LONG);
                }
                Log.i("Bluetooth", "Send End!");
            }
        });

        app.bluetooth.subscribe(new BluetoothManager.InputHandler() {
            public void handleMessage(String message) {

                TextView received = (TextView) findViewById(R.id.lastReceived);
                received.setText("Last Received: |" + message + "|");
                Toast.makeText(me, "Received: " + message + " from server", Toast.LENGTH_LONG).show();
            }
        });

        ListView v = (ListView) findViewById(R.id.listView);
        v.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                app.bluetooth.connect(position);
            }
        });
    }

    protected List<BluetoothDevice> devices;

    public void scan() {
        app.bluetooth.scan();

        ListView list = (ListView) findViewById(R.id.listView);
        ArrayAdapter<BluetoothDevice> adapter = new BluetoothDeviceAdapter(this, R.layout.bluetooth_device_list_row, app.bluetooth.getDevices());
        list.setAdapter(adapter);

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
