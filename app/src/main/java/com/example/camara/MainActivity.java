package com.example.camara;

import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.example.camara.utils.Constants;
import com.example.camara.utils.ImageUtils;
import com.zhuchudong.toollibrary.AppUtils;
import com.zhuchudong.toollibrary.L;
import com.zhuchudong.toollibrary.StatusBarUtil;
import com.zhuchudong.toollibrary.ToastUtils;
import com.zhuchudong.toollibrary.okHttpUtils.OkHttpUtils;
import com.zhuchudong.toollibrary.okHttpUtils.callback.JsonCallBack;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    //宽度450
    TimerTask task;

    long netTime;
    long processTime;
    private Timer timer;
    Camera camera;
    SurfaceHolder holder;
    SurfaceView surface_camera;
    SVDraw surface_tip;
    TextView timeView;
    StringBuffer tv_string =new StringBuffer();

    public int screenOritation = 60;

    Camera.PictureCallback currentCallBack;
    public OrientationEventListener mOrientationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        holder = surface_camera.getHolder();
        holder.setKeepScreenOn(true);
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        initOrientationListener();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (timer == null) {
            timer = new Timer();
            initSchedule();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        OkHttpUtils.getInstance().getOkhttpClient().dispatcher().cancelAll();
        L.e("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }
    }

    private void initView() {
        StatusBarUtil.setColor(MainActivity.this, getResources().getColor(R.color.colorPrimary));
        surface_camera = (SurfaceView) findViewById(R.id.surface_camera);
        surface_tip = (SVDraw) findViewById(R.id.surface_tip);
        timeView= (TextView) findViewById(R.id.tv_time);

    }

    public void initSchedule() {
        task = new TimerTask() {

            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (camera != null) {
                            processTime=System.currentTimeMillis();
                            camera.takePicture(null, null, new FirstCallback());
                        }
                    }
                });
            }
        };
        if (timer == null) {
            timer = new Timer();
        }
//        timer.schedule(task, 2000, 2000);
        timer.schedule(task, 2000);
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

        List<Camera.Size> previewsizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> picturesizes = parameters.getSupportedPictureSizes();


        Camera.Size previewMaxSize = getMaxSize(previewsizes);
        if (previewMaxSize != null) {
            parameters.setPreviewSize(previewMaxSize.width, previewMaxSize.height);
            L.e("setPreviewSize  " + previewMaxSize.width + "   " + previewMaxSize.height);

        } else {
            L.e("setPreviewSize    null");
        }


        Camera.Size pictureMaxSize = getMaxSize(picturesizes, (float) previewMaxSize.width / (float) previewMaxSize.height);
        if (pictureMaxSize != null) {
            parameters.setPictureSize(pictureMaxSize.width, pictureMaxSize.height);
            L.e("setPictureSize   " + pictureMaxSize.width + "   " + pictureMaxSize.height);
        } else {
            L.e("setPictureSize    null");

        }


        if (previewsizes != null && previewsizes.size() > 0) {
            for (int i = 0; i < previewsizes.size(); i++) {
                Camera.Size size = previewsizes.get(i);
                L.i("previewsizes " + size.width + "  " + size.height);
            }
        }

        if (picturesizes != null && picturesizes.size() > 0) {
            for (int i = 0; i < picturesizes.size(); i++) {
                Camera.Size size = picturesizes.get(i);
                L.i("picturesizes " + size.width + "  " + size.height);
            }
        }

        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//连续对焦
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
        camera.startPreview();
        camera.cancelAutoFocus();
    }


    public Camera.Size getMaxSize(List<Camera.Size> arrayList) {
        if (arrayList != null && arrayList.size() > 0) {
            Camera.Size maxSize = arrayList.get(0);
            for (int i = 1; i < arrayList.size(); i++) {
                if ((arrayList.get(i).width + arrayList.get(i).height) > (maxSize.width + maxSize.height)) {
                    maxSize = arrayList.get(i);
                }
            }
            return maxSize;
        } else {
            return null;
        }
    }

    public Camera.Size getMaxSize(List<Camera.Size> arrayList, float scare) {
        if (arrayList != null && arrayList.size() > 0) {
            Camera.Size maxSize = arrayList.get(arrayList.size() / 2);
            for (int i = 0; i < arrayList.size(); i++) {
                if (((arrayList.get(i).width + arrayList.get(i).height) > (maxSize.width + maxSize.height) &&
                        ((float) arrayList.get(i).width / (float) arrayList.get(i).height) == scare)) {
                    maxSize = arrayList.get(i);
                }
            }
            return maxSize;
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View v) {

    }

    private final class FirstCallback implements Camera.PictureCallback {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.startPreview();
            byte[] compressDada = ImageUtils.processBitmapBytesSmaller2(data, Constants.requestWidth, screenOritation);
//            new UploadImageTask(Constants.url, compressDada, surface_tip, screenOritation).execute();
            tv_string.append("处理用时："+(System.currentTimeMillis() - processTime) + "ms").append("\n");
            timeView.setText(tv_string);
            netTime = System.currentTimeMillis();
            OkHttpUtils.postBytes().url(Constants.url).data(compressDada).build().connTimeOut(5000).enqueue(firstcallback);
        }
    }


    private JsonCallBack firstcallback = new JsonCallBack() {
        @Override
        public void onError(Call call, Exception e) {
            if (!AppUtils.isTopActivity(MainActivity.this, "com.example.camara.MainActivity")) {
                L.e("isTopActivity==false");
                return;
            }
            tv_string.append("请求用时："+(System.currentTimeMillis() - netTime) + "ms").append("\n").append("\n");
            timeView.setText(tv_string);
            L.e(System.currentTimeMillis() - netTime + "ms");
            L.e("网络请求出错 " + e.toString());
            ToastUtils.showToast(MainActivity.this, "网络请求出错 " + (System.currentTimeMillis() - netTime) + "ms");
            netTime = System.currentTimeMillis();
            processTime=System.currentTimeMillis();
            camera.takePicture(null, null, new FirstCallback());
        }

        @Override
        public void onResponse(JSONObject response) {
            if (!AppUtils.isTopActivity(MainActivity.this, "com.example.camara.MainActivity")) {
                L.e("isTopActivity==false");
                return;
            }
            tv_string.append("请求用时："+(System.currentTimeMillis() - netTime) + "ms").append("\\n");
            timeView.setText(tv_string);
            L.e(System.currentTimeMillis() - netTime + "");
            netTime = System.currentTimeMillis();
            if (response != null && response.has("bounding_rects")) {
                JSONArray locations = response.optJSONArray("bounding_rects");
                ArrayList<LocationBean> locationList = new ArrayList();
                if (locations != null && locations.length() > 0) {
                    for (int i = 0; i < locations.length(); i++) {
                        JSONObject locationJson = locations.optJSONObject(i);
                        LocationBean locationBean = new LocationBean();

                        locationBean.setX(locationJson.optInt("x"));
                        locationBean.setY(locationJson.optInt("y"));
                        locationBean.setWidth(locationJson.optInt("width"));
                        locationBean.setHeight(locationJson.optInt("height"));

                        locationList.add(locationBean);
                    }
                }
                if (locationList != null && locationList.size() > 0) {
                    for (int i = 0; i < locationList.size(); i++) {
                        LocationBean locationBean = locationList.get(i);
                        L.e("locationBean  " + i + "   " + locationBean.toString());
                    }
                }
                surface_tip.drawlocation(locationList, screenOritation);
            }
            processTime=System.currentTimeMillis();
            camera.takePicture(null, null, new FirstCallback());
        }
    };


    private void initOrientationListener() {
        mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {

            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == -1) {
                } else if (orientation < 45 || orientation > 315) {
                    screenOritation = Constants.TOP;
                } else if (orientation < 135 && orientation > 45) {
                    screenOritation = Constants.LEFT;
                } else if (orientation < 225 && orientation > 135) {
                    screenOritation = Constants.BOTTOM;
                } else if (orientation < 315 && orientation > 225) {
                    screenOritation = Constants.RIGHT;
                } else {
                }
            }
        };

        if (mOrientationListener.canDetectOrientation() == true) {
            mOrientationListener.enable();
        } else {
            L.e("Cannot detect orientation");
            mOrientationListener.disable();
        }
    }


}
