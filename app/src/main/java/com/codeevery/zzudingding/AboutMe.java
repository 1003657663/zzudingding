package com.codeevery.zzudingding;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.codeevery.NetGetPost.DoPostGet;
import com.codeevery.myElement.myDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

/**
 * Created by songchao on 15/8/8.
 *
 */
public class AboutMe extends Activity implements DoPostGet.DoSomeThing,myDialog.SureButton{
    RelativeLayout relativeLayout;
    ImageButton back;

    Button update;
    myDialog dialog;
public class AboutMe extends Activity {
    RelativeLayout relativeLayout;
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me_layout);
        dialog = new myDialog(this);
        dialog.setSureButton(this);
        relativeLayout = (RelativeLayout) findViewById(R.id.topRelative);
        back = (ImageButton) findViewById(R.id.back);
        update = (Button) findViewById(R.id.check_update);
        relativeLayout = (RelativeLayout) findViewById(R.id.topRelative);
        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        relativeLayout.setBackgroundResource(R.color.transparent);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });
    }
    private void checkUpdate(){
        DoPostGet doPostGet = new DoPostGet(AboutMe.this);
        doPostGet.setInterface(AboutMe.this);
        String url = "http://www.codeevery.com/dingding/update";
        //String url = "http://www.baidu.com";
        String chaset = "utf-8";
        doPostGet.doGet(url, chaset);
    }

    private float getVersion(){
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(),0);
            String version = packageInfo.versionName;
            float ver = Float.valueOf(version);
            System.out.println("version:"+ver);
            return ver;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onDo(String str) {
        float version = getVersion();
        float newVersion = 0;
        if(!str.equals("")){
            newVersion = Float.valueOf(str);
        }
        //newVersion = 1.4f;
        if(newVersion>version){
            dialog.showDialogWithSureAndNo("发现新版本:"+newVersion+" 是否更新？","更新","下次再说");
        }
    }

    @Override
    public void sureButtonDo() {
        //--------点击确定按钮后开始更新
        //跳转到内置浏览器打开更新网址
        String updateUrl = "http://www.codeevery.com/dingding/download/郑大小丁丁.apk";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri = Uri.parse(updateUrl);
        intent.setData(uri);
        startActivity(intent);
    }
}
