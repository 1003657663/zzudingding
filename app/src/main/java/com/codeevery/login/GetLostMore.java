package com.codeevery.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.codeevery.application.SerializableMap;
import com.codeevery.zzudingding.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.codeevery.NetGetPost.DoPostGet;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by songchao on 15/8/12.
 */
public class GetLostMore extends Activity implements DoPostGet.DoSomeThing{
    TextView title,textContent;
    TextView[] text;
    ImageButton back;
    String url;
    DoPostGet doPostGet;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_lost_more);
        Intent intent = getIntent();
        url = intent.getStringExtra("lostMoreUrl");

        title = (TextView) findViewById(R.id.lost_more_title);
        textContent = (TextView) findViewById(R.id.lost_more_textcontent);
        textContent.setTextColor(getResources().getColor(R.color.secondary_text));

        text = new TextView[5];
        text[0] = (TextView) findViewById(R.id.lost_more_text1);
        text[1] = (TextView) findViewById(R.id.lost_more_text2);
        text[2] = (TextView) findViewById(R.id.lost_more_text3);
        text[3] = (TextView) findViewById(R.id.lost_more_text4);
        text[4] = (TextView) findViewById(R.id.lost_more_text5);
        for(int h=0;h<text.length;h++){
            text[h].setTextColor(getResources().getColor(R.color.secondary_text));
        }

        back = (ImageButton) findViewById(R.id.lost_more_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle bundle = intent.getExtras();
        if(bundle.getBoolean("isNotification",false)){
            SerializableMap serializableMap = (SerializableMap) bundle.getSerializable("message");
            dealInfo(serializableMap.getMap());
        }
        else {
            doPostGet = new DoPostGet(this);
            doPostGet.setInterface(this);
            doPostGet.doGet(url, "utf-8");
        }
    }

    @Override
    public void onDo(String str) {
        Element main = Jsoup.parse(str).getElementsByClass("content-box").first();
        String titleText = main.getElementsByClass("title").first().getElementsByTag("h3").first().text();
        title.setText(titleText);
        String con = main.getElementsByClass("rctxt").first().text();
        textContent.setText(con);
        Elements li = main.getElementsByClass("rcborw").first().getElementsByTag("li");
        for(int r=0;r<li.size();r++){
            if(r>=5){
                return;
            }
            String word = li.get(r).text();
            text[r].setText(word);
        }
    }

    private void dealInfo(Map<String,String> map){
        String Title = map.get("Title");
        String Address = map.get("Address");
        String Linkman = map.get("Linkman");
        String Mobile = map.get("Mobile");
        String QQ = map.get("QQ");
        String Email = map.get("Email");
        String Description = map.get("Description");

        title.setText(Title);
        textContent.setText(Description);

        text[0].setText("地址:"+Address);
        text[1].setText("联系人:"+Linkman);
        text[2].setText("电话:"+Mobile);
        text[3].setText("QQ号:"+QQ);
        text[4].setText("Email:"+Email);
    }
}
