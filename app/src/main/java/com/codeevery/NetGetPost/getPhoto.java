package com.codeevery.NetGetPost;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.codeevery.zzudingding.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by songchao on 15/7/31.
 */
public class getPhoto {
    private Map<String,Bitmap> cache = Collections.synchronizedMap(new LinkedHashMap<String, Bitmap>());
    private MemoryPhoto memoryPhoto;
    private ExecutorService executorService;
    public getPhoto(){
        memoryPhoto = new MemoryPhoto(cache);
        executorService = Executors.newFixedThreadPool(5);
    }
    //默认的图片
    final int defaultPhoto = R.drawable.book_default;
    //重要方法，获取加载图片
    public void DisplayImage(String url,ImageView imageView) {
        //从内存中查找图片
        Bitmap bitmap = memoryPhoto.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        else {
            //如果内存中没有这个图片，那么就新建一个线程获取图片
            //先加载默认图片
            imageView.setImageResource(defaultPhoto);
            //新建线程获取图片
            //新建一个线程类，新建类加入线程中
            executorService.submit(new PhotosLoader(url,imageView));

        }
    }


    class PhotosLoader implements Runnable{
        String url;
        ImageView imageView;
        PhotosLoader(String url,ImageView imageView){
            this.url = url;
            this.imageView = imageView;
        }
        //设置Handler
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==1){
                    imageView.setImageBitmap((Bitmap)msg.obj);
                }
                super.handleMessage(msg);
            }
        };
        @Override
        public void run() {
            //下载图片
            Bitmap bitmap = getBitmap(url);
            memoryPhoto.put(url,bitmap);
            //更新UI线程
            if(bitmap!=null){
                Message message = handler.obtainMessage(1,bitmap);
                handler.sendMessage(message);
                //imageView.setImageBitmap(bitmap);
            }
            else{
                imageView.setImageResource(defaultPhoto);
            }
        }
    }
    //获取图片的方法
    public Bitmap getBitmap(String imageUrl){
        try{
            Bitmap bitmap = null;
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            //设置是否自动处理重定向
            connection.setInstanceFollowRedirects(true);
            InputStream in = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(in);
            return bitmap;
        }catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }
}
