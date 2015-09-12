package com.codeevery.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codeevery.zzudingding.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeevery.NetGetPost.DoPostGet;
import com.codeevery.NetGetPost.VolleyErrorHelper;
import com.codeevery.application.AllObject;
import com.codeevery.myElement.GetPostThread;
import com.codeevery.myElement.myDialog;

import io.yunba.android.manager.YunBaManager;

/**
 * Created by songchao on 15/8/13.
 */
public class GetLostIWill extends Activity {
    ImageButton back;
    Button submit;
    ImageView checkNum;
    TextView topTitle;
    EditText title, address, contactPeople, phoneNum, qqNum, email, mainContent, checkTextEdit;
    String url = "http://szhq.zzu.edu.cn/home/SaveGetLost_ssd";
    boolean isLostGet;
    String Typee = "";
    myDialog dialog;
    AllObject setting;
    EditText xuehao,name;
    TextView xuehaoDes,nameDes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_lost_i_will);
        Intent intent = getIntent();

        setting = (AllObject) getApplication();
        isLostGet = intent.getBooleanExtra("isLostGet", false);

        back = (ImageButton) findViewById(R.id.get_lost_will_back);
        submit = (Button) findViewById(R.id.get_will_submit);
        topTitle = (TextView) findViewById(R.id.get_lost_will_title);
        title = (EditText) findViewById(R.id.get_will_edit_title);
        address = (EditText) findViewById(R.id.get_will_edit_address);
        contactPeople = (EditText) findViewById(R.id.get_will_edit_contactpeople);
        phoneNum = (EditText) findViewById(R.id.get_will_edit_phonenum);
        qqNum = (EditText) findViewById(R.id.get_will_edit_qq);
        email = (EditText) findViewById(R.id.get_will_edit_email);
        mainContent = (EditText) findViewById(R.id.get_will_edit_content);
        xuehao = (EditText) findViewById(R.id.get_will_xuehao);
        name = (EditText) findViewById(R.id.get_will_name);
        xuehaoDes = (TextView) findViewById(R.id.get_will_xuehaoDes);
        nameDes = (TextView) findViewById(R.id.get_will_nameDes);
        if (isLostGet) {
            topTitle.setText("我要招领");
            Typee = "招领";
        } else {
            topTitle.setText("我要报失");
            xuehao.setVisibility(View.GONE);
            name.setVisibility(View.GONE);
            xuehaoDes.setVisibility(View.GONE);
            nameDes.setVisibility(View.GONE);
            Typee = "报失";
        }
        dialog = new myDialog(this);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkText()) {
                    if (!toXuehao.equals("") || !toName.equals("")) {
                        sendOut();
                    }
                    showCheckNumDialog();
                }
            }
        });
    }

    class GetCheckNum extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                Bitmap bitmap = (Bitmap) msg.obj;
                checkNum.setImageBitmap(bitmap);
                dialog.hideProgressDialog();
            } else {
                dialog.showDialogWithSure("验证码获取错误,请重新进入此页面", "确定");
            }
        }
    }

    //第三步请求最后的cookie
    private class GetCook extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 5) {
                String url = "http://szhq.zzu.edu.cn/home/SaveGetLost_ssd";
                fabu(url);
            } else {
                dialog.showDialogWithSure("登陆出现错误,再试一遍吧", "确定");
            }
        }
    }


    //第二步，输入验证码，点击提交，进入登陆程序，获得cookie
    class LoginHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                String result = msg.getData().getString("result");
                if (result.contains("欢迎你访问")) {
                    //登陆成功
                    //*******************在这里登陆成功之后，解析获取下一页的网址，继续请求
                    Pattern pattern = Pattern.compile("window.location='(.*?)'");
                    Matcher matcher = pattern.matcher(result);
                    String nextUrl = "";
                    if (matcher.find()) {
                        nextUrl = matcher.group(1);
                    }
                    //这里继续请求获取cookie
                    GetCook getCook = new GetCook();
                    GetPostThread getC = new GetPostThread(getCook, nextUrl, null, true, null);
                    getC.setEncoding("utf-8");
                    getC.setOnlyCookie(true);
                    getC.start();
                    checkAlert.cancel();
                } else if (result.contains("你未输入正确的验证码")) {
                    dialog.showDialogWithSure("验证码错误,或超时", "确定");
                    getCheckBitmap();
                } else {
                    dialog.showDialogWithSure("登陆出现错误,请重试", "确定");
                }
            } else if (msg.what == -1) {//登陆失败
                dialog.showDialogWithSure("发布服务器出错,请重试", "确定");
            } else if (msg.what == -2) {
                dialog.showDialogWithSure("请求超时,请重试", "确定");
            } else {
                dialog.showDialogWithSure("请求异常,请重试", "确定");
            }
        }

    }

    String Title;
    String Address;
    String Linkman;
    String Mobile;
    String QQ;
    String Email;
    String Description;
    String checkText;
    String toXuehao;
    String toName;

    public boolean checkText() {
        Title = title.getText().toString();
        Address = address.getText().toString();
        Linkman = contactPeople.getText().toString();
        Mobile = phoneNum.getText().toString();
        QQ = qqNum.getText().toString();
        Email = email.getText().toString();
        Description = mainContent.getText().toString();
        toXuehao = xuehao.getText().toString();
        toName = name.getText().toString();

        if (Title.isEmpty() || Title.length() < 5 || Title.length() > 30) {
            dialog.showDialogWithSure("标题过长或过短", "确定");
            return false;
        }

        if (Address.isEmpty() || Address.length() < 5 || Address.length() > 150) {
            dialog.showDialogWithSure("地址过长或过短", "确定");
            return false;
        }

        if (Linkman.isEmpty() || !isAllChinese(Linkman) || Linkman.length() < 2 || Linkman.length() > 10) {
            dialog.showDialogWithSure("联系人必须是汉字,2-10个之间", "确定");
            return false;
        }

        if (Mobile.isEmpty() || Mobile.length() != 11 || !isAllNum(Mobile)) {
            dialog.showDialogWithSure("电话号码格式有误", "确定");
            return false;
        }

        if (QQ.isEmpty()) {
            QQ = " ";
        } else if (!isAllNum(QQ) || QQ.length() > 15 || QQ.length() <= 5) {
            dialog.showDialogWithSure("QQ号码格式有误", "确定");
            return false;
        }

        if (Email.isEmpty() || !Email.contains("@")) {
            dialog.showDialogWithSure("电子邮件格式有误", "确定");
            return false;
        }

        if (Description.isEmpty()) {
            dialog.showDialogWithSure("描述不可以为空", "确定");
            return false;
        }
        return true;
    }


    public Map<String, String> getMap() {
        Map<String, String> map = new HashMap<String, String>();

        map.put("Type", Typee);
        String ID = " ";
        String IP = " ";
        String IsCheck = " ";
        String IsLock = " ";
        String Clicks = " ";
        String Checker = " ";
        String CheckTime = " ";
        String UID = " ";
        String Counts = " ";

        map.put("Title", Title);
        map.put("Address", Address);
        map.put("Linkman", Linkman);
        map.put("Mobile", Mobile);
        map.put("QQ", QQ);
        map.put("Email", Email);
        map.put("Description", Description);
        map.put("ID", ID);
        map.put("IP", IP);
        map.put("IsCheck", IsCheck);
        map.put("IsLock", IsLock);
        map.put("Clicks", Clicks);
        map.put("Checker", Checker);
        map.put("CheckTime", CheckTime);
        map.put("UID", UID);
        map.put("Counts", Counts);
        return map;
    }

    public boolean isAllNum(String str) {
        for (int i = 0; i < str.length(); i++) {
            char t = str.charAt(i);
            if (t < '0' || t > '9') {
                return false;
            }
        }
        return true;
    }

    public boolean isAllChinese(String string) {
        for (int i = 0; i < string.length(); i++) {
            char t = string.charAt(i);
            if ((t >= '\u4e00' && t <= '\u9fa5') || (t >= '\uf900' && t <= '\ufa2d')) {
            } else {
                return false;
            }
        }
        return true;
    }

    //自定义一个dialog用来显示验证码，防止验证码过期
    AlertDialog checkAlert;

    private void showCheckNumDialog() {
        LayoutInflater inflater = LayoutInflater.from(GetLostIWill.this);
        View checkView = inflater.inflate(R.layout.check_num_layout, null);
        checkNum = (ImageView) checkView.findViewById(R.id.get_will_checkNum);
        checkTextEdit = (EditText) checkView.findViewById(R.id.get_will_checkText);
        Button checkSubmit = (Button) checkView.findViewById(R.id.check_num_submit);

        checkSubmit.setOnClickListener(new submitListener());
        AlertDialog.Builder checkDialog = new AlertDialog.Builder(GetLostIWill.this);
        checkDialog.setView(checkView);
        checkAlert = checkDialog.create();
        checkAlert.show();
        getCheckBitmap();
    }

    private void getCheckBitmap() {
        //第一步获取验证码图片
        dialog.showProgressDialog("正在获取验证码图片,请稍等..");
        String getImageUrl = "http://acc.zzu.edu.cn/teass/zzjlogin3d.dll/zzjgetimg?ids=" + Math.round(Math.random() * 10000);
        GetCheckNum getCheckNum = new GetCheckNum();
        new GetPostThread(getCheckNum, getImageUrl, null, true).start();
    }

    //点击之后发布
    class submitListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //点击提交之后再获取验证码，如果输入之后再提交
            checkText = checkTextEdit.getText().toString();
            if (checkText.length() != 4) {
                dialog.showDialogWithSure("验证码长度为4位", "确定");
                return;
            }

            String loginUrl = "http://acc.zzu.edu.cn/teass/zzjlogin6.dll/login";
            Map<String, String> loginMap = new HashMap<String, String>();
            loginMap.put("uid", setting.xuehao);
            loginMap.put("pw", setting.mima);
            loginMap.put("login", "+%B5%C7%C2%BC+");
            loginMap.put("verc", checkText);
            loginMap.put("fun_id", "hq419");

            LoginHandler loginHandler = new LoginHandler();
            GetPostThread loginThread = new GetPostThread(loginHandler, loginUrl, null, false, loginMap);
            loginThread.setEncoding("gb2312");
            dialog.showProgressDialog("正在发布,请稍后..");
            loginThread.start();
        }
    }

    public void fabu(String url) {
        //下面是发布到校园网里的程序
        RequestQueue requestQueue = Volley.newRequestQueue(GetLostIWill.this);
        final StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                dialog.hideProgressDialog();
                if (s.contains("ok")) {
                    dialog.showDialogWithSure("发布成功", "确定");
                } else {
                    dialog.showDialogWithSure("发布失败，重新试一下吧", "确定");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(GetLostIWill.this, VolleyErrorHelper.getMessage(volleyError, GetLostIWill.this), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("ServiceUserCookies", "UserID=" + setting.xuehao + "&UserName=" + setting.name + "&UserType=student");
                return map;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map;
                map = getMap();
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void sendOut(){
        //下面是发布到推送平台和服务器后台的程序
        Map<String,String> map = new HashMap<>();
        map.put("Type","getLost");
        map.put("Title", Title);
        map.put("Address", Address);
        map.put("Linkman", Linkman);
        map.put("Mobile", Mobile);
        map.put("QQ", QQ);
        map.put("Email", Email);
        map.put("Description", Description);

        if(toXuehao.length()==11 && isAllNum(toXuehao)) {
            map.put("Description",map.get("Description")+"\r\n失主学号:"+toXuehao);
            Iterator<Map.Entry<String,String>> it = map.entrySet().iterator();
            StringBuilder sb = new StringBuilder();
            while(it.hasNext()){
                Map.Entry<String,String> entry = it.next();
                sb.append(entry.getKey()+"="+entry.getValue()+"&");
            }
            sb.substring(0,sb.length()-1);
            YunBaManager.publishToAlias(GetLostIWill.this,toXuehao,sb.toString(),null);
        }else{
            if(toName.length()>0 && toName.length()<=5){

            }
            //说明这里是名字，需要用服务器查询学号。以后再写这部分
        }
    }

}
