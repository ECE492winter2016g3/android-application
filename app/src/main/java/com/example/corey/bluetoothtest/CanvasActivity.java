package com.example.corey.bluetoothtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Corey on 04/03/2016.
 */
public class CanvasActivity extends AppCompatActivity {
    private BluetoothApplication app;

    public enum HeldButton {
        NONE,
        FORWARD,
        REVERSE,
        LEFT,
        RIGHT
    };

    public class ButtonHolder {
        public void set(HeldButton btn) {
            button = btn;
        }
        public HeldButton get() {
            return button;
        }
        private HeldButton button = HeldButton.NONE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvas_activity);
        final AppCompatActivity me = this;
        app = (BluetoothApplication) getApplicationContext();

        final DrawView drawView = (DrawView) findViewById(R.id.draw);



        Button back = (Button) findViewById(R.id.back);
//        Button oneCW = (Button) findViewById(R.id.oneCW);
//        Button oneCCW = (Button) findViewById(R.id.oneCCW);
//        Button twoCW = (Button) findViewById(R.id.twoCW);
//        Button twoCCW = (Button) findViewById(R.id.twoCCW);
//        Button oneStop = (Button) findViewById(R.id.oneStop);
//        Button twoStop = (Button) findViewById(R.id.twoStop);
//        Button stepperLeft = (Button) findViewById(R.id.stepperLeft);
//        Button stepperRight = (Button) findViewById(R.id.stepperRight);

        Button forward = (Button) findViewById(R.id.forward);
        Button reverse = (Button) findViewById(R.id.reverse);
        Button right = (Button) findViewById(R.id.right);
        Button left = (Button) findViewById(R.id.left);

        final TextView lidarDistance = (TextView) findViewById(R.id.lidarDistance);
        final TextView rotation = (TextView) findViewById(R.id.rotation);

        final ButtonHolder holder = new ButtonHolder();

        final Timer timer = new Timer();
        final TimerTask periodicSend = new TimerTask() {
            @Override
            public void run() {
                switch(holder.get()) {
                    case FORWARD:
                        app.bluetooth.send("f");
                        app.bluetooth.send("f");
                        app.bluetooth.send("f");
                        break;
                    case REVERSE:
                        app.bluetooth.send("b");
                        app.bluetooth.send("b");
                        app.bluetooth.send("b");
                        break;
                    case LEFT:
                        app.bluetooth.send("l");
                        app.bluetooth.send("l");
                        app.bluetooth.send("l");
                        break;
                    case RIGHT:
                        app.bluetooth.send("r");
                        app.bluetooth.send("r");
                        app.bluetooth.send("r");
                        break;
                }
            }
        };

        timer.schedule(periodicSend, 0, 500);

        forward.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        holder.set(HeldButton.FORWARD);
                        app.bluetooth.send("f");
                        app.bluetooth.send("f");
                        app.bluetooth.send("f");
                        return true;
                    case MotionEvent.ACTION_UP:
                        holder.set(HeldButton.NONE);
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        return true;
                }
                return false;
            }
        });
        reverse.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        holder.set(HeldButton.REVERSE);
                        app.bluetooth.send("b");
                        app.bluetooth.send("b");
                        app.bluetooth.send("b");
                        return true;
                    case MotionEvent.ACTION_UP:
                        holder.set(HeldButton.NONE);
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        return true;
                }
                return false;
            }
        });
        left.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        holder.set(HeldButton.LEFT);
                        app.bluetooth.send("l");
                        app.bluetooth.send("l");
                        app.bluetooth.send("l");
                        return true;
                    case MotionEvent.ACTION_UP:
                        holder.set(HeldButton.NONE);
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        return true;
                }
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        holder.set(HeldButton.RIGHT);
                        app.bluetooth.send("r");
                        app.bluetooth.send("r");
                        app.bluetooth.send("r");
                        return true;
                    case MotionEvent.ACTION_UP:
                        holder.set(HeldButton.NONE);
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        return true;
                }
                return false;
            }
        });

//
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                app.bluetooth.disconnect();
                Intent intent = new Intent(me, MainActivity.class);
                startActivity(intent);
            }
        });
//        oneCW.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if(app.bluetooth.send("a")) {
//                    Toast.makeText(me, "Sent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        oneCCW.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if(app.bluetooth.send("b")) {
//                    Toast.makeText(me, "Sent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        twoCW.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if(app.bluetooth.send("c")) {
//                    Toast.makeText(me, "Sent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        twoCCW.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if(app.bluetooth.send("d")) {
//                    Toast.makeText(me, "Sent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        oneStop.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if(app.bluetooth.send("e")) {
//                    Toast.makeText(me, "Sent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        twoStop.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if(app.bluetooth.send("f")) {
//                    Toast.makeText(me, "Sent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        stepperLeft.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if(app.bluetooth.send("l")) {
//                    Toast.makeText(me, "Sent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//        stepperRight.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View view) {
//                if(app.bluetooth.send("r")) {
//                    Toast.makeText(me, "Sent!", Toast.LENGTH_SHORT).show();
//                } else {
//                    Toast.makeText(me, "Send didn't work!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        app.bluetooth.subscribe(new ByteArrayHandler() {
            public void handle(byte[] message) {
                if(message.length < 4) {
                    Log.e("CanvasActivity", "Message too short!");
                    return;
                }
                byte lsb = message[0];
                byte msb = message[1];

                Log.i("CanvasActivity", "bin lsb: " + String.format("%02X", lsb));
                Log.i("CanvasActivity", "bin msb: " + String.format("%02X", msb));

                int distance = 0;
                distance += (lsb & 0xFF);
                distance += ((msb << 8) & 0xFF00);

                lsb = message[2];
                msb = message[3];

                int angle = 0;
                angle += (lsb & 0xFF);
                angle += ((msb << 8) & 0xFF00);

                double realAngle = ((double) angle) / 10;

                int x = (int) (Math.cos(realAngle) * ((double) distance));
                int y = (int) (Math.sin(realAngle) * ((double) distance));

                drawView.clear();
                drawView.addPoint(new DrawView.Point(-200, -200));
                drawView.addPoint(new DrawView.Point(200, -200));
                drawView.addPoint(new DrawView.Point(-200, 200));
                drawView.addPoint(new DrawView.Point(200, 200));
                drawView.addLine(new DrawView.Line(0, 0, x, y));

                drawView.invalidate();

                Log.i("CanvasActivity", "bin dist: " + String.format("%04X", distance));
                Log.i("CanvasActivity", "Rotation int: " + angle);

                String output = "LIDAR distance: " + distance + "cm";

                Log.i("CanvasActivity", output);

                lidarDistance.setText(output);

                output = "Rotation: " + realAngle + " deg";

                Log.i("CanvasActivity", output);
                rotation.setText(output);
            }
        });
    }

}
