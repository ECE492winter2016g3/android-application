package com.example.corey.bluetoothtest;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corey on 04/03/2016.
 */
public class PacketBuilder {
    private static final char START_BYTE = BluetoothManager.START_BYTE;
    private static final char END_BYTE = BluetoothManager.END_BYTE;

    public void pushString(String data) {
        Log.i("PacketBuilder", "data = \"" + data + "\"");
        for(int i = 0; i < data.length(); ++i) {
            char c = data.charAt(i);
            if(c == START_BYTE) {
                if(current.length() > 0) {
                    Log.e("PacketBuilder", "START_BYTE with incomplete packet still in progress");
                    Log.e("PacketBuilder", "Current contents: " + current);
                }
                current = "";
            } else if(c == END_BYTE) {
                for(StringHandler h : handlers) {
                    h.handle(current);
                }
            } else {
                current += c;
            }
        }
    }

    public void subscribe(StringHandler handler) {
        handlers.add(handler);
    }

    public void clear() {
        handlers.clear();
    }

    private String current = "";
    private List<StringHandler> handlers = new ArrayList<>();
}
