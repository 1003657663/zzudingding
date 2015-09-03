package com.codeevery.login;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codeevery.NetGetPost.VolleyErrorHelper;
import com.codeevery.application.AllObject;
import com.codeevery.myElement.GetPostThread;
import com.codeevery.myElement.myDialog;

/**
 * Created by songchao on 15/8/15.
 * 用来登陆校卡中心
 */
public class CardLoginAuto {
    AllObject setting;
    Context context;
    myDialog dialog;
    boolean showDialog;

    public CardLoginAuto(AllObject setting, Context context, boolean showDialog) {
        this.setting = setting;
        this.context = context;
        this.showDialog = showDialog;
        if (showDialog)
            dialog = new myDialog(context);
        else
            dialog = null;
    }

    public void getCheckText(ThirdHandler.SuccessDo1 successDo1) {
        String url = "http://ecard.zzu.edu.cn/web/guest/home";
        firstRequest(successDo1);//使用volley代替自己的thread速度变快一倍不止
        //Firsthandler firsthandler = new Firsthandler(context, dialog, successDo1);
        //new GetPostThread(firsthandler, url, setting.cardCookieMap, true).start();
    }

    //开始请求
    //第三次请求，获取验证码
    public static class ThirdHandler extends Handler {
        myDialog dialog;
        Context context;
        AllObject setting;
        SuccessDo1 successDo1 = null;

        ThirdHandler(Context context, myDialog dialog) {
            this.context = context;
            this.dialog = dialog;
            setting = (AllObject) context.getApplicationContext();
        }

        public void setInterface1(SuccessDo1 successDo1) {
            this.successDo1 = successDo1;
        }

        public interface SuccessDo1 {
            public void successDo1();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == -1 && dialog != null) {
                dialog.showDialogWithSure("服务器异常,请稍后再试", "好的");
                return;
            } else if (msg.what == -2 && dialog != null) {
                dialog.showDialogWithSure("请求超时,请稍后再试", "好的");
                return;
            } else if (msg.what == 1) {
                Bundle bundle = msg.getData();
                String checkText = (String) bundle.get("result");
                checkText = checkText.trim();
                setting.checkText = checkText;
                if (successDo1 != null)
                    successDo1.successDo1();
            }
        }
    }

    //-------------------第二次请求是用来请求验证码图片
    public static class SecondHandler extends Handler {
        myDialog dialog;
        Context context;
        AllObject setting;
        ThirdHandler.SuccessDo1 successDo1;

        SecondHandler(Context context, myDialog dialog, ThirdHandler.SuccessDo1 successDo1) {
            this.context = context;
            this.dialog = dialog;
            setting = (AllObject) context.getApplicationContext();
            this.successDo1 = successDo1;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == -1 && dialog != null) {
                dialog.showDialogWithSure("服务器异常,请稍后再试", "好的");
                return;
            } else if (msg.what == -2 && dialog != null) {
                dialog.showDialogWithSure("请求超时,请稍后再试", "好的");
                return;
            } else if (msg.what == 1) {
                String thirdUrl = "http://ecard.zzu.edu.cn/html/portlet/login/verifyCode.jsp?namespace=_58_";
                ThirdHandler thirdHandler = new ThirdHandler(context, dialog);
                thirdHandler.setInterface1(successDo1);
                new GetPostThread(thirdHandler, thirdUrl, setting.cardCookieMap, true).start();
            }
        }
    }


    //第一个handler是获取验证码的入口，到handler3
    /*public static class Firsthandler extends Handler {
        myDialog dialog;
        Context context;
        AllObject setting;
        ThirdHandler.SuccessDo1 successDo1;

        Firsthandler(Context context, myDialog dialog, ThirdHandler.SuccessDo1 successDo1) {
            this.context = context;
            this.dialog = dialog;
            setting = (AllObject) context.getApplicationContext();
            this.successDo1 = successDo1;
            if(dialog!=null)
                dialog.showProgressDialog("正在努力登陆,耐心等等它吧");
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == -1 && dialog != null) {
                dialog.showDialogWithSure("服务器异常,请稍后再试", "好的");
                return;
            } else if (msg.what == -2 && dialog != null) {
                dialog.showDialogWithSure("请求超时,请稍后再试", "好的");
                return;
            }
            else if(msg.what == -3){
                dialog.showDialogWithSure("登陆异常","确定");
                return;
            } else if (msg.what == 1) {
                Bundle bundle = msg.getData();
                String result = bundle.getString("result", "");
                String imgUrl = "";
                try {
                    imgUrl = resolveResult(result);
                } catch (NullPointerException ex) {
                    System.out.println(result);
                }
                if(imgUrl.equals("")){
                    if(result.contains("欢迎")){
                        dialog.hideProgressDialog();
                        if(ForthHandler.successDo!=null)
                            ForthHandler.successDo.successDo();
                    }
                }else {

                    CookieStore cookieStore = (CookieStore) msg.obj;
                    List<HttpCookie> cookies = cookieStore.getCookies();
                    for (HttpCookie cookie : cookies) {
                        System.out.println(cookie);
                        String a[] = cookie.toString().split("=");
                        setting.cardCookieMap.put(a[0], a[1]);
                    }
                    System.out.println(setting.cardCookieMap.toString());
                    SecondHandler secondHandler = new SecondHandler(context, dialog, successDo1);
                    new GetPostThread(secondHandler, imgUrl, setting.cardCookieMap, true).start();
                }
            }
        }

        public String resolveResult(String str) {
            Element element = Jsoup.parse(str).body();
            String imgUrl = element.getElementById("_58_captcha").attr("src");
            setting.formAction = element.getElementById("_58_loginForm").attr("action");
            return imgUrl;
        }
    }*/


    public void firstRequest(final ThirdHandler.SuccessDo1 successDo1){
        final CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://ecard.zzu.edu.cn/web/guest/home", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //返回字符串
                CookieStore cookieStore = cookieManager.getCookieStore();
                String result = s;

                String imgUrl = "";
                try {
                    imgUrl = resolveResult(result);
                } catch (NullPointerException ex) {
                    if(dialog!=null){
                        dialog.showDialogWithSure("读取验证码异常,重试一下吧","确定");
                    }
                }
                if(imgUrl.equals("")){
                    if(result.contains("欢迎")){
                        if(dialog!=null)
                            dialog.hideProgressDialog();
                        if(ForthHandler.successDo!=null)
                            ForthHandler.successDo.successDo();
                    }
                }else {
                    List<HttpCookie> cookies = cookieStore.getCookies();
                    for (HttpCookie cookie : cookies) {
                        String a[] = cookie.toString().split("=");
                        setting.cardCookieMap.put(a[0], a[1]);
                    }
                    SecondHandler secondHandler = new SecondHandler(context, dialog, successDo1);
                    new GetPostThread(secondHandler, imgUrl, setting.cardCookieMap, true).start();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(dialog!=null)
                    dialog.showDialogWithSure(VolleyErrorHelper.getMessage(volleyError,context),"确定");
            }
        });
        if(dialog!=null)
            dialog.showProgressDialog("正在努力登陆,耐心等等它吧");
        requestQueue.add(stringRequest);
    }

    public String resolveResult(String str) {
        Element element = Jsoup.parse(str).body();
        String imgUrl = element.getElementById("_58_captcha").attr("src");
        setting.formAction = element.getElementById("_58_loginForm").attr("action");
        return imgUrl;
    }

    public static class ForthHandler extends Handler {
        myDialog dialog;
        Context context;
        AllObject setting;
        public static SuccessDo successDo;
        String noSureXuehao;
        String noSureMima;

        ForthHandler(Context context, myDialog dialog, String noSureXuehao, String noSureMima) {
            this.context = context;
            this.dialog = dialog;
            setting = (AllObject) context.getApplicationContext();
            this.noSureXuehao = noSureXuehao;
            this.noSureMima = noSureMima;
        }


        public void setInterface(SuccessDo successDo) {
            this.successDo = successDo;
        }

        public interface SuccessDo {
            public void successDo();
        }

        @Override
        public void handleMessage(Message msg) {
            if (dialog != null)
                dialog.hideProgressDialog();
            if (msg.what == -1 && dialog != null) {
                dialog.showDialogWithSure("服务器异常,请稍后再试", "好的");
                return;
            } else if (msg.what == -2 && dialog != null) {
                dialog.showDialogWithSure("请求超时,请稍后再试", "好的");
                return;
            } else if (msg.what == 5) {
                //String result = msg.getData().getString("result");
                //if (result.contains("欢迎")) {
                CookieStore cookieStore = (CookieStore) msg.obj;
                if(cookieStore.getCookies().toString().contains("JSESSIONID")){
                    List<HttpCookie> cookies = cookieStore.getCookies();
                    for (HttpCookie cookie : cookies) {
                        String a[] = cookie.toString().split("=");
                        if (setting.cardCookieMap.containsKey(a[0])) {
                            if (!setting.cardCookieMap.get(a[0]).equals(a[1])) {
                                setting.cardCookieMap.put(a[0], a[1]);
                            }
                        } else {
                            setting.cardCookieMap.put(a[0], a[1]);
                        }
                    }
                    setting.cardLoginTime = System.currentTimeMillis();//登陆成功就记录当前时间
                    successDo.successDo();
                }
                else{
                    //---------------------------------------------登录失败
                    if(dialog!=null) {
                        dialog.showDialogWithSure("登陆失败,账号或密码错误,您也可以尝试注销饭卡重新登陆", "好的");
                    }
                }
            }
            else {
                if(dialog!=null)
                    dialog.showDialogWithSure("登陆异常,请退出此页面重新进入","确定");
                return;
            }
        }
    }


    public void startCardLogin(String noSureXuehao, String noSureMima, ForthHandler.SuccessDo successDo) {
        //------------------------------------------
        Map<String, String> map = new HashMap<>();
        map.put("save_last_path", "0");
        map.put("_58_redirect", "");
        map.put("_58_rememberMe", "false");
        map.put("_58_login", noSureXuehao);
        map.put("_58_password", noSureMima);
        map.put("_58_captchaText", setting.checkText);
        ForthHandler forthHandler = new ForthHandler(context, dialog, noSureXuehao, noSureMima);
        forthHandler.setInterface(successDo);
        GetPostThread getPostThread2 = new GetPostThread(forthHandler, setting.formAction, setting.cardCookieMap, false, map);
        getPostThread2.setOnlyCookie(true);
        getPostThread2.start();
    }
}
