package com.codeevery.CheckGrade;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.codeevery.myElement.myDialog;
import com.codeevery.zzudingding.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.codeevery.NetGetPost.VolleyErrorHelper;
import com.codeevery.application.AllObject;

/**
 * Created by songchao on 15/8/6.
 */
public class EmptyRoom extends Activity {
    private Spinner weekSpin;
    private Spinner classSpin;
    private Button start;
    private ImageButton backButton;
    private String emptyRoomcheckUrl;
    private AllObject setting;
    private RequestQueue requestQueue;//新建请求队列
    private int weekNum;
    private int classNum;
    private myDialog dialog;
    private TableLayout table;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.emptyroom);
        this.setting = (AllObject) getApplication();
        this.emptyRoomcheckUrl = setting.emptyRoomUrl;
        dialog = new myDialog(this);
        requestQueue = Volley.newRequestQueue(this);
        //初始化控件
        weekSpin = (Spinner) findViewById(R.id.spinner_week);
        classSpin = (Spinner) findViewById(R.id.spinner_class);
        backButton = (ImageButton) findViewById(R.id.empty_room_back_button);
        table = (TableLayout) findViewById(R.id.empty_room_table);
        table.setBackgroundColor(getResources().getColor(R.color.dark_background));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_week, android.R.layout.simple_spinner_dropdown_item);
        weekSpin.setAdapter(adapter);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,R.array.sqinner_class,android.R.layout.simple_spinner_dropdown_item);
        classSpin.setAdapter(adapter1);
        start = (Button) findViewById(R.id.empty_room_start);
        //设置监听事件
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.showProgressDialog("正在加载...");
                weekNum = weekSpin.getSelectedItemPosition()+1;
                classNum = classSpin.getSelectedItemPosition()+1;
                Map<String,String> map = new HashMap<>();
                map.put("zhanghao",setting.xuehao);
                map.put("mima",setting.mima);
                doPost(emptyRoomcheckUrl, map);
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    private void doPost(String url,final Map<String,String> map){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(s.contains("放假期间")){
                    dialog.showDialogWithSure("现在是放假期间,不能查询","确定");
                    return;
                }
                if (!s.contains("当前显示为本周")) {
                    Document document = Jsoup.parse(s);
                    String postAction = document.select("form[method]").first().attr("action");
                    Map<String, String> map1 = new HashMap<>();
                    map1.put("xqsort", weekNum + "");//向Map中添加，星期几
                    map1.put("jcsort", classNum + "");//添加第几节课
                    doPost(postAction, map1);//重新doPost
                } else {
                    dialog.hideProgressDialog();
                    changeUI(s);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                dialog.hideProgressDialog();
                Toast.makeText(EmptyRoom.this, VolleyErrorHelper.getMessage(volleyError,EmptyRoom.this),Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String temp;
                try {
                    temp = new String(response.data,"gb2312");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    temp = new String(response.data);
                }
                return Response.success(temp, HttpHeaderParser.parseCacheHeaders(response));
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void changeUI(String str){
        table.removeAllViews();
        int screenWidth = setting.screenWidth;
        Element body = Jsoup.parse(str).body();
        Element tableE = body.select("form[method]").first().getElementsByTag("table").get(1);
        Elements tr = tableE.getElementsByTag("tr");
        int num = tr.size();
        for(int i=0;i<num;i++){
            if(i==0){
                continue;
            }
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(0, 20, 0, 20);
            TableRow.LayoutParams params = new TableRow.LayoutParams();
            params.setMargins(0,0,0,3);
            tableRow.setLayoutParams(params);
            tableRow.setBackgroundColor(getResources().getColor(R.color.background));

            TextView textView1 = new TextView(this);
            TextView textView2 = new TextView(this);
            TextView textView3 = new TextView(this);

            textView1.setWidth(screenWidth/3);
            textView2.setWidth(screenWidth/3);
            textView3.setWidth(screenWidth/3);
            textView1.setGravity(Gravity.CENTER);
            textView2.setGravity(Gravity.CENTER);
            textView3.setGravity(Gravity.CENTER);

            textView1.setTextColor(getResources().getColor(R.color.primary_text));
            textView2.setTextColor(getResources().getColor(R.color.primary_text));
            textView3.setTextColor(getResources().getColor(R.color.primary_text));

            if(i==1){
                textView1.setText("教室");
                textView2.setText("座位数");
                textView3.setText("教室类型");
            }else {
                //解析字符串
                Elements td = tr.get(i).getElementsByTag("td");
                textView1.setText(td.get(1).text());
                textView2.setText(td.get(2).text());
                textView3.setText(td.get(3).text());
            }

            tableRow.addView(textView1);
            tableRow.addView(textView2);
            tableRow.addView(textView3);

            table.addView(tableRow);
        }
    }
}
