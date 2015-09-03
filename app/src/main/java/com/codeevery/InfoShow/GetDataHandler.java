package com.codeevery.InfoShow;

import android.os.Handler;
import android.os.Message;

import java.util.Map;

import com.codeevery.myElement.myDialog;

/**
 * Created by songchao on 15/8/18.
 */
public class GetDataHandler extends Handler {

    myDialog dialog;
    Map<String,String> cookieMap;
    GetDataHandler(myDialog dialog,Map<String,String> cookieMap){
        this.dialog = dialog;
        this.cookieMap = cookieMap;
    }

    Do aDo;
    public void setOnDo(Do aDo){
        this.aDo = aDo;
    }
    interface Do {
        public void onDo(String str);
    }

    @Override
    public void handleMessage(Message msg) {
        if(msg.what == 1){
            String result = msg.getData().getString("result");
            //解析获得的json
            result = result.replace("root","\"root\"").replace("totalProperty","\"totalProperty\"");
            aDo.onDo(result);
        }
        else if(msg.what == -1){
            dialog.showDialogWithSure("服务器有问题,请稍后重试","确定");
        }
        else if(msg.what == -2){
            dialog.showDialogWithSure("连接超时,请稍后重试","确定");
        }
        else{
            dialog.showDialogWithSure("连接异常,请稍后重试","确定");
        }
    }

}
