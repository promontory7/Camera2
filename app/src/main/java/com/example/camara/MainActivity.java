package com.example.camara;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.example.camara.utils.ImageUtils;
import com.example.camara.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    static final int WRITE_STORAGE = 2;

    public static final String TAG = "MainActivity";
    //宽度450
    TimerTask task;

    private Timer timer ;
    Camera camera;
    SurfaceHolder holder;
    SurfaceView surface_camera;
    SVDraw surface_tip;
    Button take_picture;
    Button draw_tip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        holder = surface_camera.getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(this);
        holder.setFixedSize(450, 600);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        timer = new Timer();
        initSchedule();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();

    }


    public void initSchedule(){
        task = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (camera != null) {
                            camera.takePicture(null, null, new MyPictureCallback());
                        }
                    }
                });
            }
        };
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(task, 1000, 2000);
    }


    private void initView() {
        surface_camera = (SurfaceView) findViewById(R.id.surface_camera);
        surface_tip = (SVDraw) findViewById(R.id.surface_tip);
        take_picture = (Button) findViewById(R.id.btn_takepicture);
        draw_tip = (Button) findViewById(R.id.btn_drawtip);

        take_picture.setOnClickListener(this);
        draw_tip.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_takepicture:
                camera.takePicture(null, null, new MyPictureCallback());

                break;
            case R.id.btn_drawtip:
                surface_tip.clearDraw();
                break;
            default:
                break;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (camera == null) {
            camera = Camera.open();
            try {

                camera.setPreviewDisplay(holder);
                initCamera();
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera();
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private void initCamera() {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
        camera.startPreview();
        camera.cancelAutoFocus();
    }


    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            byte[] compressDada = ImageUtils.processBitmapBytesSmaller(data, 450);

            File pictureFile = Utils.getOutputMediaFile(MainActivity.this, Utils.MEDIA_TYPE_IMAGE);
            if (!pictureFile.exists()) {
                try {
                    pictureFile.createNewFile();
                    Log.e(TAG, "图片文件创建成功");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "图片文件创建失败");
                }
            }
            if (pictureFile == null) {
                Log.e(TAG, "图片文件为空");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);

                fos.write(compressDada);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            float xscare = (float) surface_tip.getWidth() / 450;
//            float yscare = (float) surface_tip.getHeight() / 750;
//            Log.e("surface_tip", xscare + "    " + yscare);
//
//            ArrayList<LocationBean> locationBeanArrayList = new ArrayList<>();
//            LocationBean locationBean1 = new LocationBean((int)(144*xscare), (int)(82*yscare), (int)(176*xscare), (int)(146*yscare));
//            LocationBean locationBean2 = new LocationBean((int)(94*xscare), (int)(375*yscare), (int)(102*xscare), (int)(61*yscare));
//
//            locationBeanArrayList.add(locationBean1);
//            locationBeanArrayList.add(locationBean2);
//            surface_tip.drawlocation(locationBeanArrayList);
            new UploadImageTask("http://192.168.1.136:4212/index/searcher", compressDada,surface_tip).execute();
            camera.startPreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_STORAGE) {

        }

    }

}
