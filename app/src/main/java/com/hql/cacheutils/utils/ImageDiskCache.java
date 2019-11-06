package com.hql.cacheutils.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * @author ly-huangql
 * <br /> Create time : 2019/6/26
 * <br /> Description :
 */
public class ImageDiskCache {
    private final static String TAG = "cache_ImageDiskCache";
    private final static long CACHE_SIZE = 1024 * 1024 * 50;
    private final static int CACHE_INDEX = 0;
    //private final static int IO_BUFFER_SIZE = 1024 * 1024 * 3;
    Context mContext;
    DiskLruCache mDiskLruCache;
    ImageClip mImageClip;

    public ImageDiskCache(Context context) {
        mContext = context;
        mImageClip = new ImageClip();
        File cacheDir = getDiskCacheDir(mContext, "temp");
        try {
            mDiskLruCache = DiskLruCache.open(cacheDir,
                    1/*app版本改变会导致清空缓存，但是没必要*/,
                    1,
                    CACHE_SIZE);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Bitmap getBitmapFromDisk(String path, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        FileInputStream fis = null;
        try {
            Log.d(TAG, "从磁盘读取path：" + path);
            Log.d(TAG, "从磁盘读取转换path：" + Utils.getMD5Key(path + reqWidth + reqHeight));
            //DiskLruCache.Snapshot snapshot = mDiskLruCache.get(Utils.getMD5Key(path + reqWidth + reqHeight));
            DiskLruCache.Snapshot snapshot = mDiskLruCache.get(Utils.getMD5Key(path + reqWidth + reqHeight));//从磁盘获取该URL的图片
            if (null != snapshot) {
                fis = (FileInputStream) snapshot.getInputStream(0);
                FileDescriptor fileDescriptor = fis.getFD();
                bitmap = mImageClip.decodeBitmapFromInputStream(fileDescriptor, reqWidth, reqHeight);
                Log.d(TAG, "从磁盘读取" + bitmap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public boolean remove(String key) {
        String path = Utils.getMD5Key(key);
        try {
            return mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
//---读取bitmap模糊文件--------------------------------------------------------------------


    //---从本地文件读取--------------------------------------------------------------------
    public Bitmap localPicIntoDisk(String path, int width, int height) {
        Bitmap bitmap = null;
        try {
            // DiskLruCache.Editor editor = mDiskLruCache.edit(Utils.getMD5Key(path + width + height));
            DiskLruCache.Editor editor;
            editor = mDiskLruCache.edit(Utils.getMD5Key(path));
            if (null != editor) {//editor如果在下载同一个图片时，会返回空
                OutputStream outputStream = editor.newOutputStream(CACHE_INDEX);
                boolean result = readPicToStream(path, outputStream);
                if (result) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                mDiskLruCache.flush();
                outputStream.close();
                if (result) {
                    bitmap = getBitmapFromDisk(path, width, height);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return bitmap;
        }
        return bitmap;
    }

    private boolean readPicToStream(String path, OutputStream outputStream) {
        try {
            FileReader fr = new FileReader(path);
            int length = 0;
            while ((length = fr.read()) != -1) {          //如果没有读到文件末尾
                outputStream.write(length);           //向文件写入数据
            }
            fr.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //byte[] fileByte = Files.readAllBytes(file.toPath());

        return false;
    }

//---从音频文件读取--------------------------------------------------------------------

    /**
     * @param path
     * @param width
     * @param height
     * @return
     */
    public Bitmap mediaAlbumIntoDisk(String path, int width, int height) {
        Bitmap bitmap = null;
        MediaMetadataRetriever metadataRetriever = null;
        try {
            metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(path);
            metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            byte[] bos = metadataRetriever.getEmbeddedPicture();
            Log.d(TAG, "mediaAlbumIntoDisk 读取多媒体path:" + path);
            if (null != bos) {
                bitmap = BitmapUtil.getMusicPic(bos, width, height);

                DiskLruCache.Editor editor;
                editor = mDiskLruCache.edit(Utils.getMD5Key(path));
                if (null != editor) {//editor如果在下载同一个图片时，会返回空
                    OutputStream outputStream = editor.newOutputStream(CACHE_INDEX);
                    boolean result = blurToStream(bitmap, outputStream);
                    Log.d(TAG, "虚幻图片result:" + result);
                    if (result) {
                        editor.commit();
                    } else {
                        editor.abort();
                    }
                    mDiskLruCache.flush();
                    outputStream.close();
                }
            } else {
                Log.d(TAG, "mediaAlbumIntoDisk bos  null");
                return null;
            }
        } catch (Exception e) {
            metadataRetriever.release();
            Log.e(TAG, "onBindViewHolder metadataRetriever erro: " + e.toString());
        } finally {
            if (null != metadataRetriever) {
                metadataRetriever.release();
                metadataRetriever = null;
            }

        }
        return bitmap;
    }
//-----------------------------------------------------------------------

    /**
     * 从网络文件解析
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    public Bitmap downloadIntoDisk(String path, int width, int height) {
        Bitmap bitmap = null;
        try {
            DiskLruCache.Editor editor = null;
            boolean result = false;
            OutputStream outputStream = null;
            editor = mDiskLruCache.edit(Utils.getMD5Key(path + width + height));
            if (null != editor) {  //editor如果在下载同一个图片时，会返回空
                outputStream = editor.newOutputStream(CACHE_INDEX);
                result = tranleUrlToStream(path, outputStream);

                if (result) {
                    editor.commit();
                } else {
                    editor.abort();
                }
            }
            if (null != outputStream) {
                outputStream.close();
            }

            mDiskLruCache.flush();
            Log.d(TAG, "downloadIntoDisk 下载结果:" + result);
            if (result) {
                Log.d(TAG, "downloadIntoDisk不虚化 下载完成重新从磁盘读取:" + result);
                Log.d(TAG, "downloadIntoDisk 写入磁盘文件名:" + Utils.getMD5Key(path + width + height));
                bitmap = getBitmapFromDisk(path, width, height);
                Log.d(TAG, "downloadIntoDisk 下载结果读取:" + result);
            }
        } catch (
                SocketTimeoutException e) {
            e.printStackTrace();
        } catch (
                IOException e) {
            e.printStackTrace();
            return bitmap;
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private boolean tranleUrlToStream(String path, OutputStream outputStream) throws Exception {
        Log.d(TAG, "开始下载 path:" + path);
        //Object[] result = new Object[2];
        HttpURLConnection httpURLConnection = null;
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        //try {
        URL url = new URL(path);
        if (null != url) {
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            //设置允许输入
            httpURLConnection.setDoInput(true);
            //设置为GET方式请求数据
            httpURLConnection.setRequestMethod("GET");
            //设置请求报文头，设定请求数据类型
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
            // httpURLConnection.connect();
            //获取连接响应码，200为成功，如果为其他，均表示有问题
            int responseCode = httpURLConnection.getResponseCode();
            Log.d(TAG, ">>>>>>>>>>>>>>>>>>responseCode:" + responseCode);
            if (HttpURLConnection.HTTP_OK == responseCode) {
                bis = new BufferedInputStream(httpURLConnection.getInputStream());
                Log.d(TAG, ">>>>转换图片");
                //result[1] = BitmapFactory.decodeStream(bis);
                bos = new BufferedOutputStream(outputStream);
                int b;
                while ((b = bis.read()) != -1) {
                    bos.write(b);
                }

                // result[0] = true;

                bis.close();
                bos.close();
                return true;
            }
        }
        if (null != httpURLConnection) {
            httpURLConnection.disconnect();
        }
        return false;
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    //---bitmap转虚化start-----------------------------------
    public final static String BLUR_TAG = "blur_tag";

    /**
     * 保存虚化图片到硬盘
     *
     * @param path
     * @param bitmap
     */
    public void saveBitmapIntoDisk(String path, Bitmap bitmap) {

        try {
            Log.d(TAG, "保存到磁盘:" + path);
            DiskLruCache.Editor editor = mDiskLruCache.edit(Utils.getMD5Key(path));
            if (null != editor) {//editor如果在下载同一个图片时，会返回空
                OutputStream outputStream = editor.newOutputStream(CACHE_INDEX);
                boolean result = blurToStream(bitmap, outputStream);
                Log.d(TAG, "保存到磁盘 结果:" + result);
                if (result) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                mDiskLruCache.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 把bitmap写入流
     *
     * @param bitmap1
     * @param outputStream
     * @return
     */
    private boolean blurToStream(Bitmap bitmap1, OutputStream outputStream) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        try {
            Log.d(TAG, "blurToStream >>>" + data.length);
            outputStream.write(data);
            baos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}
