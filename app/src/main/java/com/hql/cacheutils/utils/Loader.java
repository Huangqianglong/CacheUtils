package com.hql.cacheutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.widget.ImageView;

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
    /**
     * 主线程handler,不要用来处理非UI线程的工作
     */
    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ACTION_UPDATE_BITMAP:
                    LoaderResult result = (LoaderResult) msg.obj;
                    if (result.imageView.getTag().equals(result.getUriTag())) {
                        if (null != result.bitmap) {
                            result.imageView.setImageBitmap(result.bitmap);
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
    /**
     *尝试从内存和磁盘获取图片
     * @param url
     */
    private Bitmap getBitmap(String path, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        bitmap = mImageMemoryCache.getFromMemory(path + reqWidth + reqHeight);//从缓存中获取对应宽高的图片
        if (null == bitmap) {
            bitmap = mImageDiskCache.getBitmapFromDisk(path, reqWidth, reqHeight);
            if (null != bitmap) {
                Log.d(TAG, "从磁盘读取");
            }
        } else {
            Log.d(TAG, "从内存读取");
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

    /**
     * 从磁盘读取并设置图片
     *
     * @param path
     * @param view
     * @param width
     * @param height
     */
    public void bindBitmapFromDisk(String path, ImageView view, int width, int height) {
        Bitmap bitmap = getBitmap(path, width, height);
        if (null != bitmap) {
            view.setImageBitmap(bitmap);
            return;
        }
    }

    /**
     * 从网络读取并设置图片
     *
     * @param url
     * @param view
     * @param width
     * @param height
     */
    public void bindBitmapFromURL(final String url, final ImageView view, final int width, final int height) {
        Bitmap bitmap = getBitmap(url, width, height);
        if (null != bitmap) {
            view.setImageBitmap(bitmap);
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "从网络读取");
                Bitmap bitmap = mImageDiskCache.downloadIntoDisk(url, width, height);
                LoaderResult result = new LoaderResult(view, bitmap, url);
                mainHandler.obtainMessage(ACTION_UPDATE_BITMAP, result).sendToTarget();
                if (null != bitmap) {
                    mImageMemoryCache.putIntoMemory(url + width + height, bitmap);
                }
            }
        };
        THERAD_POOL.execute(runnable);
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
}
