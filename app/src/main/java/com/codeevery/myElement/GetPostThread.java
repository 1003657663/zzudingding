package com.codeevery.myElement;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Created by codeevery on 2015/8/13.
 */
public class GetPostThread extends Thread {

    String postUrl;
    Handler handler;
    Map<String, String> cookie;
    Map<String, String> map;
    String encode = "UTF-8";
    Context context;
    boolean onlyCookie = false;//true表示只想获得cookie，false是想一起获得内容
    boolean isPostFileMethod = false;//true是二进制传输方法，false是普通传输方法
    boolean getOrPost = true;//true是get，false是post
    boolean hasCookieManager = true;

    public GetPostThread(Handler handler, String posturl, Map<String, String> cookie, boolean getOrPost) {
        this.handler = handler;
        this.postUrl = posturl;
        this.cookie = cookie;
        this.getOrPost = getOrPost;
        this.map = null;
    }

    public GetPostThread(Handler handler, String posturl, Map<String, String> cookie, boolean getOrPost, Map<String, String> map) {
        this.handler = handler;
        this.postUrl = posturl;
        this.cookie = cookie;
        this.getOrPost = getOrPost;
        this.map = map;
    }

    public GetPostThread(Handler handler, String posturl, Map<String, String> cookie, boolean getOrPost, Map<String, String> map, boolean isPostFileMethod) {
        this.handler = handler;
        this.postUrl = posturl;
        this.cookie = cookie;
        this.getOrPost = getOrPost;
        this.map = map;
        this.isPostFileMethod = isPostFileMethod;
    }

    public void setEncoding(String encode) {
        this.encode = encode;
    }

    public void setOnlyCookie(boolean onlyCookie) {
        this.onlyCookie = onlyCookie;
    }

    public void setNoCookieManager(boolean hasCookieManager) {
        this.hasCookieManager = hasCookieManager;
    }

    public void doPost() {
        try {
            CookieManager cookieManager = null;
            if(hasCookieManager){
                cookieManager = new CookieManager();
                CookieHandler.setDefault(cookieManager);
            }

            URL url = new URL(postUrl);
            //HttpCookie httpCookie = new HttpCookie();
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setConnectTimeout(1500);
            httpURLConnection.setReadTimeout(2000);

            httpURLConnection.setRequestProperty("Content-Language", "zh-cn");
            httpURLConnection.setRequestProperty("Connection", "keep-alive");
            httpURLConnection.setRequestProperty("Cache-Control", "no-cache");

            if (cookie != null) {
                String cookieText = "";
                Iterator<Map.Entry<String, String>> iterator = cookie.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    if (entry.getKey().contains("JSESSIONID")) {
                        cookieText += "JSESSIONID=" + entry.getValue() + ";";
                    } else {
                        cookieText += entry.getKey() + "=" + entry.getValue() + ";";
                    }
                }
                System.out.println("cookieText:" + cookieText);
                httpURLConnection.setRequestProperty("Cookie", cookieText);
            }

            if (!isPostFileMethod) {
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                if (map != null) {
                    StringBuffer stringBuffer = new StringBuffer();
                    Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, String> entry = it.next();
                        stringBuffer.append(entry.getKey() + "=" + entry.getValue() + "&");
                    }
                    OutputStream out = httpURLConnection.getOutputStream();
                    stringBuffer.append("/r/n/r/n");
                    byte[] data = stringBuffer.toString().getBytes();
                    System.out.println("before post:" + stringBuffer.toString());
                    out.write(data);
                    out.flush();
                    out.close();
                }
            } else {
                if (map != null) {
                    String boundary = "----" + UUID.randomUUID().toString();
                    System.out.println("boundary:" + boundary);
                    httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    OutputStream out = httpURLConnection.getOutputStream();
                    StringBuffer sb = new StringBuffer();
                    Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
                    String enterAnd = "\r\n";
                    boundary = boundary + enterAnd;
                    while (it.hasNext()) {
                        sb.append(boundary);
                        Map.Entry<String, String> entry = it.next();
                        sb.append("Content-Disposition: form-data;name=\"" + entry.getKey() + "\"");
                        sb.append(enterAnd + enterAnd);
                        sb.append(entry.getValue() + enterAnd);
                    }
                    sb.append(boundary);
                    System.out.println(sb.toString());

                    out.write(sb.toString().getBytes("utf-8"));
                    out.flush();
                    out.close();
                }
            }
            String encoding;
            if (encode == null)
                encoding = "utf-8";
            else
                encoding = encode;
            InputStreamReader input = new InputStreamReader(httpURLConnection.getInputStream(), encoding);

            //判断服务器状态
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode >= 400 && responseCode <= 600) {
                //服务器错误
                Message msg = new Message();
                msg.what = -1;//-1代表服务器异常
                handler.sendMessage(msg);
                input.close();
                httpURLConnection.disconnect();
                return;
            }

            if (cookieManager != null) {
                CookieStore cookieStore = cookieManager.getCookieStore();
                if (onlyCookie) {
                    Message message = new Message();
                    message.obj = cookieStore;
                    message.what = 5;
                    handler.sendMessage(message);
                    //return;
                }
            }

            BufferedReader bufferedReader = new BufferedReader(input);
            String line;
            String result = "";
            while ((line = bufferedReader.readLine()) != null) {
                result += line;
                if(onlyCookie) {
                    input.close();
                    bufferedReader.close();
                    httpURLConnection.disconnect();
                    return;
                }
            }
            input.close();
            bufferedReader.close();
            httpURLConnection.disconnect();
            Message message = new Message();
            if (cookieManager != null) {
                CookieStore cookieStore = cookieManager.getCookieStore();
                message.obj = cookieStore;
            }
            Bundle bundle = new Bundle();
            bundle.putString("result", result);
            message.setData(bundle);
            message.what = 1;
            handler.sendMessage(message);

        } catch (SocketTimeoutException se) {
            Message msg = new Message();
            msg.what = -2; //-2代表超时异常
            handler.sendMessage(msg);
        } catch (IOException e) {
            Message msg = new Message();
            msg.what = -3;
            handler.sendMessage(msg);
            e.printStackTrace();
        }
    }


    public void doGet() {
        try {
            CookieManager cookieManager = null;
            if (hasCookieManager) {
                cookieManager = new CookieManager();
                CookieHandler.setDefault(cookieManager);
            }

            URL url = new URL(postUrl);
            //HttpCookie httpCookie = new HttpCookie();
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(1500);
            httpURLConnection.setReadTimeout(2000);

            httpURLConnection.setRequestProperty("Content-Type", "com/codeevery/application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Language", "zh-cn");
            httpURLConnection.setRequestProperty("Connection", "keep-alive");
            httpURLConnection.setRequestProperty("Cache-Control", "no-cache");
            if (cookie != null) {
                String cookieText = "";
                Iterator<Map.Entry<String, String>> iterator = cookie.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> entry = iterator.next();
                    cookieText += entry.getKey() + "=" + entry.getValue() + ";";
                }
                httpURLConnection.setRequestProperty("Cookie", cookieText);
            }

            InputStream in = httpURLConnection.getInputStream();

            //判断服务器状态
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode >= 400 && responseCode <= 600) {
                //服务器错误
                Message msg = new Message();
                msg.what = -1;
                handler.sendMessage(msg);
                in.close();
                httpURLConnection.disconnect();
                return;
            }
            if (cookieManager != null) {
                CookieStore cookieStore = cookieManager.getCookieStore();
                if (onlyCookie) {
                    Message message = new Message();
                    message.obj = cookieStore;
                    message.what = 5;
                    handler.sendMessage(message);
                    in.close();
                    httpURLConnection.disconnect();
                    return;
                }
            }
            if (httpURLConnection.getContentType().contains("html")) {
                String encoding;
                if (encode == null)
                    encoding = "utf-8";
                else
                    encoding = encode;
                InputStreamReader inReader = new InputStreamReader(in, encoding);
                BufferedReader bufferedReader = new BufferedReader(inReader);

                String line;
                String result = "";
                while ((line = bufferedReader.readLine()) != null) {
                    result += line + "\n";
                }
                //关闭流
                inReader.close();
                httpURLConnection.disconnect();

                Message message = new Message();
                if (cookieManager != null) {
                    CookieStore cookieStore = cookieManager.getCookieStore();
                    message.obj = cookieStore;
                }
                Bundle bundle = new Bundle();
                bundle.putString("result", result);
                message.setData(bundle);
                message.what = 1;
                handler.sendMessage(message);
            } else if (httpURLConnection.getContentType().contains("image")) {
                InputStream is = httpURLConnection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                Message message = new Message();
                message.obj = bitmap;
                message.what = 10;
                handler.sendMessage(message);
            }
        } catch (SocketTimeoutException se) {
            Message msg = new Message();
            msg.what = -2; //-2代表超时异常
            handler.sendMessage(msg);
        } catch (Exception ex) {
            Message msg = new Message();
            msg.what = -3;
            handler.sendMessage(msg);
            Log.e("Exception Error",ex.getStackTrace().toString());
        }
    }

    @Override
    public void run() {
        if (getOrPost)
            doGet();
        else
            doPost();
    }
}
