package com.codeevery.InfoShow;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.codeevery.application.AllObject;
import com.codeevery.myElement.myDialog;
import com.codeevery.zzudingding.R;

/**
 * Created by songchao on 15/8/25.
 */
public class MyInfo extends Activity implements myDialog.SureButton{
    TextView title;
    WebView content;
    AllObject setting;
    myDialog dialog;
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_info);
        setting = (AllObject) getApplication();
        dialog = new myDialog(MyInfo.this);

        title = (TextView) findViewById(R.id.title);
        title.setText("个人信息");
        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(v -> finish());
        content = (WebView) findViewById(R.id.my_info_webview);
        if(setting.webStr.equals("")){
            dialog.setSureButton(this);
            dialog.showDialogWithSure("未能获取您的信息,请返回重新登陆教务中心获取","确定");
        }else {
            content.loadData(setting.webStr, "text/html;charset=utf-8", "UTF-8");
        }
    }

    @Override
    public void sureButtonDo() {
        finish();
    }
}
