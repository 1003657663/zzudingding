package com.codeevery.login;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codeevery.InfoShow.SpendMoney;
import com.codeevery.myElement.myDialog;
import com.codeevery.zzudingding.R;

import com.codeevery.NetGetPost.GetInternetState;
import com.codeevery.application.AllObject;

import java.sql.Time;

/*
*说明：
* LoginActivity类主管：
* 1. 获取输入的密码和学号
* 2. 记录是否记住密码或者自动登录
* 3. 启动查询密码的线程登陆教务系统，登陆成功，线程返回数据
* 4. 接收线程返回的数据，判断并写入数据库
* 5. 同时把设置信息，即自动登陆还是记住密码写入系统
* 6. 在启动此线程的时候就判断是自动登录还是记住密码等信息
* 7. 登陆后跳转到指定页面
* 未完成：
* 	登陆成功弹出的Toast优化
*   数据库，转发信息到服务器
*   记住密码和自动登陆状态的识别和写入
*/
public class FoodcardLoginActivity extends Activity {
    private EditText usename;
    private EditText password;
    private Button submit, reset,help;
    private myDialog dialog;
    private String noSureXuehao, noSureMima;
    private CheckBox remberBox;
    private CheckBox loginAutoBox;
    private ConnectivityManager cm;
    private AllObject setting;
    private ImageButton backButton;
    private TextView loginTitle;
    private int afterWhich;
    private CardLoginAuto cardLoginAuto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regist);
        Intent intent = getIntent();
        afterWhich = intent.getIntExtra("afterWhich", 0);
        usename = (EditText) findViewById(R.id.usename);
        password = (EditText) findViewById(R.id.password);
        password.setHint("初始身份证后六位");
        submit = (Button) findViewById(R.id.submit);
        reset = (Button) findViewById(R.id.reset);
        remberBox = (CheckBox) findViewById(R.id.remberBox);
        loginAutoBox = (CheckBox) findViewById(R.id.loginAutoBox);
        backButton = (ImageButton) findViewById(R.id.login_back_button);
        setting = (AllObject) getApplication();
        loginTitle = (TextView) findViewById(R.id.login_title);
        loginTitle.setText("校卡系统登录");
        dialog = new myDialog(FoodcardLoginActivity.this);
        help = (Button) findViewById(R.id.login_help_button);

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showdialog("初始密码是以下四个中任意一个：\n①身份证后六位，最后一位为x的，去掉x补0；②000000；③666666；④888888");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //读取选择状态
        if (setting.isCardRemember)
            remberBox.setChecked(true);
        else
            remberBox.setChecked(false);

        if (setting.isCardLoginAuto)
            loginAutoBox.setChecked(true);
        else
            loginAutoBox.setChecked(false);
        //为loginAutonBox添加监听事件，如果选中此选框，那么就自动选择remberBox
        loginAutoBox.setOnCheckedChangeListener(new isCheck());
        remberBox.setOnCheckedChangeListener(new isCheck());

        usename.setText(setting.cardXuehao);
        if (setting.isCardRemember) {
            password.setText(setting.cardMima);
        }

        //给按钮添加监听类
        submit.setOnClickListener(new MyListener());
        reset.setOnClickListener(new MyListener());

        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        //读取数据库里面的cookie
    }


    private class isCheck implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.loginAutoBox:
                    if (isChecked == true) {
                        remberBox.setChecked(true);
                        setting.isCardLoginAuto = true;
                    } else {
                        setting.isCardLoginAuto = false;
                    }
                    break;
                case R.id.remberBox:
                    if (isChecked == true) {
                        setting.isCardRemember = true;
                    } else {
                        setting.isCardRemember = false;
                        loginAutoBox.setChecked(false);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //网络请求的参数
    //cmd:already-registered
    //tabs1:already-registered
    //redirect:/web/guest/stu
    //rememberMe:false
    //http://ecard.zzu.edu.cn/c/portal/login

    //监听类
    class MyListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.reset) {
                usename.setText("");
                password.setText("");
                return;
            }
            //如果点击的是登陆按钮，那么进行一下动作
            //首先判断网络情况
            GetInternetState getInternetState = new GetInternetState(cm);
            if (!getInternetState.isNetConnected()) {
                showdialog("亲,您的网络有问题哦");
            } else {
                noSureXuehao = usename.getText().toString();
                noSureMima = password.getText().toString();
                //如果学号或者密码为空弹出警告
                if (noSureXuehao.isEmpty() || noSureMima.isEmpty()) {
                    showdialog("学号和密码不能为空哦");
                    return;
                }
                //开始登陆了

                final CardLoginAuto.ForthHandler.SuccessDo successDo = new CardLoginAuto.ForthHandler.SuccessDo() {
                    @Override
                    public void successDo() {
                        //-------------------------------------------------------------------这里是登陆成功的代码
                        //登陆成功

                        //如果选择了记住密码那么就写到数据库里，并且发送给服务器
                        try {
                            SharedPreferences pdf = FoodcardLoginActivity.this.getSharedPreferences("dingding", 0);
                            SharedPreferences.Editor editor = pdf.edit();

                            editor.putBoolean("isCardRemember", remberBox.isChecked());
                            editor.putBoolean("isCardLoginAuto", loginAutoBox.isChecked());
                            editor.commit();

                            if (setting.cardXuehao.equals(noSureXuehao)) {
                                if (!setting.cardMima.equals(noSureMima)) {
                                    //如果密码 不同的话，那么，写入密码，且发送重置标志位
                                    //发送给服务器************************这里需要改一下
                                    //new SendInfoToService(getBaseContext(),setting.urlDatabase,editor,setting.xuehao,setting.mima,setting.name).doPost();
                                    String tempMima = AllObject.encod(noSureMima);
                                    editor.putString("cardMima", tempMima);
                                    editor.putBoolean("cardSendTo", false);
                                    editor.commit();
                                }
                            } else {
                                //重置所有数据
                                //发送给服务器**********************这里需要改一下
                                //new SendInfoToService(getBaseContext(),setting.urlDatabase,editor,setting.xuehao,setting.mima,setting.name).doPost();
                                String tempMima = AllObject.encod(noSureMima);
                                String tempXuehao = AllObject.encod(noSureXuehao);
                                editor.putString("cardMima", tempMima);
                                editor.putString("cardXuehao", tempXuehao);
                                editor.putBoolean("cardSendTo", false);
                                editor.commit();
                            }

                            //如果登陆成功，就把登陆名和密码写到静态变量中
                            setting.cardXuehao = noSureXuehao;
                            setting.cardMima = noSureMima;
                            setting.isCardLoginSuccess = true;

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        Toast.makeText(FoodcardLoginActivity.this, "登陆成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                };

                //开始请求cookie
                CardLoginAuto.ThirdHandler.SuccessDo1 successDo1 = new CardLoginAuto.ThirdHandler.SuccessDo1() {
                    @Override
                    public void successDo1() {
                        cardLoginAuto.startCardLogin(noSureXuehao, noSureMima, successDo);
                    }
                };
                setting.cardCookieMap.clear();
                cardLoginAuto = new CardLoginAuto(setting, FoodcardLoginActivity.this, true);
                cardLoginAuto.getCheckText(successDo1);

            }
        }
    }

    @Override
    protected void onDestroy() {
        if (afterWhich != 0 && setting.isCardLoginSuccess) {
            Intent intent = new Intent();
            switch (afterWhich) {
                case 1:
                    Toast.makeText(FoodcardLoginActivity.this, "登陆成功，请再次点击查询余额", Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    intent.setClass(FoodcardLoginActivity.this, SpendMoney.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
        super.onDestroy();
    }

    private void showdialog(String state) {
        dialog.showDialogWithSure(state, "确定");
    }

}
