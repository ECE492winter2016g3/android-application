package com.example.corey.bluetoothtest;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    private Thread inputThread;
    private MessageHandler messageHandler;


    private final static UUID SPP_UUID = UUID.fromString(
            "00001101-0000-1000-8000-00805F9B34FB"
    );

    private final static Charset CHARSET = StandardCharsets.UTF_8;

    public BluetoothManager() {
        adapter = BluetoothAdapter.getDefaultAdapter();
        devices = new ArrayList<>();
        messageHandler = new MessageHandler();
    }


    public void scan() {
        Set<BluetoothDevice> deviceSet = adapter.getBondedDevices();
        devices.clear();
        for(BluetoothDevice bt : deviceSet) {
            devices.add(bt);
        }
    }

    public List<BluetoothDevice> getDevices() {
        return devices;
    }

    public boolean connect(int index) {
        BluetoothDevice device = devices.get(index);

        // Disconnect first in case we try to connect twice
        if(socket != null) {
            disconnect();
        }

        try {
            socket = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);

            socket.connect();

            output = socket.getOutputStream();
            input = socket.getInputStream();

            BluetoothScanner scanner = new BluetoothScanner(messageHandler, input);
            inputThread = new Thread(scanner);
            inputThread.start();

        } catch(IOException e) {
            Log.i("BluetoothManager", "Connection Failed!");
            socket = null;
            input = null;
            output = null;

            if(inputThread != null) {
                inputThread.interrupt();
                inputThread = null;
            }

            return false;
        }
        return true;
    }

    public void subscribe(InputHandler handler) {
        messageHandler.addHandler(handler);
    }

    public void disconnect() {
        Log.i("Bluetooth", "Disconnect Start!");
        try {
            input.close();
            input = null;
        } catch (java.io.IOException e) {

        }
        try {
            output.close();
            output = null;
        } catch (java.io.IOException e) {

        }
        try {
            socket.close();
            socket = null;
        } catch (java.io.IOException e) {

        }

        if(inputThread != null) {
            inputThread.interrupt();
            inputThread = null;
        }
        Log.i("Bluetooth", "Disconnect End!");
    }

    public boolean send(String message) {
        if(output == null) {
            return false;
        }
        try {
            output.write(message.toString().getBytes(CHARSET));
        } catch(IOException e) {
            return false;
        }
        return true;
    }

    public interface InputHandler {
        public void handleMessage(String message);
    }

    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String contents = message.getData().getString("data");

            for(InputHandler h : handlers) {
                h.handleMessage(contents);
            }
        }

        public void addHandler(InputHandler handler) {
            handlers.add(handler);
        }

        public void clear() {
            handlers.clear();
        }

        private List<InputHandler> handlers = new ArrayList<InputHandler>();
    }

}
