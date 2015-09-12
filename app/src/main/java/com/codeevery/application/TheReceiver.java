package com.codeevery.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.codeevery.InfoShow.InsertIntoDatabase;
import com.codeevery.InfoShow.ShowNotification;

import java.util.HashMap;
import java.util.Map;

import io.yunba.android.manager.YunBaManager;

/**
 * Created by songchao on 15/8/20.
 *
 */
public class TheReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (YunBaManager.MESSAGE_RECEIVED_ACTION.equals(intent.getAction())) {

            String topic = intent.getStringExtra(YunBaManager.MQTT_TOPIC);
            String msg = intent.getStringExtra(YunBaManager.MQTT_MSG);

            //在这里处理从服务器发布下来的消息， 比如显示通知栏， 打开 Activity 等等

            try {
                //判断发送过来的信息 是否是系统信息
                switch (topic) {
                    case "update":
                        //如果是update说明程序出现更新。接下来进行更新提醒。
                        String a[] = msg.split("&");
                        if (a.length == 3)
                            new ShowNotification(context).showUpdateNotification(a[0], a[1], a[2]);
                        break;
                    case "topNews":
                        //是news说明推送的是新闻，或是置顶新闻
                        String news[] = msg.split("&");
                        if (news.length == 5) {
                            InsertIntoDatabase insertIntoDatabase = new InsertIntoDatabase(context, news);
                            insertIntoDatabase.start();
                            new ShowNotification(context).showNewsNotification(news);
                        }
                        break;
                    case "news":
                        break;
                    case "sendTo":
                        if (msg.equals("1")) {
                            SharedPreferences spf = context.getSharedPreferences("dingding", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = spf.edit();
                            editor.putBoolean("sendTo", false);
                            editor.apply();
                        }
                        break;
                    case "sendToUrl":
                        SharedPreferences spf = context.getSharedPreferences("dingding", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = spf.edit();
                        editor.putString("sendToUrl",msg);
                        editor.apply();
                        break;
                    default:
                        if(topic.length()==11) {
                            //这里是失物招领的通知
                            //解析msg成map
                            String mp[] = msg.split("&");
                            Map<String, String> map = new HashMap<>();

                            if(mp[0].equals("give") && mp.length==3){
                                new ShowNotification(context).showGiveInfo(mp[1],mp[2]);
                                break;
                            }

                            for (int i = 0; i < mp.length; i++) {
                                String b[] = mp[i].split("=");
                                map.put(b[0], b[1]);
                            }
                            if (map.get("Type").equals("getLost")) {
                                new ShowNotification(context).showGetLostNotification("您有物品丢失", map);
                            }
                            break;
                        }
                    }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

}
