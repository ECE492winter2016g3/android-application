package com.example.corey.bluetoothtest;

import android.app.Application;

/**
 * Created by Corey on 03/03/2016.
 */
public class BluetoothApplication extends Application {
    // Probably bad, but oh well
    public BluetoothManager bluetooth = new BluetoothManager();
    public boolean mapInitialized = false;
}
