package com.codeevery.NetGetPost;

import android.content.Context;
import android.widget.Toast;

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
import java.util.Map;

import com.codeevery.myElement.myDialog;

/**
 * Created by songchao on 15/8/7.
 */
public class DoPostGet{
    Context context;
    DoSomeThing doSomeThing;
    myDialog dialog;
    public DoPostGet(Context context){
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        dialog = new myDialog(context);
    }

    public void setDialogNull(){
        dialog.initProgressDialog();
    }

    public void getDialog(){
        dialog.getProgressDialog();
    }

    RequestQueue requestQueue;
    public void doGet(String url,final String charase){
        dialog.showProgressDialog("正在加载数据...");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hideProgressDialog();
                doSomeThing.onDo(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hideProgressDialog();
                Toast.makeText(context,VolleyErrorHelper.getMessage(volleyError,context),Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parase;
                try {
                    parase = new String(response.data,charase);
                } catch (UnsupportedEncodingException e) {
                    parase = new String(response.data);
                    e.printStackTrace();
                }
                return Response.success(parase, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        requestQueue.add(stringRequest);
    }

    public void doPost(String url, final String charase,final Map map){
        dialog.showProgressDialog("正在加载数据...");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hideProgressDialog();
                doSomeThing.onDo(s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hideProgressDialog();
                Toast.makeText(context,VolleyErrorHelper.getMessage(volleyError,context),Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parase;
                try {
                    parase = new String(response.data,charase);
                } catch (UnsupportedEncodingException e) {
                    parase = new String(response.data);
                    e.printStackTrace();
                }
                return Response.success(parase, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        requestQueue.add(stringRequest);
    }
    public void setInterface(DoSomeThing doSomeThing){
        this.doSomeThing = doSomeThing;
    }

    public interface DoSomeThing {
        public void onDo(String str);
    }
}

