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

import com.example.camara.utils.Constants;
import com.example.camara.utils.ImageUtils;
import com.example.camara.utils.Utils;
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

public class TakePhotoTest extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {
    //宽度450

    Camera camera;
    SurfaceHolder holder;
    SurfaceView surface_camera;
    SVDraw surface_tip;
    public int screenOritation = 60;
    long currentTime;

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
        currentCallBack = new SecondCallback();
        findViewById(R.id.btn_linearlayout).setVisibility(View.VISIBLE);
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

        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }
    }

    private void initView() {
        StatusBarUtil.setColor(TakePhotoTest.this, getResources().getColor(R.color.colorPrimary));
        findViewById(R.id.btn_linearlayout).setVisibility(View.VISIBLE);
        surface_camera = (SurfaceView) findViewById(R.id.surface_camera);
        surface_tip = (SVDraw) findViewById(R.id.surface_tip);
        findViewById(R.id.btn_takepicture).setOnClickListener(this);
        findViewById(R.id.btn_again).setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_takepicture:
                camera.takePicture(null, null, currentCallBack);
//                ArrayList<LocationBean> arrayList =new ArrayList<LocationBean>();
//                arrayList.add(new LocationBean(10,10,400,400));
//                surface_tip.drawlocation(arrayList,screenOritation);
                break;
            case R.id.btn_again:
                surface_tip.clearDraw();
                camera.startPreview();
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



    private final class SecondCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            camera.stopPreview();
            byte[] compressDada = ImageUtils.processBitmapBytesSmaller2(data, Constants.requestWidth, screenOritation);

            Utils.savepicture(TakePhotoTest.this, compressDada);
//            new UploadImageTask(Constants.url, compressDada, surface_tip, screenOritation).execute();
            currentTime=System.currentTimeMillis();
            OkHttpUtils.postBytes().url(Constants.url).data(compressDada).build().connTimeOut(5000).enqueue(secondCallback);
        }
    }


    private JsonCallBack secondCallback = new JsonCallBack() {
        @Override
        public void onError(Call call, Exception e) {
            if (!AppUtils.isTopActivity(TakePhotoTest.this, "com.example.camara.TakePhotoTest")) {
                L.e("isTopActivity==false");
                return;
            }
            L.e(System.currentTimeMillis() - currentTime + "");
            currentTime = System.currentTimeMillis();
            ToastUtils.showToast(TakePhotoTest.this, "网络请求出错 " + e.getMessage());
            camera.takePicture(null, null, new SecondCallback());
        }

        @Override
        public void onResponse(JSONObject response) {
            if (!AppUtils.isTopActivity(TakePhotoTest.this, "com.example.camara.TakePhotoTest")) {
                L.e("isTopActivity==false");
                return;
            }
            L.e(System.currentTimeMillis() - currentTime + "");
            currentTime = System.currentTimeMillis();

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
            camera.takePicture(null, null, new SecondCallback());
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
