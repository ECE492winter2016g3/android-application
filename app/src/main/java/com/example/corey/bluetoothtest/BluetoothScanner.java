package com.example.corey.bluetoothtest;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by Corey on 01/03/2016.
 */
public class BluetoothScanner implements Runnable {
    private Handler handler;
    private InputStream input;

    public BluetoothScanner(Handler h, InputStream i) {
        handler = h;
        input = i;
    }

    @Override
    public void run() {
        byte[] buf = new byte[256];

        int count = 0;

        while(true) {
            try {
                count = input.read(buf);
                String recvd = new String(Arrays.copyOfRange(buf, 0, count));

                Log.i("BluetoothScanner", "Received packet: " + recvd);

                Message m = new Message();
                Bundle b = new Bundle();

                b.putString("data", recvd);
                m.setData(b);
                handler.sendMessage(m);
            } catch (java.io.IOException e) {
                Log.e("BluetoothScanner", e.getMessage());
            }
        }
    }
}
