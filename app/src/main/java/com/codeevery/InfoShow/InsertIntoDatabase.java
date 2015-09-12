package com.codeevery.InfoShow;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;

import com.codeevery.application.AllObject;
import com.codeevery.myElement.DatabaseHelper;
import com.codeevery.zzudingding.tabFragment2;

/**
 * Created by songchao on 15/8/21.
 */
public class InsertIntoDatabase extends Thread {
    Context context;
    AllObject setting;
    String a[];

    //新的构造方法专门为新闻准备,传入的是字符串数组
    public InsertIntoDatabase(Context context, String a[]) {
        this.context = context;
        setting = (AllObject) context.getApplicationContext();
        this.a = a;
    }

    @Override
    public void run() {
        insertNews();
        super.run();
    }

    public void insertNews() {
        try {
            DatabaseHelper databaseHelper = new DatabaseHelper(context, "dingding", null, 1);
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            //得到数据库的长度
            String selSql = "select max(id) as maxId from news";
            Cursor cursor = db.rawQuery(selSql, null);
            int maxId = 0;
            if (cursor.moveToFirst()) {
                maxId = cursor.getInt(cursor.getColumnIndex("maxId"));
            }
            if (maxId >= 5) {//如果大于等于5那么删掉第一个在最后插入数据
                String delSql = "delete from news where id=" + (maxId - 4);
                db.execSQL(delSql);
            }
            db.execSQL("insert into news values (null,'" + a[0] + "','" + a[1] + "','" + a[2] + "','" + a[3] + "','" + a[4] + "')");

            while (tabFragment2.newsHandler == null) {
                this.wait(200);
            }
            if (tabFragment2.newsHandler != null) {
                Handler handler = tabFragment2.newsHandler;
                handler.sendEmptyMessage(6);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
