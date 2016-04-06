package com.example.corey.bluetoothtest;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    // Bluetooth Permissions
    private static final int BLUETOOTH = 0;
    private static final int BLUETOOTH_ADMIN = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    private BluetoothApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final MainActivity me = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app = (BluetoothApplication) getApplicationContext();

        app.bluetooth = new BluetoothManager();

        scan();

        final ProgressBar progress = (ProgressBar) findViewById(R.id.progressBar);
        progress.setVisibility(View.GONE);

        Button refresh = (Button) findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                scan();
            }
        });
        Button canvas = (Button) findViewById(R.id.canvas);
        canvas.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                Intent intent = new Intent(me, CanvasActivity.class);
                startActivity(intent);
            }
        });

        ListView v = (ListView) findViewById(R.id.listView);
        v.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                progress.setVisibility(View.VISIBLE);
                final int pos = position;
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        if(app.bluetooth.connect(pos)) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    progress.setVisibility(View.GONE);
                                    app.mapInitialized = false;
                                    Intent intent = new Intent(me, CanvasActivity.class);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(me, "Couldn't connect!", Toast.LENGTH_LONG).show();
                                    progress.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });

                t.start();

//                    progress.setVisibility(View.GONE);
//                    app.mapInitialized = false;
//                    Intent intent = new Intent(me, CanvasActivity.class);
//                    startActivity(intent);
//                } else {
//                    Toast.makeText(me, "Connection Failed!", Toast.LENGTH_LONG).show();
//                }
            }
        });
    }

    public void scan() {

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    BLUETOOTH
            );
        }
        permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    BLUETOOTH_ADMIN
            );
        }

        app.bluetooth.scan();

        ListView list = (ListView) findViewById(R.id.listView);
        ArrayAdapter<BluetoothDevice> adapter = new BluetoothDeviceAdapter(this, R.layout.bluetooth_device_list_row, app.bluetooth.getDevices());
        list.setAdapter(adapter);

    }
}
