package com.example.camara;

import android.os.AsyncTask;
import android.util.Log;
import android.view.SurfaceView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/5/7.
 */
public class UploadImageTask extends AsyncTask {
    SVDraw surfaceView;
    HttpClient httpClient;
    HttpResponse responseHttpClient;
    HttpEntity entity;
    HttpPost httpPost;
    String url;
    byte[] data;


    public UploadImageTask(String url, byte[] data, SVDraw surfaceView) {
        this.surfaceView = surfaceView;
        this.url = url;
        this.data = data;
        try {
            httpClient = new DefaultHttpClient();
            httpPost = new HttpPost(url);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    protected Object doInBackground(Object[] params) {
        Log.e("up", "shangchuang");
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(data);
        httpPost.setEntity(byteArrayEntity);
        try {
            responseHttpClient = httpClient.execute(httpPost);
            entity = responseHttpClient.getEntity();
            Log.e("result", EntityUtils.toString(entity));
        } catch (Exception e) {
            e.printStackTrace();
        }
       if (entity!=null){
           try {
               JSONObject jsonObject = new JSONObject(entity.toString());
               if (jsonObject != null && jsonObject.has("bounding_rects")) {
                   JSONArray locations = jsonObject.optJSONArray("bounding_rects");
                   ArrayList<LocationBean> locationList = new ArrayList();
                   if (locations != null&&locations.length()>0) {
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
                   return locationList;
               }

           } catch (JSONException e) {
               e.printStackTrace();
           }
       }


        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        surfaceView.clearDraw();
        ArrayList<LocationBean> locationBeanArrayList = (ArrayList<LocationBean>) o;
        if (locationBeanArrayList != null && locationBeanArrayList.size() > 0) {
            for (int i = 0; i < locationBeanArrayList.size(); i++) {
                LocationBean locationBean = locationBeanArrayList.get(i);

                surfaceView.drawlocation(locationBean.getX(), locationBean.getY()
                        , locationBean.getWidth(), locationBean.getHeight());
            }

        }

    }
}
