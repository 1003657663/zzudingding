package com.codeevery.NetGetPost;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by songchao on 15/7/31.
 */
public class MemoryPhoto {
    private final int maxNumPhoto = 100;
    //内存大小限制参数
    private long size = 0;
    private long maxSize = 5000000;
    private Map<String,Bitmap> cache;

    public MemoryPhoto(Map<String,Bitmap> cache){
        //获取可用最大内存的25%的值，用来限制图片占用内存过大；
        maxSize = Runtime.getRuntime().maxMemory()/4;
        Log.i("可用最大图片内存",""+maxSize);
        this.cache = cache;
    }
    //获取从map
    public Bitmap get(String id){
        try {
            if (!cache.containsKey(id))
                return null;
            else
                return cache.get(id);
        }catch (NullPointerException ex){
            Log.i("get Bitmap Error:",ex.getMessage());
            return null;
        }
    }
    //放进去
    public  void put(String id,Bitmap bitmap){
        try {
            if(bitmap!=null) {
                if (cache.containsKey(id)) {
                    size -= cache.get(id).getRowBytes() * cache.get(id).getHeight();
                    cache.remove(id);
                    cache.put(id, bitmap);
                    size += bitmap.getRowBytes() * bitmap.getHeight();
                }
                cache.put(id, bitmap);
                size += bitmap.getRowBytes() * bitmap.getHeight();
                checkSize();
            }
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
    }

    public void checkSize(){
        if(size>maxSize){
            Iterator<Map.Entry<String,Bitmap>> entryIterator = cache.entrySet().iterator();
            while(entryIterator.hasNext()){
                Map.Entry<String,Bitmap> entry = entryIterator.next();
                size -= entry.getValue().getRowBytes()*entry.getValue().getHeight();
                entryIterator.remove();
                if(size<=maxSize)
                    break;
            }
        }
    }



}
