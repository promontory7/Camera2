package com.example.camara;

import android.os.AsyncTask;
import android.util.Log;

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

import java.io.IOException;

/**
 * Created by Administrator on 2016/5/7.
 */
public class UploadImageTask extends AsyncTask {
    HttpClient httpClient;
    HttpResponse responseHttpClient;
    HttpEntity entity ;
    HttpPost httpPost;
    String url;
    byte[] data;
    public UploadImageTask(String url,byte[] data) {
        this.url=url;
        this.data=data;
        try {
            httpClient= new DefaultHttpClient();
            httpPost=new HttpPost(url);
        }catch (Exception e){
            e.printStackTrace();
        }


    }

    @Override
    protected Object doInBackground(Object[] params) {
        Log.e("up","shangchuang");
        ByteArrayEntity byteArrayEntity = new ByteArrayEntity(data);
        httpPost.setEntity(byteArrayEntity);
        try {
            responseHttpClient = httpClient.execute(httpPost);
            entity = responseHttpClient.getEntity();
            Log.e("result",EntityUtils.toString(entity));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return entity.toString();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
    }
}
