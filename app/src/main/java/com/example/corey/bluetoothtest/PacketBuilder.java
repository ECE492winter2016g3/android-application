package com.example.corey.bluetoothtest;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Corey on 04/03/2016.
 */
public class PacketBuilder {
    private static final byte START_BYTE = BluetoothManager.START_BYTE;
    private static final byte END_BYTE = BluetoothManager.END_BYTE;
    private static final byte ESCAPE_BYTE = BluetoothManager.ESCAPE_BYTE;

    private boolean escaped = false;

    @Deprecated
    public void pushString(String data) {
//        Log.i("PacketBuilder", "data = \"" + data + "\"");
        for(int i = 0; i < data.length(); ++i) {
            byte c = (byte) data.charAt(i);
            Log.i("PacketBuilder", "Received: " + (int) c);
            if((c == START_BYTE) && !escaped) {
                escaped = false;
                if(current.length() > 0) {
                    Log.e("PacketBuilder", "START_BYTE with incomplete packet still in progress");
                    Log.e("PacketBuilder", "Current contents: " + current);
                }
                current = "";
            } else if((c == END_BYTE) && !escaped) {
                escaped = false;
                Log.i("PacketBuilder", "END_BYTE received");
//                for(StringHandler h : handlers) {
//                    h.handle(current);
//                }
                current = "";
            } else if((int) c == -3) {
                Log.i("PacketBuilder", "ESCAPE received");
                escaped = true;
            } else {
                escaped = false;
                current += (char) c;
            }
        }
    }

    public void pushByteArray(byte[] data) {
        Log.i("PacketBuilder", "pushByteArray start --------------------");
        if(currentBytes == null) {
            currentBytes = new byte[data.length];
            currentIndex = 0;
        } else {
            byte[] newArray = new byte[currentIndex + data.length];
//            currentIndex = currentBytes.length;
            System.arraycopy(currentBytes, 0, newArray, 0, currentIndex);
            currentBytes = newArray;
        }
        if(currentBytes == null) {
            Log.i("PacketBuilder", "LOLWTF");
        }
        for(int i = 0; i < data.length; ++i) {
            byte c = data[i];
            Log.i("PacketBuilder", "Received: " + c);
            if((c == START_BYTE) && !escaped) {
                if(currentBytes.length > 0) {
//                    Log.e("PacketBuilder", "START_BYTE with incomplete packet still in progress");
//                    Log.e("PacketBuilder", "Current contents: " + currentBytes);
                }
                escaped = false;
//                current = "";
                currentBytes = new byte[data.length];
                currentIndex = 0;
            } else if((c == END_BYTE) && !escaped) {
//                Log.i("PacketBuilder", "END_BYTE received");
                for(ByteArrayHandler h : handlers) {
                    h.handle(Arrays.copyOf(currentBytes, currentIndex));
                }
                escaped = false;
//                current = "";
                currentBytes = new byte[data.length];
                currentIndex = 0;
            } else if((c == ESCAPE_BYTE) && !escaped) {
//                Log.i("PacketBuilder", "ESCAPE received");
                escaped = true;
            } else {
                escaped = false;
//                current += (char) c;
                currentBytes[currentIndex++] = c;
            }
        }
        Log.i("PacketBuilder", "pushByteArray end --------------------");
    }

    public void subscribe(ByteArrayHandler handler) {
        handlers.add(handler);
    }

    public void clear() {
        handlers.clear();
    }

    private String current = "";
    private byte[] currentBytes;
    private int currentIndex;
    private List<ByteArrayHandler> handlers = new ArrayList<>();
}
