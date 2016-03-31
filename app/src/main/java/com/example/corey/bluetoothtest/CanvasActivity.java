package com.example.corey.bluetoothtest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Corey on 04/03/2016.
 */
public class CanvasActivity extends AppCompatActivity {
    private BluetoothApplication app;
    private final mapping map = new mapping();

    private final ArrayList<Float> sweepInProgressDists = new ArrayList<>();
    private final ArrayList<Float> sweepInProgressAngles = new ArrayList<>();

    private final Holder<ScanType> moveType = new Holder<>(ScanType.INITIAL);

    public enum HeldButton {
        NONE,
        FORWARD,
        REVERSE,
        LEFT,
        RIGHT
    };

    public enum ScanType {
        INITIAL,
        LINEAR,
        ROTATION
    };

    public class Holder<T> {
        public Holder(T value) {
            myValue = value;
        }
        public void set(T value) {
            myValue = value;
        }
        public T get() {
            return myValue;
        }
        private T myValue;
    }

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
    protected void onStart() {
        super.onStart();
        Log.i("CanvasActivity", "Sending Init packets!");
        moveType.set(ScanType.INITIAL);
        app.bluetooth.send("i");
        app.bluetooth.send("i");
        app.bluetooth.send("i");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvas_activity);
        final AppCompatActivity me = this;
        app = (BluetoothApplication) getApplicationContext();

        map.init();
        Log.i("CanvasActivity", "Sending Init packets!");
        app.bluetooth.send("i");
        app.bluetooth.send("i");
        app.bluetooth.send("i");
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

        final Holder<HeldButton> holder = new Holder<>(HeldButton.NONE);

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
                        moveType.set(ScanType.LINEAR);
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
                        moveType.set(ScanType.LINEAR);
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
                        moveType.set(ScanType.ROTATION);
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
                        moveType.set(ScanType.ROTATION);
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
////        });
//        'S' - start
//                'V' - value
//                        'F' - finish

        app.bluetooth.subscribe(new ByteArrayHandler() {
            public void handle(byte[] message) {
                byte code = message[0];
                if(code == 'S') {
                    sweepInProgressDists.clear();
                    sweepInProgressAngles.clear();
                } else if(code == 'V') {
                    Log.i("CanvasActivity", "Value Message!");
                    if(message.length < 5) {
                        Log.e("CanvasActivity", "Value message too short!");
                        return;
                    }
                    int distance = 0;
                    byte lsb = message[1];
                    byte msb = message[2];
                    distance += (lsb & 0xFF);
                    distance += ((msb << 8) & 0xFF00);
                    sweepInProgressDists.add((float) distance);

                    int steps = 0;
                    lsb = message[3];
                    msb = message[4];
                    steps += (lsb & 0xFF);
                    steps += ((msb << 8) & 0xFF00);
                    float angle = (((float) steps) - 36.0f) * 1.8f * 2.0f * (float) Math.PI / 360.0f;
                    sweepInProgressAngles.add(angle);
                } else if(code == 'F') {
                    float[] angles = new float[sweepInProgressAngles.size()];
                    float[] dists = new float[sweepInProgressDists.size()];
                    if(angles.length != dists.length) {
                        Log.e("CanvasActivity", "# of angles != # of dists!");
                    }
                    for(int i = 0; i < angles.length; ++i) {
                        angles[i] = sweepInProgressAngles.get(i);
                        dists[i] = sweepInProgressDists.get(i);
                    }
                    if(moveType.get() == ScanType.LINEAR) {
                        Log.i("CanvasActivity", "ScanType LINEAR");
                        map.updateLin(angles, dists);
                    } else if(moveType.get() == ScanType.ROTATION) {
                        Log.i("CanvasActivity", "ScanType ROTATION");
                        map.updateRot(angles, dists, 0 /* TODO: Turn hint*/);
                    } else if(moveType.get() == ScanType.INITIAL) {
                        Log.i("CanvasActivity", "ScanType INITIAL");
                        map.initialScan(angles, dists);
                    }
                    List<mapping.MapSegment> segments = map.getSegments();
                    for(int i = 0; i < segments.size(); ++i) {
                        mapping.MapSegment segment = segments.get(i);
                        drawView.addLine(new DrawView.Line(
                                segment.origin.x,
                                segment.origin.y,
                                segment.vec.x,
                                segment.vec.y
                        ));
                        drawView.invalidate();
                    }
                } else if(code == 'T') {

                } /*else if(code == 'I') {
                    float[] angles = new float[sweepInProgressAngles.size()];
                    float[] dists = new float[sweepInProgressDists.size()];
                    if(angles.length != dists.length) {
                        Log.e("CanvasActivity", "# of angles != # of dists!");
                    }
                    for(int i = 0; i < angles.length; ++i) {
                        angles[i] = sweepInProgressAngles.get(i);
                        dists[i] = sweepInProgressDists.get(i);
                    }
                    map.initialScan(angles, dists);
                }*/
                if(false /* Start of sweep */) {

                } else if(false /* End of sweep */) {

                }
                if(true)
                    return;

                if(message.length < 5) {
                    Log.e("CanvasActivity", "Message too short!");
                    return;
                }
                byte lsb = message[1];
                byte msb = message[2];

                Log.i("CanvasActivity", "bin lsb: " + String.format("%02X", lsb));
                Log.i("CanvasActivity", "bin msb: " + String.format("%02X", msb));

                int distance = 0;
                distance += (lsb & 0xFF);
                distance += ((msb << 8) & 0xFF00);

                lsb = message[3];
                msb = message[4];

                int angle = 0;
                angle += (lsb & 0xFF);
                angle += ((msb << 8) & 0xFF00);

                float realAngle = ((float) angle) / 10;

                int x = (int) (Math.cos(realAngle) * ((float) distance));
                int y = (int) (Math.sin(realAngle) * ((float) distance));

                sweepInProgressDists.add((float) distance);
                sweepInProgressAngles.add(realAngle);

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
