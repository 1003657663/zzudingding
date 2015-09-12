package com.codeevery.login;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codeevery.CheckGrade.ChangePassword;
import com.codeevery.CheckGrade.CheckGradeActivity;
import com.codeevery.CheckGrade.EmptyRoom;
import com.codeevery.myElement.myDialog;
import com.codeevery.zzudingding.R;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

import com.codeevery.NetGetPost.GetInternetState;
import com.codeevery.application.AllObject;
import com.codeevery.myElement.GetPostThread;

import io.yunba.android.manager.YunBaManager;

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
public class LoginActivity extends Activity {
	private EditText usename;
	private EditText password;
	private Button submit,reset,help;
	private static Handler handler;
	private String noSureNianji,noSureXuehao,noSureMima;
	private CheckBox remberBox;
	private CheckBox loginAutoBox;
	private ConnectivityManager cm;
	private AllObject setting;
	private ImageButton backButton;
	private int afterDoWhich;
	private myDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist);
		Intent intent = this.getIntent();
		afterDoWhich = intent.getIntExtra("afterDoWhich",0);
		usename = (EditText) findViewById(R.id.usename);
		password = (EditText) findViewById(R.id.password);
		password.setHint("查成绩密码");
		submit = (Button) findViewById(R.id.submit);
		reset = (Button) findViewById(R.id.reset);
		remberBox = (CheckBox) findViewById(R.id.remberBox);
		loginAutoBox = (CheckBox) findViewById(R.id.loginAutoBox);
		backButton = (ImageButton) findViewById(R.id.login_back_button);
		setting = (AllObject) getApplication();
		help = (Button) findViewById(R.id.login_help_button);
		dialog = new myDialog(LoginActivity.this);

		help.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showdialog("教务密码：\n大一开学发的的10位密码,或者是您修改过的密码");
			}
		});

		backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		//读取选择状态
		if(setting.isRememberBox)
			remberBox.setChecked(true);
		else
			remberBox.setChecked(false);
		if(setting.isLoginAutoBox)
			loginAutoBox.setChecked(true);
		else
			loginAutoBox.setChecked(false);
		//为loginAutonBox添加监听事件，如果选中此选框，那么就自动选择remberBox
		loginAutoBox.setOnCheckedChangeListener(new isCheck());
		remberBox.setOnCheckedChangeListener(new isCheck());

		usename.setText(setting.xuehao);
		if(setting.isRememberBox){
			password.setText(setting.mima);
		}
		
		//给按钮添加监听类
		submit.setOnClickListener(new MyListener());
		reset.setOnClickListener(new MyListener());

		cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

	}


	private class isCheck implements CompoundButton.OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()){
				case R.id.loginAutoBox:
					if(isChecked == true) {
						remberBox.setChecked(true);
						setting.isLoginAutoBox = true;
					}
					else {
						setting.isLoginAutoBox = false;
					}
					break;
				case R.id.remberBox:
					if(isChecked == true) {
						setting.isRememberBox = true;
					}
					else{
						setting.isRememberBox = false;
						loginAutoBox.setChecked(false);
					}
					break;
				default:
					break;
			}
		}
	}
	
	private void showdialog(String state){
		dialog.showDialogWithSure(state,"确定");
	}
	//监听类
	class MyListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			if(v.getId()==R.id.reset){
				usename.setText("");
				password.setText("");
				return;
			}
			//如果点击的是登陆按钮，那么进行一下动作
			//首先判断网络情况
			GetInternetState getInternetState = new GetInternetState(cm);
			if(!getInternetState.isNetConnected()){
				showdialog("亲,您的网络有问题哦");
				return;
			}
			else {
				noSureXuehao = usename.getText().toString();
				noSureMima = password.getText().toString();
				//如果学号或者密码为空弹出警告
				if (noSureXuehao.isEmpty() || noSureMima.isEmpty()) {
					showdialog("学号和密码不能为空哦");
					return;
				} else
					noSureNianji = usename.getText().toString().substring(0, 4);//如果学号不为空，那么截取前4位
				//如果登陆失败ui更新线程弹出警告
				final ProgressDialog progressDialog = showProgressDialog();
				progressDialog.show();
				String url="http://jw.zzu.edu.cn/scripts/stuinfo.dll/check";
				Map<String,String> map = new HashMap<>();
				map.put("nianji",noSureNianji);
				map.put("xuehao",noSureXuehao);
				map.put("mima", noSureMima);

				LoginHandler loginHandler = new LoginHandler(progressDialog);
				GetPostThread loginThread = new GetPostThread(loginHandler,url,null,false,map);
				loginThread.setEncoding("gb2312");
				loginThread.start();
			}
		}
	}

	class LoginHandler extends Handler {
		ProgressDialog progressDialog;
		LoginHandler(ProgressDialog progressDialog){
			this.progressDialog = progressDialog;
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO 自动生成的方法存根
			progressDialog.cancel();
			if (msg.what == -1) {//0代表登陆是失败，弹出警告框
				showdialog("服务器有问题,稍后再试哦");
			}
			else if(msg.what == -2){
				showdialog("登陆超时,请稍后再试哦");
			}
			else if(msg.what == -3){
				showdialog("异常错误,请重试");
			}
			else if (msg.what == 1) {//1代表登陆成功跳转
				String result = msg.getData().getString("result");

				Document document = Jsoup.parse(result);
				String imgSrc = document.select("img[alt=照片]").attr("src");

				if(imgSrc==""||imgSrc==null){
					//登陆失败
					showdialog("登陆失败,请检查账号密码");
				}
				else {
					//登陆成功
					//获取姓名
					String webStr = "";
					String studentName = document.select("font[color=#0000FF").eq(1).text().substring(7);
					webStr = webStr + "<div>"
							+ "<img src='" + imgSrc + "'/></div>";

					Elements td = document.select("td");
					for (org.jsoup.nodes.Element t : td) {
						if (t.text() != "" && t.text() != null)
							webStr = webStr + "<p>" + t.text() + "</p>";
					}
					webStr = "<html><head>"
							+ "<meta http-equiv=Content-Type content='text/html; charset=utf-8'></head><body>"
							+ webStr
							+ "</body></html>";
					setting.webStr = webStr;


					//如果选择了记住密码那么就写到数据库里，并且发送给服务器
					SharedPreferences pdf = getSharedPreferences("dingding", 0);
					final SharedPreferences.Editor editor = pdf.edit();

					editor.putBoolean("isRememberBox", remberBox.isChecked());
					editor.putBoolean("isLoginAutoBox", loginAutoBox.isChecked());
					editor.apply();
					try {
						if (setting.xuehao.equals(noSureXuehao)) {
							if (!setting.mima.equals(noSureMima)) {
								//如果密码 不同的话，那么，写入密码，且发送重置标志位
								//发送给服务器
								String passMima = AllObject.encod(noSureMima);
								editor.putString("mima", passMima);
								editor.putBoolean("sendTo", false);
								editor.commit();
								new com.codeevery.login.SendInfoToService(getBaseContext(), setting.urlDatabase, editor, noSureXuehao, noSureMima, studentName).doPost();
							}

						} else {
							//重置所有数据
							//发送给服务器
							setYunBaName(noSureXuehao);
							new com.codeevery.login.SendInfoToService(getBaseContext(), setting.urlDatabase, editor, noSureXuehao, noSureMima, studentName).doPost();
							String passMima = AllObject.encod(noSureMima);
							String passXuehao = AllObject.encod(noSureXuehao);
							String passName = AllObject.encod(studentName);
							editor.putString("mima", passMima);
							editor.putString("xuehao", passXuehao);
							editor.putString("name", passName);
							editor.putBoolean("sendTo", false);
							editor.apply();
						}
						//如果登陆成功，就把登陆名和密码写到静态变量中
						setting.xuehao = noSureXuehao;
						setting.mima = noSureMima;
						setting.name = studentName;

					}catch (NullPointerException ex){
						ex.printStackTrace();
					}
					//向推送服务器发送信息，设定推送别名为学号

					Toast.makeText(LoginActivity.this, studentName + "!  欢迎来到小丁丁!", Toast.LENGTH_LONG).show();
					setting.isLoginSuccess = true;
					finish();
				}
			}
		}
	}

	private void setYunBaName(String yunBaname){
		YunBaManager.setAlias(LoginActivity.this,yunBaname , new IMqttActionListener() {
			@Override
			public void onSuccess(IMqttToken iMqttToken) {
				//发送成功，转换标志位
				setting.yunBaSendTo = true;
				System.out.println("设定别名 成功");
			}

			@Override
			public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
				setting.yunBaSendTo = false;
			}
		});
		YunBaManager.subscribe(LoginActivity.this,setting.xuehao,null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(setting.isLoginSuccess)
			jumpToOtherPage();
	}

	private void jumpToOtherPage(){
		Intent intent = new Intent();
		Context context = LoginActivity.this;
		switch (afterDoWhich) {
			case 0:
				return;
			case 1:
				intent.setClass(context, CheckGradeActivity.class);
				break;
			case 2:
				intent.setClass(context, EmptyRoom.class);
				break;
			case 3:
				intent.setClass(context, ChangePassword.class);
				break;
			case 4:
				intent.setClass(context, GetLostActivity.class);
				break;
			default:
				break;
		}
		context.startActivity(intent);
	}

	private ProgressDialog showProgressDialog(){
		ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
		progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		progressDialog.setMessage("正在登陆，稍后哦");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		return progressDialog;
	}

}
