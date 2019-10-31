package com.hql.cacheutils.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;

/**
 * @author ly-huangql
 * <br /> Create time : 2019/6/27
 * <br /> Description :
 */
public class ImageClip {
    private final static String TAG = "cacheImageClip";
    public ImageClip() {
    }

    /**
     * 将文件流转换为bitmap
     *
     * @param descriptor
     * @param reqWidth
     * @param reqHeight
     */
    public Bitmap decodeBitmapFromInputStream(FileDescriptor descriptor, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(descriptor, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        //Log.d(TAG,"输出图片大小>>width:"+  options.outWidth/options.inSampleSize+">>>heigh:"+  options.outHeight/options.inSampleSize);
        return BitmapFactory.decodeFileDescriptor(descriptor,null,options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (0 == reqWidth || 0 == reqHeight) {
            return inSampleSize;
        }
        final int height = options.outHeight;
        final int width = options.outWidth;
        //Log.d(TAG,"原图大小>>width:"+  options.outWidth+">>>heigh:"+  options.outHeight);
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            //计算inSampleSize直到缩放后的宽高都小于指定的宽高
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;//官方文档建议inSampleSize取值最好为2的指数
            }
        }
        return inSampleSize;
    }
}
