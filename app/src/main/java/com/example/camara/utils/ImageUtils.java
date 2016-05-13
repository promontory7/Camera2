package com.example.camara.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2016/5/9.
 */
public class ImageUtils {

    //计算图片的缩放值
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
//            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            inSampleSize = heightRatio ;
            if (heightRatio>3){
                inSampleSize--;
            }
            if (heightRatio>6){
                inSampleSize--;
            }
            if (heightRatio>8){
                inSampleSize--;
            }
            Log.e("insampe","    height："+height+"   reqHeight"+reqHeight+"     inSampleSize"+inSampleSize);
        }
        return inSampleSize;
    }

    // 根据路径获得图片并压缩，返回bitmap用于显示
    public static byte[] processBitmapBytesSmaller(byte[] data, int width) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        //图片状态为旋转90
        options.inSampleSize = calculateInSampleSize(options, 750, 450);
        options.inJustDecodeBounds = false;
        Bitmap smallBitmap= adjustPhotoRotation(BitmapFactory.decodeByteArray(data, 0, data.length, options),90);
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        float sacle =((float)smallBitmap.getWidth())/width;
        int height = (int) ((float)smallBitmap.getHeight()/sacle);
        Bitmap.createScaledBitmap(smallBitmap,width,height,true).compress(Bitmap.CompressFormat.JPEG,90,bos);
        return  bos.toByteArray();
    }

    public static Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            Bitmap bm1 = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
            return bm1;
        } catch (OutOfMemoryError ex) {
        }

        return null;
    }
}
