package com.hql.cacheutils.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.ImageView;

import com.hql.cacheutils.R;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ly-huangql
 * <br /> Create time : 2019/6/26
 * <br /> Description :
 */
public class Loader {
    private Context mContext;
    private final static String TAG = "CacheLoader";
    private ImageMemoryCache mImageMemoryCache;//内存管理
    private ImageDiskCache mImageDiskCache;//磁盘管理
    private int defaultBitmap = -1;
    private boolean saveBlur = true;
    /**
     * 主线程handler,不要用来处理非UI线程的工作
     */
    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //Log.d(TAG, "imageView handle msg");
            switch (msg.what) {
                case ACTION_UPDATE_BITMAP:
                    LoaderResult result = (LoaderResult) msg.obj;
                    //Log.d(TAG, "ACTION_UPDATE_BITMAP >>>>>>>" + result);
                    if (result.imageView.getTag(R.id.bitmap_path).equals(result.getUriTag())) {
                        // Log.d(TAG, "ACTION_UPDATE_BITMAP >>>>>>>result.bitmap:" + result.bitmap);
                        if (null != result.bitmap) {
                            result.imageView.setImageBitmap(result.bitmap);
                        } else {
                            result.imageView.setImageBitmap(null);
                            if (-1 != defaultBitmap) {
                                result.imageView.setImageResource(defaultBitmap);
                            } else {
                                result.imageView.setImageResource(R.drawable.ic_launcher_background);
                            }
                        }
                    } else {
                        Log.d(TAG, "imageView 已变更，不更新图片");
                    }

                    break;
                default:
                    break;
            }
        }
    };

    private final static int CPU_COUNT = Runtime.getRuntime().availableProcessors();//cpu核心数
    private final static int CORE_POOL_SIZE = CPU_COUNT;
    private final static int MAX_POOL_SIZE = CPU_COUNT * 2;
    private final static int KEEP_ALIVE = 8;
    private final static int ACTION_UPDATE_BITMAP = 0;

    private static final ThreadFactory mTHREAD_FACTORY = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r, "Loader:" + mCount.getAndIncrement());
        }
    };
    private Executor THERAD_POOL = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(),
            mTHREAD_FACTORY
    );

    public static Loader build(Context context) {
        return new Loader(context);
    }

    public Loader(Context context) {
        mContext = context;
        mImageMemoryCache = new ImageMemoryCache(mContext);
        mImageDiskCache = new ImageDiskCache(mContext);
    }
    //-w音频部分-----------------------------------

    /**
     * 从磁盘读取mp3并设置图片
     *
     * @param path
     * @param view
     * @param width
     * @param height
     */
    public void bindBitmapFromLocal(final String path, final ImageView view, final int width, final int height, boolean blur) {
        //Log.d(TAG, "读取本地图片");
       /* Bitmap bitmap = getBitmapURL(path, width, height);
        if (null != bitmap) {
            Log.d(TAG, "读取本地图片 从缓存获取音频专辑");
            view.setImageBitmap(bitmap);
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "读取本地图片 没有缓存");
                Bitmap bitmap = mImageDiskCache.localPicIntoDisk(path, width, height);
                if (saveBlur) {
                    Bitmap bitmap1 = BlurUti.fastBlueBlur(mContext, bitmap, 13);
                    mImageDiskCache.saveBlurBitmapIntoDisk(path + ImageDiskCache.BLUR_TAG, bitmap1);
                    putIntoMenmery(path + ImageDiskCache.BLUR_TAG + width + height, bitmap);
                }

                LoaderResult result = new LoaderResult(view, bitmap, path);
                mainHandler.obtainMessage(ACTION_UPDATE_BITMAP, result).sendToTarget();
                if (null != bitmap) {
                    putIntoMenmery(path + width + height, bitmap);
                }
            }
        };
        THERAD_POOL.execute(runnable);*/
        if (!bindBitmap(path, view, width, height, blur)) {
            runTHERAD_POOL(path, view, width, height, blur, TYPE_PIC);
        }
    }
    //-w音频部分-----------------------------------

    /**
     * 从磁盘读取mp3并设置图片
     *
     * @param path
     * @param view
     * @param width
     * @param height
     */
    public void bindBitmapFromMedia(final String path, final ImageView view, final int width, final int height, final boolean blur) {
        //Log.d(TAG, "读取多媒体专辑图");
       /* Bitmap bitmap = null;
        if (blur) {
            Log.d(TAG, "获取虚化图片");
            bitmap = getBitmapURL(path + ImageDiskCache.BLUR_TAG, width, height);
        } else {
            bitmap = getBitmapURL(path, width, height);
        }
        if (null != bitmap) {
            Log.d(TAG, "从缓存获取音频专辑");
            view.setImageBitmap(bitmap);
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "没有缓存 ，读取多媒体专辑图");
                Bitmap bitmap = mImageDiskCache.mediaAlbumIntoDisk(path, width, height);
                if (saveBlur) {
                    Bitmap bitmap1 = BlurUti.fastBlueBlur(mContext, bitmap, 13);
                    mImageDiskCache.saveBlurBitmapIntoDisk(path + ImageDiskCache.BLUR_TAG, bitmap1);
                    putIntoMenmery(path + ImageDiskCache.BLUR_TAG + width + height, bitmap);
                }
                LoaderResult result = new LoaderResult(view, bitmap, path);
                mainHandler.obtainMessage(ACTION_UPDATE_BITMAP, result).sendToTarget();
                if (null != bitmap) {
                    putIntoMenmery(path + width + height, bitmap);
                }
            }
        };
        THERAD_POOL.execute(runnable);*/
        Log.d(TAG, "读取音频文件缓存");
        if (!bindBitmap(path, view, width, height, blur)) {
            Log.d(TAG, "没有读取音频文件缓存");
            runTHERAD_POOL(path, view, width, height, blur, TYPE_MEDIA);
        }
    }


    //-w网络部分-----------------------------------

    /**
     * 从网络读取并设置图片
     *
     * @param url
     * @param view
     * @param width
     * @param height
     */
    public void bindBitmapFromURL(final String url, final ImageView view, final int width, final int height, final boolean blur) {
        if (!bindBitmap(url, view, width, height, blur)) {
            runTHERAD_POOL(url, view, width, height, blur, TYPE_NET);
        }


    }

    private boolean bindBitmap(String url, ImageView view, int width, final int height, boolean blur) {
        Bitmap bitmap = null;
        view.setTag(R.id.bitmap_path, url);
        if (blur) {
            Log.d(TAG, "获取虚化图片");
            bitmap = getBitmapURL(url + ImageDiskCache.BLUR_TAG, width, height);
        } else {
            bitmap = getBitmapURL(url, width, height);
        }

        if (null != bitmap) {
            view.setImageBitmap(bitmap);
            return true;
        }
        return false;
    }

    private final static int TYPE_NET = 0;
    private final static int TYPE_MEDIA = 1;
    private final static int TYPE_PIC = 2;

    /**
     * 线程池处理网络\音频文件\本地图片
     *
     * @param path
     * @param view
     * @param width
     * @param height
     * @param blur
     */
    private void runTHERAD_POOL(final String path, final ImageView view, final int width, final int height, final boolean blur, final int type) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                switch (type) {
                    case TYPE_NET:
                        Log.d(TAG, "从网络读取");
                        bitmap = mImageDiskCache.downloadIntoDisk(path, width, height, blur);
                        break;
                    case TYPE_MEDIA:
                        Log.d(TAG, "从音频文件读取");
                        bitmap = mImageDiskCache.mediaAlbumIntoDisk(path, width, height, blur);
                        break;
                    case TYPE_PIC:
                        Log.d(TAG, "从本地图片读取");
                        bitmap = mImageDiskCache.localPicIntoDisk(path, width, height, blur);
                        break;
                    default:
                        break;
                }


                Bitmap bitmapBlur = null;
                if (blur) {
                    if (null != bitmap) {
                        bitmapBlur = BlurUti.fastBlueBlur(mContext, bitmap, 13);
                    }
                }
                //处理显示图片
                LoaderResult result = null;
                if (blur) {
                    result = new LoaderResult(view, bitmapBlur, path);
                    if (null != bitmapBlur) {
                        putIntoMenmery(path + width + height, bitmapBlur);
                    }
                } else {
                    result = new LoaderResult(view, bitmap, path);
                    //保存缓存
                    if (null != bitmap) {
                        putIntoMenmery(path + width + height, bitmap);
                    }
                }
                mainHandler.obtainMessage(ACTION_UPDATE_BITMAP, result).sendToTarget();


                //保存虚化图片到磁盘
               /* if (saveBlur) {
                    if (null != bitmap) {
                        if (null == bitmapBlur) {
                            bitmapBlur = BlurUti.fastBlueBlur(mContext, bitmap, 13);
                        }
                        mImageDiskCache.saveBlurBitmapIntoDisk(path + ImageDiskCache.BLUR_TAG + ImageDiskCache.BLUR_TAG, bitmapBlur);
                    }
                }*/
            }
        };
        THERAD_POOL.execute(runnable);
    }

    /**
     * 尝试从内存和磁盘获取图片
     *
     * @param
     */
    private Bitmap getBitmapURL(String path, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        Log.d(TAG, "获取图片：" + path + ">>" + reqWidth + reqHeight);
        bitmap = mImageMemoryCache.getFromMemory(path + reqWidth + reqHeight);//从缓存中获取对应宽高的图片
        if (null == bitmap) {
            bitmap = mImageDiskCache.getBitmapFromDisk(path, reqWidth, reqHeight);
            if (null != bitmap) {
                Log.d(TAG, "内存没有读到，从磁盘读取到,并存入内存");
                //mImageMemoryCache.putIntoMemory(path + reqHeight + reqHeight, bitmap);
                putIntoMenmery(path + reqHeight + reqHeight, bitmap);
                return bitmap;
            }
            Log.d(TAG, "没有从磁盘读到");
        } else {
            Log.d(TAG, "从缓存读到");
            return bitmap;
        }
        return bitmap;
    }

    /**
     * 从网络下载并保存到缓存
     *
     * @param url
     */
    private void downloadFromNetwork(String url, ImageView view) throws IOException {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("禁止在UI线程加载网络");
        }
    }

    private static class LoaderResult {
        ImageView imageView;
        Bitmap bitmap;
        String uriTag;

        public LoaderResult(ImageView imageView, Bitmap bitmap, String uriTag) {
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.uriTag = uriTag;
        }

        public String getUriTag() {
            return uriTag;
        }

        public void setUriTag(String uriTag) {
            this.uriTag = uriTag;
        }

        @UiThread
        public void setImageView(ImageView imageView) {
            this.imageView = imageView;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }

    public int getDefaultBitmap() {
        return defaultBitmap;
    }

    public void setDefaultBitmap(int defaultBitmap) {
        this.defaultBitmap = defaultBitmap;
    }

    /**
     * 存入内存
     *
     * @param path
     * @param bitmap
     */
    private void putIntoMenmery(String path, Bitmap bitmap) {
        Log.d(TAG, "保存到内存" + path);
        mImageMemoryCache.putIntoMemory(path, bitmap);
    }
}
