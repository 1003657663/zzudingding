package com.codeevery.InfoShow;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codeevery.zzudingding.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeevery.NetGetPost.DoPostGet;
import com.codeevery.myElement.myDialog;

/**
 * Created by songchao on 15/8/10.
 */
public class ShowInfoMore extends Activity implements DoPostGet.DoSomeThing, myDialog.SureButton {
    TextView titleText, otherText;
    WebView contentView;
    String url, charaset;
    int doWhich;
    ImageButton backButton,jumpButton;
    DoPostGet doPostGet;
    myDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_info_more);
        titleText = (TextView) findViewById(R.id.info_more_title);
        otherText = (TextView) findViewById(R.id.info_more_other);
        contentView = (WebView) findViewById(R.id.show_info_content);
        //支持javascript
        contentView.getSettings().setJavaScriptEnabled(true);
        //安全设置
        contentView.removeJavascriptInterface("searchBoxJavaBredge_");
        // 设置可以支持缩放
        contentView.getSettings().setSupportZoom(true);
        // 设置出现缩放工具
        contentView.getSettings().setBuiltInZoomControls(true);
        contentView.getSettings().setDisplayZoomControls(false);
        //扩大比例的缩放
        //contentView.getSettings().setUseWideViewPort(true);
        //设置webView在当前窗口打开连接
        contentView.setWebViewClient(new WebClient());

        jumpButton = (ImageButton) findViewById(R.id.jump_to_chrome);
        backButton = (ImageButton) findViewById(R.id.show_info_backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        final Intent intent = getIntent();
        this.url = intent.getStringExtra("moreInfoUrl");
        this.charaset = intent.getStringExtra("charaset");
        this.doWhich = intent.getIntExtra("doWhich", 1);

        dialog = new myDialog(this);
        dialog.showProgressDialog("正在加载..");
        dialog.setSureButton(this);

        doPostGet = new DoPostGet(this);
        doPostGet.setInterface(this);
        doPostGet.doGet(url, charaset);

        jumpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                intent1.setAction(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(url);
                intent1.setData(uri);
                startActivity(intent1);
            }
        });
    }
    class WebClient extends WebViewClient{
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onDo(String str) {
        switch (doWhich) {
            case 1:
                onDo1(str);
                break;
            case 2:
                onDo2(str);
                break;
            case 3:
                onDo2(str);
                break;
            case 4:
                onDo1(str);
                break;
            case 5:
                onDo5(str);
                break;
            case 6:
                break;
            default:
                break;
        }
        dialog.hideProgressDialog();
    }

    private void onDo1(String s) {
        String title = "";
        String otherInfo = "";
        String content = "";
        Document document = Jsoup.parse(s);
        Elements tr;
        try {
            String con = document.getElementById("02").getElementsByTag("tr").first().html();
            content = "<html><head><style>body{line-height:200%;}</style></head><body style='background:#eeeeee'>" + con + "</body></html>";
        } catch (Exception ex) {
            Log.e("get content wrong", "wrong");
            content = "";
            contentView.loadUrl(url);
            Toast.makeText(ShowInfoMore.this, "因为无法解析此页面,内置浏览器打开,如果出现任何错误，点击右上方用手机浏览器打开", Toast.LENGTH_LONG).show();
            //dialog.showDialogWithSureAndNo("此页面无法解析，可能包含交互内容，是否用浏览器打开？","好的","取消");
        }
        contentView.loadData(content, "text/html; charset=utf-8", "utf-8");

        try {
            tr = document.body().getElementById("01").getElementsByTag("tr");
        } catch (Exception ex) {
            ex.printStackTrace();
            contentView.loadUrl(url);
            Toast.makeText(ShowInfoMore.this, "因为无法解析此页面,内置浏览器打开,如果出现任何错误，点击右上方用手机浏览器打开", Toast.LENGTH_LONG).show();
            //dialog.showDialogWithSureAndNo("此页面无法解析，可能包含交互内容，是否用浏览器打开？", "好的", "取消");
            return;
        }
        try {
            title = tr.get(0).text();
        } catch (Exception ex) {
            title = "";
        }
        try {
            otherInfo = tr.get(2).text();
        } catch (Exception ex) {
            otherInfo = "";
        }
        titleText.setText(title);
        otherText.setText(otherInfo);
    }

    private void onDo2(String s) {
        String title = "";
        String otherInfo = "";
        String content = "";
        Document document = Jsoup.parse(s);
        if(document.head().html().contains("refresh")){
            String jumpUrl = document.head().select("meta[http-equiv=refresh]").attr("content");
            Pattern pattern = Pattern.compile("url='(.*)'");
            Matcher matcher = pattern.matcher(jumpUrl);
            if(matcher.find()){
                jumpUrl = matcher.group(1);
            }
            doPostGet.doGet(jumpUrl, charaset);
            return;
        }
        Element body = document.body();
        String img = "";
        try {
            String con = body.getElementsByClass("zzj_5").first().outerHtml();
            if(body.html().contains("\"zzj_8a\"")) {
                Element zzj_8a = body.getElementsByClass("zzj_8a").first();
                if (zzj_8a.html().contains("img"))
                    img = zzj_8a.select("img[src]").first().outerHtml();
            }
            content = "<html><head><style>body{line-height:200%;}</style></head><body style='background:#eeeeee'><center>"+img+"</center>"+con+"</body></html>";
        } catch (Exception ex) {
            ex.printStackTrace();
            contentView.loadUrl(url);
            Toast.makeText(ShowInfoMore.this, "因为无法解析此页面,内置浏览器打开,如果出现任何错误，点击右上方用手机浏览器打开", Toast.LENGTH_LONG).show();
            return;
            //dialog.showDialogWithSureAndNo("此页面无法解析，可能包含交互内容，是否用浏览器打开？","好的","取消");
        }
        contentView.loadData(content, "text/html; charset=utf-8", "utf-8");

        try {
            title = body.getElementsByClass("zzj_3").first().text();
        } catch (Exception ex) {
            title = "";
        }
        try {
            otherInfo = body.getElementsByClass("zzj_4").first().text();
        } catch (Exception ex) {
            otherInfo = "";
        }
        titleText.setText(title);
        otherText.setText(otherInfo);
    }

    private void onDo5(String s){
        Element submain = Jsoup.parse(s).getElementsByClass("submain-article").first();
        String title = submain.getElementsByTag("h1").first().text();
        titleText.setText(title);
        submain.getElementsByTag("h1").first().remove();
        String content = "<html><head><style>body{line-height:200%;}</style></head><body style='background:#eeeeee'>"+submain.outerHtml()+"</body></html>";
        contentView.loadData(content,"text/html;charset=utf-8",charaset);
    }

    @Override
    public void sureButtonDo() {
        contentView.loadUrl(url);
    }
}
