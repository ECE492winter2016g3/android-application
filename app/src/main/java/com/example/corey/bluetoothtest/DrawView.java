package com.example.corey.bluetoothtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corey on 04/03/2016.
 */
public class DrawView extends View {
    Point mainPoint;
    float robotAngle = 0.0f;
    public void setRobotAngle(float angle) {
        robotAngle = angle;
    }
    public float getMapSize() {
        return xScale.domainMax - xScale.domainMin;
    }
    public void init() {
        mainPoint = new Point(0,0);
        points = new ArrayList<>();
        oldLines = new ArrayList<>();
        newLines = new ArrayList<>();
        xScale = new LinearScale();
        yScale = xScale;
//
//        addPoint(new Point(150, 110));
//        addPoint(new Point(320, 90));
//        addPoint(new Point(210, 195));
//        addPoint(new Point(260, 408));
//        addPoint(new Point(51, 280));
//        addPoint(new Point(112, 495));
//
//        addLine(new Line(100, 100, 400, 400));
//        addLine(new Line(400, 400, 100, 400));
//        addLine(new Line(100, 400, 400, 100));
//        addLine(new Line(400, 100, 100, 100));
    }
    private Paint paint = new Paint();

    public static class Point {
        public Point(float xi, float yi) {
            x = xi;
            y = yi;
        }
        public float x;
        public float y;
    }
    public static class Line {
        public Line(float x1i, float y1i, float x2i, float y2i) {
            x1 = x1i;
            y1 = y1i;
            x2 = x2i;
            y2 = y2i;
        }
        public float x1;
        public float y1;
        public float x2;
        public float y2;
    }

    public class LinearScale {
        public void setDomain(float min, float max) {
            domainMin = min;
            domainMax = max;
        }
        public void setRange(float min, float max) {
            rangeMin = min;
            rangeMax = max;
        }
        public float scale(float input) {
            return ((input - domainMin) / (domainMax - domainMin)) * (rangeMax - rangeMin) + rangeMin;
        }
        public void reset() {
            domainMin = 0;
            domainMax = 1;
            rangeMin = 0;
            rangeMax = 1;
        }
        public float domainMin = 0;
        public float domainMax = 1;
        public float rangeMin = 0;
        public float rangeMax = 1;
    }

    private List<Point> points;
    private List<Line> oldLines;
    private List<Line> newLines;
    private LinearScale xScale;
    private LinearScale yScale;

    public DrawView(Context context) {
        super(context);
        init();
    }
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public void setPoint(Point point) {
        mainPoint = point;
        if(point.x < xScale.domainMin) {
            xScale.domainMin = point.x;
        }
        if(point.x > xScale.domainMax) {
            xScale.domainMax = point.x;
        }
        if(point.y < yScale.domainMin) {
            yScale.domainMin = point.y;
        }
        if(point.y > yScale.domainMax) {
            yScale.domainMax = point.y;
        }
    }

    public void addPoint(Point point) {
        points.add(point);
        if(point.x < xScale.domainMin) {
            xScale.domainMin = point.x;
        }
        if(point.x > xScale.domainMax) {
            xScale.domainMax = point.x;
        }
        if(point.y < yScale.domainMin) {
            yScale.domainMin = point.y;
        }
        if(point.y > yScale.domainMax) {
            yScale.domainMax = point.y;
        }
    }
    public void addLine(Line line, boolean isNew) {
        if(isNew) {
            newLines.add(line);
        } else {
            oldLines.add(line);
        }

        if(line.x1 < xScale.domainMin) {
            xScale.domainMin = line.x1;
        }
        if(line.x1 > xScale.domainMax) {
            xScale.domainMax = line.x1;
        }
        if(line.y1 < yScale.domainMin) {
            yScale.domainMin = line.y1;
        }
        if(line.y1 > yScale.domainMax) {
            yScale.domainMax = line.y1;
        }
        if(line.x2 < xScale.domainMin) {
            xScale.domainMin = line.x2;
        }
        if(line.x2 > xScale.domainMax) {
            xScale.domainMax = line.x2;
        }
        if(line.y2 < yScale.domainMin) {
            yScale.domainMin = line.y2;
        }
        if(line.y2 > yScale.domainMax) {
            yScale.domainMax = line.y2;
        }
    }

    public void clear() {
        mainPoint = new Point(0, 0);
        points.clear();
        oldLines.clear();
        newLines.clear();
        xScale.reset();
        yScale.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        addPoint(new Point(x, y));

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        float margin = 100;
        xScale.setRange(margin, w - margin);
        yScale.setRange(margin, h - margin);

//        paint.setColor(Color.BLACK);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(3);
//        canvas.drawRect(0 + 10, 0 + 10, w - 10, h - 10, paint);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeWidth(0);
//        paint.setColor(Color.CYAN);
//        canvas.drawRect(33, 60, 77, 77, paint);
//        paint.setColor(Color.YELLOW);
//        canvas.drawRect(33, 33, 77, 60, paint);

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        paint.setColor(Color.BLUE);
        for(Point p : points) {
            Log.i("DrawView", "Drawing point at " + xScale.scale(p.x) + ", " + yScale.scale(p.y));
            canvas.drawCircle(
                    xScale.scale(p.x),
                    yScale.scale(p.y),
                    2,
                    paint);
        }

        paint.setColor(Color.DKGRAY);
        for(Line l : oldLines) {
            canvas.drawLine(
                    xScale.scale(l.x1),
                    yScale.scale(l.y1),
                    xScale.scale(l.x2),
                    yScale.scale(l.y2),
                    paint);
        }
        paint.setColor(Color.RED);
        paint.setStrokeWidth(4);
        for(Line l : newLines) {
            canvas.drawLine(
                    xScale.scale(l.x1),
                    yScale.scale(l.y1),
                    xScale.scale(l.x2),
                    yScale.scale(l.y2),
                    paint);
        }
        paint.setColor(Color.BLACK);
        canvas.drawCircle(
                xScale.scale(mainPoint.x),
                yScale.scale(mainPoint.y),
                8,
                paint);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        canvas.drawLine(
                xScale.scale(mainPoint.x),
                xScale.scale(mainPoint.y),
                xScale.scale(mainPoint.x) + 50 * (float) Math.cos(robotAngle),
                xScale.scale(mainPoint.y) - 50 * (float) Math.sin(robotAngle),
                paint);
        paint.setColor(Color.BLACK);
        canvas.drawLine(
                0,
                h,
                xScale.scale(100) - xScale.scale(0),
                h,
                paint);
        canvas.drawLine(
                0,
                h,
                0,
                h - 5,
                paint);
        canvas.drawLine(
                xScale.scale(100) - xScale.scale(0),
                h,
                xScale.scale(100) - xScale.scale(0),
                h - 5,
                paint);
    }

}
