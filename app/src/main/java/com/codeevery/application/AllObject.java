package com.codeevery.application;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Map;

import io.yunba.android.manager.YunBaManager;

/**
 * Created by songchao on 15/8/4.
 */
public class AllObject extends Application {

    public boolean isFirst = true;//判断软件是不是第一次打开
    public int actionbarHeight = 0;

    public boolean isLoginAutoBox = false;
    public boolean isRememberBox=false;
    public boolean isCardLoginAuto = false;
    public boolean isCardRemember = false;

    public boolean isLoginSuccess = false;
    public boolean isCardLoginSuccess = false;
    public boolean photoHasChange = false;
    public int screenWidth = 0;
    public int screenHeight = 0;

    public String xuehao = "";
    public String mima = "";
    public String name = "";
    public String cardXuehao = "";
    public String cardMima = "";

    public long cardLoginTime = 0;//记录饭卡登陆时间

    public boolean needReadSet = true;
    public boolean yunBaSendTo = false;

    public String urlDatabase = "http://www.codeevery.com/dingding/MyServlet";//数据库服务器url
    final public String emptyRoomUrl = "http://jw.zzu.edu.cn/scripts/freeroom/freeroom.dll/mylogin";//查询空教室的post请求网址
    final public String changePasswordUrl = "http://jw.zzu.edu.cn/scripts/ChnPW.dll/student";

    public Map<String,String> cardCookieMap;
    public String checkText = "";
    public String formAction = "";
    public int moneyNum = 0;

    public String webStr = "";



    public void hideKeyboard(View v)
    {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if ( imm.isActive( ) ) {
            imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
    }

    //显示虚拟键盘
    public void showKeyboard(View v)
    {
        InputMethodManager imm = ( InputMethodManager ) v.getContext( ).getSystemService( Context.INPUT_METHOD_SERVICE );
        imm.showSoftInputFromInputMethod(v.getApplicationWindowToken(),0);
    }

    //下面是推送，和推送成功要做的事情
    public void onCreate() {
        String pindao[] = new String[]{
                "admin",//推送普通消息的频道名称
                "topNews",//置顶新闻
                "news",//新闻制定频道,新闻推送的是标题,部分内容和网址
                "update",//更新频道名称
                "sendTo",//强制更新所有账号的上传状态，广播
        };

        super.onCreate();
        YunBaManager.start(getApplicationContext());

        YunBaManager.subscribe(getApplicationContext(), pindao, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken arg0) {
            }

            @Override
            public void onFailure(IMqttToken arg0, Throwable arg1) {
                //System.out.println("Subscribe topic failed:" + arg1.getMessage());
            }
        });
    }
    //下面是加密解密的代码
    //加密解密的密钥是 符号&
    //加密
    public static String encod(String str){
        //byte[] miByte = key.getBytes(charset);
        final Charset charset = Charset.forName("UTF-8");
        char key = '&';

        try {
            str = URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] strByte = str.getBytes(charset);
        int size = strByte.length;

        for(int i=0;i<size;i++){
            strByte[i] = (byte) (strByte[i]^key);
        }
        String temp = "";
        try {
            temp = new String(strByte,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static int dpToPx(Context context,float dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale + 0.5f);
    }

    public static String dencod(String str){
        final Charset charset = Charset.forName("UTF-8");
        char key = '&';

        byte[] cunByte = str.getBytes(charset);
        int size = cunByte.length;
        for(int i =0;i<size;i++){
            cunByte[i] = (byte) (cunByte[i]^key);
        }
        String temp = "";
        try {
            temp = new String(cunByte,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        try {
            temp = URLDecoder.decode(temp, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return temp;
    }
}
