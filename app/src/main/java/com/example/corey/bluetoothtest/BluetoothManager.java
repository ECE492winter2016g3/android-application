package com.example.corey.bluetoothtest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Corey on 01/03/2016.
 */
public class BluetoothManager {
    private BluetoothAdapter adapter;
    private List<BluetoothDevice> devices;

    private BluetoothSocket socket;
    private InputStream input;
    private OutputStream output;

    private final static UUID SPP_UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB"
    );

    public BluetoothManager() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        devices = new ArrayList<>();
    }


    public void scan() {
        Set<BluetoothDevice> deviceSet = adapter.getBondedDevices();
        for(BluetoothDevice bt : deviceSet) {
            devices.add(bt);
        }
    }

    public List<BluetoothDevice> getDevices() {
        return devices;
    }

    public boolean connect(int index) {
        BluetoothDevice device = devices.get(index);

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);

            socket.connect();

            output = socket.getOutputStream();
            input = socket.getInputStream();

        } catch(IOException e) {
            socket = null;
            input = null;
            output = null;

            return false;
        }
        return true;
    }

    public interface InputHandler {
        public void handleMessage(String message);
    }

    public void subscribe(InputHandler handler) {

    }

}
