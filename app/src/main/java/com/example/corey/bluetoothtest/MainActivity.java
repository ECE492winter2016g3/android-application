package com.example.corey.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private BluetoothApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final MainActivity me = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (BluetoothApplication) getApplicationContext();

        app.bluetooth = new BluetoothManager();

        scan();

        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                scan();
            }
        });

        ListView v = (ListView) findViewById(R.id.listView);
        v.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                app.bluetooth.connect(position);
                Intent intent = new Intent(me, CommunicatorActivity.class);
                startActivity(intent);
            }
        });
    }

    public void scan() {
        app.bluetooth.scan();

        ListView list = (ListView) findViewById(R.id.listView);
        ArrayAdapter<BluetoothDevice> adapter = new BluetoothDeviceAdapter(this, R.layout.bluetooth_device_list_row, app.bluetooth.getDevices());
        list.setAdapter(adapter);

    }
}
