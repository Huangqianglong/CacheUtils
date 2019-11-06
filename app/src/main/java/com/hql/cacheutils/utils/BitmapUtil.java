package com.hql.cacheutils.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.hql.cacheutils.R;

/**
 * @author ly-huangql
 * <br /> Create time : 2019/8/5
 * <br /> Description :
 */
public class BitmapUtil {
    public static Bitmap getMusicPic(byte[] pic, int reqWidth, int reqHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(pic, 0, pic.length, options);

        int sampleHeight = Math.round((float) options.outHeight / (float) reqHeight);
        int sampleWidth = Math.round((float) options.outWidth / (float) reqHeight);

        int sampleSize = sampleHeight < sampleWidth ? sampleHeight : sampleWidth;

        //无须用采样率处理
        if (sampleSize < 1) {
            sampleSize = 1;
        }

        options.inJustDecodeBounds = false;
        //设置图片采样率
        options.inSampleSize = sampleSize;
        //设置图片解码格式
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeByteArray(pic, 0, pic.length, options);
    }

    // 圓角
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
        Bitmap roundBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(roundBitmap);
        int color = 0xff424242;
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        //float roundPx = 150;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return roundBitmap;
    }

    public static Bitmap drawable2Bitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            Bitmap bitmap = Bitmap
                    .createBitmap(
                            drawable.getIntrinsicWidth(),
                            drawable.getIntrinsicHeight(),
                            drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                                    : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    public static Drawable bitmap2Drawable(Bitmap bitmap) {
        return new BitmapDrawable(bitmap);
    }


    public static Bitmap getFileBitmap(Context context, String path, int requestSize) {
        Bitmap result = null;
        MediaMetadataRetriever metadataRetriever = null;
        try {
            if (!TextUtils.isEmpty(path)) {
                metadataRetriever = new MediaMetadataRetriever();
                metadataRetriever.setDataSource(path);
                metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);

                byte[] bos = metadataRetriever.getEmbeddedPicture();
                if (!TextUtils.isEmpty(bos)) {
                    result = getMusicPic(bos, requestSize, requestSize);
                } else {
                    result = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                }
            } else {

                result = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
                Log.e("BitmapUtil", "getFileBitmap getFileBitmap  获取默认图片  : "+result );
            }

            //LogUtil.d("BitmapUtil", "getFileBitmap  >>>>>>>result:" + result);
        } catch (Exception e) {
            if (null != metadataRetriever) {
                metadataRetriever.release();
                metadataRetriever = null;
            }
            Log.e("BitmapUtil", "onBindViewHolder metadataRetriever erro: " + e.toString());
        } finally {
            if (null != metadataRetriever) {
                metadataRetriever.release();
                metadataRetriever = null;
                return result;
            }
        }
        Log.e("BitmapUtil", "getFileBitmap result  : "+result );
        return result;
    }


    /**
     * 得到图片处理的option
     *
     * @param filePath
     * @return
     */
    public static BitmapFactory.Options getBitMapOption(String filePath) {
        //获取Options对象
        BitmapFactory.Options options = new BitmapFactory.Options();
        //仅做解码处理，不加载到内存
        //
        options.inJustDecodeBounds = true; //解析文件
        BitmapFactory.decodeFile(filePath, options);
        return options;
    }
}
