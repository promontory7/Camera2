package com.example.camara;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {


    Camera camera;
    SurfaceHolder holder;
    SurfaceView surface_camera;
    SVDraw tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        holder = surface_camera.getHolder();
        holder.setKeepScreenOn(true);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(this);

        tip.drawLine();

    }

    private void initView() {
        surface_camera = (SurfaceView) findViewById(R.id.surface_camera);
        tip = (SVDraw) findViewById(R.id.surface_tip);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null) {
            camera = Camera.open();
            try {
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFocusMode("auto");
        camera.setParameters(parameters);
        camera.startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }


}
