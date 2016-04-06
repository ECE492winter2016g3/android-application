package com.example.corey.bluetoothtest;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Corey on 04/03/2016.
 */
public class CanvasActivity extends AppCompatActivity {
    // Storage Permissions
    private static final int READ_EXTERNAL_STORAGE = 0;
    private static final int WRITE_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static float RADS_PER_SEC = 3.840f;

    private BluetoothApplication app;
    private final mapping map = new mapping();

    private final ArrayList<Float> sweepInProgressDists = new ArrayList<>();
    private final ArrayList<Float> sweepInProgressAngles = new ArrayList<>();

    private final Holder<ScanType> moveType = new Holder<>(ScanType.INITIAL);
//    private final Holder<Integer> rotation = new Holder<>(0);
    float rotation = 0;

    private int logCount = 0;
    private File logDir = null;

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
        Log.i("CanvasActivity", "onStart!");
        if(!app.mapInitialized) {
            Log.i("CanvasActivity", "Sending Init packets!");
            moveType.set(ScanType.INITIAL);
            app.bluetooth.send("i");
//            app.bluetooth.send("i");
//            app.bluetooth.send("i");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("CanvasActivity", "onDestroy!");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("CanvasActivity", "onCreate!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.canvas_activity);
        final CanvasActivity me = this;
        app = (BluetoothApplication) getApplicationContext();

        map.init();
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

        final TextView dimensions = (TextView) findViewById(R.id.dimensions);
//        final TextView rotation = (TextView) findViewById(R.id.rotation);

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
                        return false;
                    case MotionEvent.ACTION_UP:
                        holder.set(HeldButton.NONE);
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        return false;
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
                        return false;
                    case MotionEvent.ACTION_UP:
                        holder.set(HeldButton.NONE);
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        return false;
                }
                return false;
            }
        });

        left.setOnTouchListener(new View.OnTouchListener() {
            private long timeStamp;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timeStamp = System.currentTimeMillis();
                        moveType.set(ScanType.ROTATION);
                        holder.set(HeldButton.LEFT);
                        app.bluetooth.send("l");
                        app.bluetooth.send("l");
                        app.bluetooth.send("l");
                        return false;
                    case MotionEvent.ACTION_UP:
                        long currentTime = System.currentTimeMillis();

                        Log.i("Rotation", "LEFT Rotated for " + (currentTime - timeStamp) + "ms");
                        holder.set(HeldButton.NONE);
                        CanvasActivity.this.rotation = (float) (currentTime - timeStamp) * RADS_PER_SEC / 1000f;
                        Log.i("Rotation", "LEFT Rotated by " + CanvasActivity.this.rotation);
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        return false;
                }
                return false;
            }
        });
        right.setOnTouchListener(new View.OnTouchListener() {
            private long timeStamp;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        timeStamp = System.currentTimeMillis();
                        moveType.set(ScanType.ROTATION);
                        holder.set(HeldButton.RIGHT);
                        app.bluetooth.send("r");
                        app.bluetooth.send("r");
                        app.bluetooth.send("r");
                        return false;
                    case MotionEvent.ACTION_UP:
                        long currentTime = System.currentTimeMillis();
                        Log.i("Rotation", "RIGHT Rotated for " + (currentTime - timeStamp) + "ms");
                        holder.set(HeldButton.NONE);
                        CanvasActivity.this.rotation = (float) (currentTime - timeStamp) * -RADS_PER_SEC / 1000f;
                        Log.i("Rotation", "RIGHT Rotated by " + CanvasActivity.this.rotation);
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        app.bluetooth.send("s");
                        return false;
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

        app.bluetooth.subscribe(new ByteArrayHandler() {
            public void handle(byte[] message) {
                byte code = message[0];
                if(code == 'S') {
                    sweepInProgressDists.clear();
                    sweepInProgressAngles.clear();
                } else if(code == 'V') {
//                    Log.i("CanvasActivity", "Value Message!");
                    if(message.length < 5) {
                        Log.e("CanvasActivity", "Value message too short!");
                        return;
                    }
                    int distance = 0;
                    byte lsb = message[1];
                    byte msb = message[2];
                    distance += (lsb & 0xFF);
                    distance += ((msb << 8) & 0xFF00);
                    sweepInProgressDists.add((float) (distance+8));

                    int steps = 0;
                    lsb = message[3];
                    msb = message[4];
                    steps += (lsb & 0xFF);
                    steps += ((msb << 8) & 0xFF00);
                    float angle = (((float) steps) - 136.0f) * 1.8f * 2.0f * (float) Math.PI / 360.0f;
                    sweepInProgressAngles.add(angle);
                } else {
                    if (code == 'F') {
                        float[] angles = new float[sweepInProgressAngles.size()];
                        float[] dists = new float[sweepInProgressDists.size()];
                        if (angles.length != dists.length) {
                            Log.e("CanvasActivity", "# of angles != # of dists!");
                        }
                        int permission = ActivityCompat.checkSelfPermission(me, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            // We don't have permission so prompt the user
                            ActivityCompat.requestPermissions(
                                    me,
                                    PERMISSIONS_STORAGE,
                                    WRITE_EXTERNAL_STORAGE
                            );
                        }
                        permission = ActivityCompat.checkSelfPermission(me, Manifest.permission.READ_EXTERNAL_STORAGE);
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            // We don't have permission so prompt the user
                            ActivityCompat.requestPermissions(
                                    me,
                                    PERMISSIONS_STORAGE,
                                    READ_EXTERNAL_STORAGE
                            );
                        }

                        Log.i("CanvasActivity", "Storage state: " + Environment.getExternalStorageState());
                        File externalStorage = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                        String currentTime = java.text.DateFormat.getTimeInstance().format(Calendar.getInstance().getTime());
                        if(logDir == null) {
                            logDir = new File(externalStorage.getAbsolutePath() + "/log/session-" + currentTime);
                        }
                        Log.i("CanvasActivity", "External Storage: " + externalStorage.getAbsolutePath() + " exists: " + externalStorage.exists());
                        if(!logDir.exists()) {
                            if(!logDir.mkdirs()) {
                                Log.i("CanvasActivity", "mkdirs fails even though file doesn't exist!");
                            }
                        }
                        currentTime = currentTime.replace(" ", "");
                        File logFile = new File(logDir, "log-" + moveType.get().toString() + "-" + logCount++ + ".csv");
                        Log.i("CanvasActivity", "Log dir: " + logDir.getAbsolutePath() + " exists: " + logDir.exists());
                        if(!logFile.exists()) {
                            try {
//                                logFile.mkdirs();
                                logFile.createNewFile();
                            } catch(Exception e) {
                                Log.e("CanvasActivity", e.getMessage());
                            }
                        }
                        Log.i("CanvasActivity", "Logging to file: " + logFile.getAbsolutePath() + " exists: " + logFile.exists());
                        FileOutputStream fos = null;
                        OutputStreamWriter writer = null;
                        try {
                            fos = new FileOutputStream(logFile);
                            writer = new OutputStreamWriter(fos);
                        } catch (Exception e) {
                            Log.e("CanvasActivity", e.getMessage());
                            e.printStackTrace();
                        }
                        for (int i = 0; i < angles.length; ++i) {
                            angles[i] = sweepInProgressAngles.get(i);
                            dists[i] = sweepInProgressDists.get(i);
//                            Log.i("CanvasActivityData", angles[i] + ", " + dists[i]);
                            try {
                                // Discard any measurements of 8 cm - they are errors
                                if(dists[i] - 8f < 0.5f) {
                                    continue;
                                }
                                if(writer != null) {
                                    writer.write(angles[i] + "," + dists[i] + "\n");
//                                    Log.i("CanvasActivity", "WRITING :: " + angles[i] + "," + dists[i]);
                                }
                            } catch(Exception e) {
                                Log.e("CanvasActivity", "WRITING :: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                        try {
                            if(writer != null) {
                                writer.flush();
                            }
                            if(fos != null) {
                                fos.close();
                            }
//                            if(writer != null) {
//                                writer.close();
//                            }
                        } catch(Exception e) {
                            Log.e("CanvasActivity", "Closing: " + e.getMessage());
                            e.printStackTrace();
                        }
                        if (moveType.get() == ScanType.LINEAR) {
                            Log.i("CanvasActivity", "ScanType LINEAR");
                            map.updateLin(angles, dists);
                        } else if (moveType.get() == ScanType.ROTATION) {
                            Log.i("CanvasActivity", "ScanType ROTATION");
                            // NOTE: 220 degress / second of button hold
                            map.updateRot(angles, dists, CanvasActivity.this.rotation);
                            CanvasActivity.this.rotation = 0;
                        } else if (moveType.get() == ScanType.INITIAL) {
                            Log.i("CanvasActivity", "ScanType INITIAL");
                            map.initialScan(angles, dists);
                            app.mapInitialized = true;
                        }
                        drawView.clear();
                        List<mapping.MapSegment> segments = map.getSegments();
                        int currentSweep = map.getCurrentSweep();
                        for (int i = 0; i < segments.size(); ++i) {
                            mapping.MapSegment segment = segments.get(i);
                            drawView.addLine(new DrawView.Line(
                                    segment.origin.x,
                                    -segment.origin.y,
                                    segment.origin.x + segment.vec.x,
                                    -(segment.origin.y + segment.vec.y)
                            ), segment.created == currentSweep);
                        }
                        mapping.Vec pos = map.getPosition();
                        float robotAngle = map.getAngle();
                        drawView.setRobotAngle(robotAngle);
                        drawView.setPoint(new DrawView.Point(pos.x, -pos.y));
                        for (int i = 0; i < angles.length; ++i) {
                            drawView.addPoint(new DrawView.Point(
                                    (float) Math.cos(robotAngle + angles[i]) * dists[i] + pos.x,
                                    -((float) Math.sin(robotAngle + angles[i]) * dists[i] + pos.y)
                            ));
                        }

                        drawView.invalidate();

                        float dimension = drawView.getMapSize();
                        dimensions.setText("Map Dimensions: " + dimension + "cm X " + dimension + "cm");
                    } else if (code == 'T') {

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
                }
                if(false /* Start of sweep */) {

                } else if(false /* End of sweep */) {

                }
//                if(true)
//                    return;
//
//                if(message.length < 5) {
//                    Log.e("CanvasActivity", "Message too short!");
//                    return;
//                }
//                byte lsb = message[1];
//                byte msb = message[2];
//
//                Log.i("CanvasActivity", "bin lsb: " + String.format("%02X", lsb));
//                Log.i("CanvasActivity", "bin msb: " + String.format("%02X", msb));
//
//                int distance = 0;
//                distance += (lsb & 0xFF);
//                distance += ((msb << 8) & 0xFF00);
//
//                lsb = message[3];
//                msb = message[4];
//
//                int angle = 0;
//                angle += (lsb & 0xFF);
//                angle += ((msb << 8) & 0xFF00);
//
//                float realAngle = ((float) angle) / 10;
//
//                int x = (int) (Math.cos(realAngle) * ((float) distance));
//                int y = (int) (Math.sin(realAngle) * ((float) distance));
//
//                sweepInProgressDists.add((float) distance);
//                sweepInProgressAngles.add(realAngle);
//
//                drawView.clear();
//                drawView.addPoint(new DrawView.Point(-200, -200));
//                drawView.addPoint(new DrawView.Point(200, -200));
//                drawView.addPoint(new DrawView.Point(-200, 200));
//                drawView.addPoint(new DrawView.Point(200, 200));
////                drawView.addLine(new DrawView.Line(0, 0, x, y), false);
//
//                drawView.invalidate();
//
//                Log.i("CanvasActivity", "bin dist: " + String.format("%04X", distance));
//                Log.i("CanvasActivity", "Rotation int: " + angle);
//
//                String output = "LIDAR distance: " + distance + "cm";
//
//                Log.i("CanvasActivity", output);
//
//                lidarDistance.setText(output);
//
//                output = "Rotation: " + realAngle + " deg";
//
//                Log.i("CanvasActivity", output);
//                rotation.setText(output);
            }
        });
    }

}
