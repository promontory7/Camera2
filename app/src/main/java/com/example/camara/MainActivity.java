package com.example.camara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.camara.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    static final int WRITE_STORAGE = 2;

    public static final String TAG = "MainActivity";
    //宽度450

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
                surface_tip.drawLine();
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
        Camera.Parameters params = camera.getParameters();
        params.setPictureFormat(PixelFormat.JPEG);
        camera.setParameters(params);
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }


    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

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
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

//            new UploadImageTask("http://192.168.1.136:4212/index/searcher", data).execute();
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
