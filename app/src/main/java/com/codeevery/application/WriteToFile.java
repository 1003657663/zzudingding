package com.codeevery.application;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by songchao on 15/8/7.
 */
public class WriteToFile {
    public SharedPreferences spf;
    private Context context;
    public SharedPreferences.Editor editor;
    public WriteToFile(Context context){
        this.context = context;
        spf = context.getSharedPreferences("dingding",Context.MODE_PRIVATE);
        editor = spf.edit();
    }
}