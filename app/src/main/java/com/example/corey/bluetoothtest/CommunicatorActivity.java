package com.example.corey.bluetoothtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corey on 03/03/2016.
 */
public class CommunicatorActivity extends AppCompatActivity {

    private BluetoothApplication app;
    private List<String> messages = new ArrayList<>();
    private ArrayAdapter<String> messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communicator);

        final CommunicatorActivity me = this;
        app = (BluetoothApplication) getApplicationContext();

        Button send = (Button) findViewById(R.id.commSend);
        Button disconnect = (Button) findViewById(R.id.commDisconnect);
        ListView received = (ListView) findViewById(R.id.commReceived);

        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Log.i("Bluetooth", "Send Start!");
                EditText toSend = (EditText) findViewById(R.id.commToSend);
                String message = toSend.getText().toString();
                if(app.bluetooth.send(message)) {
                    messages.add("SENT: " + message);
                    toSend.setText("");
                    messageAdapter.notifyDataSetChanged();

                } else {
                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_LONG).show();
                }
                Log.i("Bluetooth", "Send End!");
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                app.bluetooth.disconnect();
                messages.clear();
                Intent intent = new Intent(me, MainActivity.class);
                startActivity(intent);
            }
        });

        app.bluetooth.subscribe(new StringHandler() {
            public void handle(String message) {
                messages.add("RECEIVED: " + message);
                messageAdapter.notifyDataSetChanged();
            }
        });

        messageAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, messages);
        received.setAdapter(messageAdapter);
    }
}
