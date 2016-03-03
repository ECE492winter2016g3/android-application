package com.example.corey.bluetoothtest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Corey on 03/03/2016.
 */
public class CommunicatorActivity extends AppCompatActivity {

    private BluetoothApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final CommunicatorActivity me = this;
        app = (BluetoothApplication) getApplicationContext();

        
    }
}
