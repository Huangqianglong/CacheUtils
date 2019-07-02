package com.hql.cacheutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * @author ly-huangql
 * <br /> Create time : 2019/6/26
 * <br /> Description :
 */
public class ImageMemoryCache {
    private final static String TAG = "Cache_ImageMemoryCache";
    LruCache<String, Bitmap> cacheList;


    public ImageMemoryCache(Context context) {
        // 计算可使用的最大内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 取可用内存空间的1/4作为缓存
        final int cacheSize = maxMemory / 4;
        this.cacheList = new LruCache<>(cacheSize);
    }


    public void putIntoMemory(String key, Bitmap bitmap) {

        if (null != key && null != bitmap) {
            cacheList.put(Utils.getMD5Key(key), bitmap);
        } else {
            Log.e(TAG, "存入缓存失败 key:" + key + ">>>bitmap:" + bitmap);
        }
    }

    public Bitmap getFromMemory(String key) {
        return cacheList.get(Utils.getMD5Key(key));
    }

    public boolean remove(String key) {
        return cacheList.remove(Utils.getMD5Key(key)) instanceof Bitmap;
    }
}
