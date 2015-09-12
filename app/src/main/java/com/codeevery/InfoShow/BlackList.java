package com.codeevery.InfoShow;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.codeevery.NetGetPost.DoPostGet;
import com.codeevery.NetGetPost.VolleyErrorHelper;
import com.codeevery.myElement.myDialog;
import com.codeevery.zzudingding.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by songchao on 15/8/25.
 */
public class BlackList extends Activity implements DoPostGet.DoSomeThing,myDialog.SureButton{
    RequestQueue requestQueue;
    myDialog dialog;
    DoPostGet doPostGet;
    TableLayout tableLayout;
    ImageButton back;
    TextView title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.black_list);
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(BlackList.this);
        tableLayout = (TableLayout) findViewById(R.id.black_list_table);
        back = (ImageButton) findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
        title.setText("IP黑名单");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dialog = new myDialog(BlackList.this);
        dialog.setSureButton(this);
        doPostGet = new DoPostGet(BlackList.this);
        doPostGet.setInterface(this);
        String url = "http://202.196.64.224";
        doPostGet.doGet(url,"gb2312");
    }

    private void updateUI(Elements trs){
        if(trs.size()==1){
            dialog.showDialogWithSure("没有被禁的IP哦","确定");
            return;
        }
        for(int i=0;i<trs.size();i++){
            if(i==0){
                continue;
            }
            Element tr = trs.get(i);
            Elements td = tr.getElementsByTag("td");
            TableRow tableRow = new TableRow(BlackList.this);
            TextView ipText = new TextView(BlackList.this);
            TextView ipTextContent = new TextView(BlackList.this);
            ipText.setText("IP地址");
            ipTextContent.setText(td.get(0).text());
            tableRow.addView(ipText);
            tableRow.addView(ipTextContent);

            TableRow tableRow1 = new TableRow(BlackList.this);
            TextView inText = new TextView(BlackList.this);
            TextView inTextContent = new TextView(BlackList.this);
            inText.setText("进入时间");
            inText.setPadding(0,0,30,0);
            inTextContent.setText(td.get(1).text());
            tableRow1.addView(inText);
            tableRow1.addView(inTextContent);

            TableRow tableRow2 = new TableRow(BlackList.this);
            TextView outText = new TextView(BlackList.this);
            TextView outTextContent = new TextView(BlackList.this);
            outText.setText("预计解禁");
            outTextContent.setText(td.get(2).text());
            tableRow2.addView(outText);
            tableRow2.addView(outTextContent);
            tableRow2.setPadding(0,0,0,40);

            tableLayout.addView(tableRow);
            tableLayout.addView(tableRow1);
            tableLayout.addView(tableRow2);
        }
    }

    @Override
    public void onDo(String str) {
        //解析字符串
        Element body = Jsoup.parse(str).body();
        Element table = body.getElementsByTag("table").get(1);
        Elements tr = table.getElementsByTag("tr");
        updateUI(tr);
    }

    @Override
    public void sureButtonDo() {
        finish();
    }
}
