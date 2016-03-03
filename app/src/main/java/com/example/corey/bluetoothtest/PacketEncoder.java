package com.example.corey.bluetoothtest;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corey on 26/02/2016.
 */
public class PacketEncoder {
    public enum PacketType {
        DATA,
        UNKNOWN
    };

    public static void testLogPacket(String packet) {
        Log.i("PacketEncoder", "Packet length: " + packet.length());
        for(int i = 0; i < packet.length(); ++i) {
            Log.i("PacketEncoder", "Packet at " + i + ": " + (int)packet.charAt(i));
        }
    }

    public static List<Integer> decodeDataPacket(String packet) {
        List<Integer> arr;

        if(packet.charAt(0) != 'a') {
            return null;
        }

        int length = 0;
        char msb = packet.charAt(1);
        char lsb = packet.charAt(2);

        length += msb << 8;
        length += lsb;

        arr = new ArrayList<>(length);

        for(int i = 0; i < length; ++i) {
            msb = packet.charAt(2*i + 3);
            lsb = packet.charAt(2*i + 4);

            int value = 0;
            value += msb << 8;
            value += lsb;

            arr.add(value);
        }

        return arr;
    }

    public static String encodeDataPacket(List<Integer> data) {
        String packet = "a";

        int length = data.size();

        char msb = (char) ((length & 0xFF00) >> 8);
        char lsb = (char) (length & 0xFF);

        packet += msb;
        packet += lsb;

        for(int i = 0; i < length; ++i) {
            msb = (char) ((data.get(i) & 0xFF00) >> 8);
            lsb = (char) (data.get(i) & 0x00FF);

            packet += msb;
            packet += lsb;
        }

        packet += 'a';

        return packet;
    }

    public static PacketType getType(String packet) {
        Log.i("PacketEncoder", "charAt(0): " + packet.charAt(0));
        if(packet.charAt(0) == 'a') {
            return PacketType.DATA;
        } else {
            return PacketType.UNKNOWN;
        }
    }
}
