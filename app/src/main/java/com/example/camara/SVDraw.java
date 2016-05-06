package com.example.camara;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Administrator on 2016/5/6.
 */
public class SVDraw extends SurfaceView implements SurfaceHolder.Callback {
    public SurfaceHolder mSurfaceHolder;
    private int mWidth;
    private int mHeight;

    public SVDraw(Context context, AttributeSet attrs) {
        super(context, attrs);
        mSurfaceHolder =getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
    }

    public SVDraw(Context context) {
        super(context);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mWidth=width;
        mHeight=height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
    public void drawLine(){
        Canvas canvas =mSurfaceHolder.lockCanvas();
        canvas.drawColor(Color.TRANSPARENT);

        Paint mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(200,200,20,mPaint);
        canvas.drawLine(20,100,500,300,mPaint);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }

    public void clearDraw(){
        Canvas canvas= mSurfaceHolder.lockCanvas();
        canvas.drawColor(Color.BLUE);
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }
}
