package com.codeevery.zzudingding;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.codeevery.application.AllObject;
import com.codeevery.mail.SendEmailMain;
import com.codeevery.myElement.myDialog;

/**
 * Created by songchao on 15/8/24.
 */
public class FeedBack extends Activity {
    EditText content;
    Button submit;
    myDialog dialog;
    AllObject setting;
    TextView title;
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed_back);
        setting = (AllObject) getApplication();

        //初始化
        dialog = new myDialog(FeedBack.this);
        content = (EditText) findViewById(R.id.feed_back_content);
        submit = (Button) findViewById(R.id.feed_back_submit);
        title = (TextView) findViewById(R.id.title);
        title.setText("反馈");
        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what==1) {
                    if (dialog != null) {
                        dialog.hideProgressDialog();
                        dialog.showDialogWithSure("已经收到您的反馈,我们会给您回复哒","确定");
                    }
                }else if(msg.what== -1){
                    dialog.showDialogWithSure("发送失败了,可能是网络不好,再发一遍试试吧","好的");
                }
            }
        };
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentText = content.getText().toString();
                if(contentText==null||contentText.equals("")){
                    dialog.showDialogWithSure("内容不能为空哦","好的");
                    return;
                }else if(contentText.length()<5){
                    dialog.showDialogWithSure("再多给点建议吧,5个字以内太少了啊","好的");
                    return;
                }
                new SendEmailMain("小丁丁 反馈",contentText,setting.xuehao,handler).start();
                dialog.showProgressDialog("正在发送..");
            }
        });
    }
}
