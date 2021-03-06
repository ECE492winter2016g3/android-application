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
    public static final int BUF_LEN = 256;

    private Handler handler;
    private InputStream input;

    public BluetoothScanner(Handler h, InputStream i) {
        handler = h;
        input = i;
    }

    @Override
    public void run() {
        byte[] buf = new byte[BUF_LEN];

        int count = 0;

        boolean socketOpen = true;
        while(socketOpen) {
            try {
                count = input.read(buf);
                if(count == 0) {
                    continue;
                }
                byte[] data = Arrays.copyOf(buf, count);
                String recvd = new String(Arrays.copyOfRange(buf, 0, count));

                Log.i("BluetoothScanner", "Received packet: " + recvd);

                Message m = new Message();
                Bundle b = new Bundle();

                b.putByteArray("data", data);
//                b.putString("data", recvd);
                m.setData(b);
                handler.sendMessage(m);
            } catch (java.io.IOException e) {
                Log.e("BluetoothScanner", e.getMessage());
                socketOpen = false;
            }
        }
        Log.i("BluetoothScanner", "Done!");
    }
}
