package com.example.corey.bluetoothtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corey on 04/03/2016.
 */
public class DrawView extends View {
    {
        pts = new ArrayList<>();
        addPt(new Pt(100, 100));
        addPt(new Pt(300, 100));
        addPt(new Pt(200, 200));
        addPt(new Pt(200, 400));
        addPt(new Pt(50, 200));
        addPt(new Pt(100, 100));
    }
    private Paint paint = new Paint();

    public class Pt {
        public Pt(float xi, float yi) {
            x = xi;
            y = yi;
        }
        public float x;
        public float y;
    }

    private List<Pt> pts;

    public DrawView(Context context) {
        super(context);
    }
    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void addPt(Pt pt) {
        pts.add(pt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = e.getX();
        float y = e.getY();

        addPt(new Pt(x, y));

        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRect(0 + 10, 0 + 10, w - 10, h - 10, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        paint.setColor(Color.CYAN);
        canvas.drawRect(33, 60, 77, 77, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawRect(33, 33, 77, 60, paint);

        paint.setColor(Color.BLACK);
        if(pts.size() > 0) {
            Pt p1;
            Pt p2;
            for(int i = 0; i < pts.size() - 1; ++i) {
                p1 = pts.get(i);
                p2 = pts.get(i+1);

                canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
            }
            p1 = pts.get(0);
            p2 = pts.get(pts.size()-1);
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint);
        }
    }

}
