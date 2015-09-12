package com.codeevery.CheckGrade;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.codeevery.login.SendInfoToService;
import com.codeevery.zzudingding.R;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.codeevery.NetGetPost.VolleyErrorHelper;
import com.codeevery.application.AllObject;
import com.codeevery.application.WriteToFile;

/**
 * Created by songchao on 15/8/6.
 */
public class ChangePassword extends Activity {
    EditText oldPassword, newPassword1, newPassword2;
    Button startChange;
    ImageButton backButton;
    AlertDialog alertDialog;
    RequestQueue requestQueue;
    String url;
    AllObject setting;
    String nianji, xuehao, oldmima, newmima1, newmima2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);
        setting = (AllObject) getApplication();
        requestQueue = Volley.newRequestQueue(this);
        url = setting.changePasswordUrl;
        startChange = (Button) findViewById(R.id.change_start_button);
        oldPassword = (EditText) findViewById(R.id.editText_old_password);
        newPassword1 = (EditText) findViewById(R.id.editText_new_password_one);
        newPassword2 = (EditText) findViewById(R.id.editText_new_password_two);
        backButton = (ImageButton) findViewById(R.id.change_password_back_button);
        if (setting.isLoginSuccess) {
            nianji = setting.xuehao.substring(0, 4);
            xuehao = setting.xuehao;
        } else {

        }
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        startChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldmima = oldPassword.getText().toString();
                newmima1 = newPassword1.getText().toString();
                newmima2 = newPassword2.getText().toString();
                if (newmima1.equals(newmima2)) {
                    if (newmima1.equals("") || oldmima.equals("")) {
                        showDialog("输入不能为空哦");
                    } else if (newmima1.substring(0, 1).equals(" ") || newmima1.substring(newmima1.length() - 1, newmima1.length()).equals(" ")) {
                        showDialog("首尾不能为空格哦");
                    } else if (newmima1.length() > 16 || newmima1.length() < 6) {
                        showDialog("密码位数不对哦");
                    } else if (newmima1.contains("+")) {
                        showDialog("密码不能包含+号");
                    } else if (!oldmima.equals(setting.mima)) {
                        showDialogWidthCancel("你的旧密码和登陆教务系统的密码不一致，是否重新输入或返回登录");
                    } else {
                        doPost(url);
                    }
                } else {//两次输入密码不一致
                    showDialog("两次密码不一致");
                }
            }
        });
    }

    private void showDialog(final String str) {
        alertDialog = new AlertDialog.Builder(ChangePassword.this).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (str.contains("修改密码成功")) {
                    dialog.cancel();
                    finish();
                } else
                    dialog.cancel();
            }
        }).create();
        alertDialog.setTitle("提醒");
        alertDialog.setMessage(str);
        alertDialog.show();
    }

    private void showDialogWidthCancel(String str) {
        alertDialog = new AlertDialog.Builder(ChangePassword.this).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doPost(url);
                dialog.cancel();
            }
        }).create();
        alertDialog.setTitle("提醒");
        alertDialog.setMessage(str);
        alertDialog.show();
    }

    private void doPost(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.contains("系统没有找到你的信息")) {
                    showDialog("修改失败，可能原密码错误或新密码格式有问题");
                } else if (s.contains("修改密码成功")) {
                    //修改密码成功，写入本地文件和变量
                    WriteToFile writeToFile = new WriteToFile(ChangePassword.this);
                    writeToFile.editor.putString("mima",newmima1).putBoolean("sendTo",false);
                    new SendInfoToService(ChangePassword.this,setting.urlDatabase,writeToFile.editor,xuehao,newmima1,setting.name).doPost();
                    setting.mima = newmima1;
                    showDialog("恭喜！修改密码成功");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(ChangePassword.this, VolleyErrorHelper.getMessage(volleyError, ChangePassword.this), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("nianji", nianji);
                map.put("xuehao", xuehao);
                map.put("oldmima", oldmima);
                map.put("newmima1", newmima1);
                map.put("newmima2", newmima2);
                map.put("xkstep", "1");
                return map;
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parse;
                try {
                    parse = new String(response.data, "gb2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    parse = new String(response.data);
                }
                return Response.success(parse, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        requestQueue.add(stringRequest);
    }
}
