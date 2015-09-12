package com.codeevery.InfoShow;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.codeevery.application.SerializableMap;
import com.codeevery.login.GetLostMore;
import com.codeevery.myElement.DatabaseHelper;
import com.codeevery.zzudingding.MainActivity;
import com.codeevery.zzudingding.R;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by songchao on 15/8/20.
 */
public class ShowNotification {
    private NotificationManager manager;
    private Notification.Builder builder;
    private Context context;
    private SerializableMap serializableMap;

    public ShowNotification(Context context){
        this.context = context;
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(context);
    }

    public void showNewsNotification(String [] a){
        final int ID = 124;
        Intent intent =  new Intent();
        int doWhich = 1;
        String charaset = "gb2312";
        //转换获取的type得到类型编号和将要打开的页面的编码和网址
        switch (a[0]) {
            case "教务公告":
                doWhich = 1;
                charaset = "gb2312";
                break;
            case "通知公告":
                doWhich = 2;
                charaset = "utf-8";
                break;
            case "学术动态":
                doWhich = 3;
                charaset = "utf-8";
                break;
            case "教务要闻":
                doWhich = 4;
                charaset = "gb2312";
                break;
            case "招聘信息":
                doWhich = 5;
                charaset = "utf-8";
                break;
            default:
                break;
        }
        intent.putExtra("doWhich",doWhich);
        intent.putExtra("charaset",charaset);
        intent.putExtra("moreInfoUrl",a[4]);
        intent.setClass(context, ShowInfoMore.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,Intent.FILL_IN_ACTION);
        show(a[1],a[1],a[3],pendingIntent,ID);
    }

    public void showUpdateNotification(String title,String content,String url){
        final int ID = 123;
        //设置Intent
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(url);//设置跳转网址
        intent.setData(uri);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        show(title,title,content,pendingIntent,ID);
    }
    public void showGetLostNotification(String title,Map<String,String> map){
        //把map序列化
        serializableMap = new SerializableMap();
        serializableMap.setMap(map);
        //显示丢失物品的通知是ID 1
        final int ID = 122;
        String tickerText = "您丢失的物品有人捡到了";

        Intent intent = new Intent(context, GetLostMore.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isNotification", true);
        bundle.putSerializable("message", serializableMap);
        intent.putExtras(bundle);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        show(tickerText,title,map.get("Title"),pendingIntent,ID);
    }

    public void showGiveInfo(String title,String content){
        final int ID = 125;
        Intent intent =  new Intent();
        intent.setClass(context, GiveInfo.class);
        intent.putExtra("title",title);
        intent.putExtra("content",content);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        show(title,title,content,pendingIntent,ID);
    }

    private void show(String ticker,String title,String text,PendingIntent pendingIntent,final int ID){
        int smallIcon = R.mipmap.ic_launcher;
        int largeIcon = R.mipmap.ic_launcher;
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);
        builder.setSmallIcon(smallIcon);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), largeIcon));
        builder.setTicker(ticker);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setContentIntent(pendingIntent);
        manager.notify(ID,builder.getNotification());
    }
}
