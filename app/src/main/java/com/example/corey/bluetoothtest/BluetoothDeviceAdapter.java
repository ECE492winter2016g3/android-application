package com.example.corey.bluetoothtest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Corey on 03/03/2016.
 */
public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    public BluetoothDeviceAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public BluetoothDeviceAdapter(Context context, int resource, List<BluetoothDevice> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.bluetooth_device_list_row, null);
        }

        BluetoothDevice dev = getItem(position);

        if(dev != null) {
            TextView name = (TextView) v.findViewById(R.id.deviceName);

            if(name != null) {
                name.setText(dev.getName());
            }
        }

        return v;
    }
}
