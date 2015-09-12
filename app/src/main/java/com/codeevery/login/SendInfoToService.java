package com.codeevery.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by codeevery on 2015/7/23.
 * 此类的作用
 * 发送用户信息到服务器，实现信息记录以便以后登陆使用
 *
 * 标记：
 * 即将实现功能：
 * 发送信息到服务器
 * 服务器可以修改客户端的服务器网址，实现发送位置的改变
 */
public class SendInfoToService {

    private String url;
    private String xuehao,mima,name;
    private RequestQueue requestQueue;
    private Context context;
    private SharedPreferences.Editor editor;
    public SendInfoToService(Context context, String url, SharedPreferences.Editor editor, String xuehao, String mima, String name){
        this.xuehao = xuehao;
        this.mima = mima;
        this.name = name;
        this.context = context;
        this.url = url;
        this.editor = editor;
    }

    //dogetf方法
    public void doGet(){
        requestQueue = Volley.newRequestQueue(context);//创建一个requestQueue对象请求队列对象


        StringRequest stringRequest = new StringRequest(Request.Method.GET,url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("Response",volleyError.getMessage(),volleyError);
            }
        });
        requestQueue.add(stringRequest);
    }

    //post方法
    public void doPost(){
        requestQueue = Volley.newRequestQueue(context);//创建一个requestQueue对象请求队列对象
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //发送成功写入文件
                editor.putBoolean("sendTo",true);
                editor.commit();
            }
        },new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                editor.putBoolean("sendTo",false);
                editor.commit();
                Log.e("Response",volleyError.getMessage(),volleyError);
            }
        })
            {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String>map = new HashMap<>();
                /*try {
                    name = new String(name.getBytes("utf-8"),"utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }*/
                map.put("xuehao",xuehao);
                map.put("mima",mima);
                map.put("name",name);
                return map;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                // TODO Auto-generated method stub
                String str = null;
                try {
                    str = new String(response.data,"utf-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return Response.success(str, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        requestQueue.add(request);
    }
}
