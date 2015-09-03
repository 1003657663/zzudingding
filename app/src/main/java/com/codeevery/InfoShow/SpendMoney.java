package com.codeevery.InfoShow;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.codeevery.zzudingding.R;
import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codeevery.application.AllObject;
import com.codeevery.myElement.GetPostThread;
import com.codeevery.myElement.myDialog;

/**
 * Created by songchao on 15/8/14.
 */
public class SpendMoney extends Activity{
    AllObject setting;
    myDialog dialog;
    Map<String,String> cookieMap;
    SpendMoneyBean spendMoneyBean;
    TextView text1,text2,text3,text4,text5;
    int screenWidth,screenHeight;
    TableLayout table;
    DoDo doDo;
    Button nextPage,prePage;
    ImageButton back;
    int allNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spend_money);
        setting = (AllObject) getApplication();
        cookieMap = setting.cardCookieMap;
        cookieMap.remove("JSESSIONID&1234");
        dialog = new myDialog(this);
        text1 = (TextView) findViewById(R.id.spend_money_spendTime);
        text2 = (TextView) findViewById(R.id.spend_money_detail);
        text3 = (TextView) findViewById(R.id.spend_money_spendName);
        text4 = (TextView) findViewById(R.id.spend_money_spendNum);
        text5 = (TextView) findViewById(R.id.spend_money_leastMoney);
        table = (TableLayout) findViewById(R.id.spend_money_table);
        nextPage = (Button) findViewById(R.id.spend_money_nextPage);
        prePage = (Button) findViewById(R.id.spend_money_prePage);
        back = (ImageButton) findViewById(R.id.spend_money_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

        text1.setWidth(screenWidth/4);
        text2.setWidth(screenWidth/4);
        text3.setWidth(screenWidth/4);
        text4.setWidth(screenWidth/8);
        text5.setWidth(screenWidth/8);

        //开始第一次请求
        final int[] start = {0};
        doDo = new DoDo();

        GetIframe getIframe = new GetIframe(dialog, cookieMap, doDo, start[0]);
        String getIframeUrl = "http://ecard.zzu.edu.cn/web/guest/stu?p_p_id=DataObject_INSTANCE_68Xe&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-2&p_p_col_count=3&_DataObject_INSTANCE_68Xe_forward_=query&_DataObject_INSTANCE_68Xe_themeId_=351&_DataObject_INSTANCE_68Xe_themeName_=%E6%B6%88%E8%B4%B9%E4%BF%A1%E6%81%AF";
        new GetPostThread(getIframe,getIframeUrl,cookieMap,true).start();

        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start[0]<allNum-30) {
                    table.removeAllViews();
                    start[0] = start[0] + 30;
                    GetIframe getIframe = new GetIframe(dialog, cookieMap, doDo, start[0]);
                    String getIframeUrl = "http://ecard.zzu.edu.cn/web/guest/stu?p_p_id=DataObject_INSTANCE_68Xe&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-2&p_p_col_count=3&_DataObject_INSTANCE_68Xe_forward_=query&_DataObject_INSTANCE_68Xe_themeId_=351&_DataObject_INSTANCE_68Xe_themeName_=%E6%B6%88%E8%B4%B9%E4%BF%A1%E6%81%AF";
                    new GetPostThread(getIframe, getIframeUrl, cookieMap, true).start();
                }
            }
        });
        prePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start[0]>=30) {
                    table.removeAllViews();
                    start[0] = start[0] - 30;
                    GetIframe getIframe = new GetIframe(dialog, cookieMap, doDo, start[0]);
                    String getIframeUrl = "http://ecard.zzu.edu.cn/web/guest/stu?p_p_id=DataObject_INSTANCE_68Xe&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-2&p_p_col_count=3&_DataObject_INSTANCE_68Xe_forward_=query&_DataObject_INSTANCE_68Xe_themeId_=351&_DataObject_INSTANCE_68Xe_themeName_=%E6%B6%88%E8%B4%B9%E4%BF%A1%E6%81%AF";
                    new GetPostThread(getIframe, getIframeUrl, cookieMap, true).start();
                }
            }
        });
    }

    class DoDo implements GetDataHandler.Do {
        public void onDo(String str) {
            if(dialog!=null){
                dialog.hideProgressDialog();
            }
            changeJson(str);
            if(str.equals("") || spendMoneyBean == null){
                System.out.println("json: empty or null");
                if(dialog!=null)
                    dialog.showDialogWithSure("没有您的消费记录哦,这段时间您没有消费过吧？","确定");
                return;
            }else if(spendMoneyBean.totalProperty==0){
                System.out.println("total=0");
                if(dialog!=null)
                    dialog.showDialogWithSure("没有您的消费记录哦,这段时间您没有消费过吧？","确定");
                return;
            }
            allNum = spendMoneyBean.totalProperty;
            //转换完json对象后展示出来
            boolean changeColor = true;
            for (int i = 0; i < spendMoneyBean.root.size(); i++) {
                TableRow tableRow = new TableRow(SpendMoney.this);
                tableRow.setPadding(0, 20, 0, 20);
                if(changeColor){
                    tableRow.setBackgroundColor(getResources().getColor(R.color.background));
                }
                else{
                    tableRow.setBackgroundColor(getResources().getColor(R.color.lightgray));
                }

                TextView textView1 = new TextView(SpendMoney.this);
                textView1.setWidth(screenWidth / 4);
                textView1.setTextColor(getResources().getColor(R.color.primary_text));
                textView1.setText(spendMoneyBean.root.get(i).OPDT);

                TextView textView2 = new TextView(SpendMoney.this);
                textView2.setWidth(screenWidth / 4);
                textView2.setTextColor(getResources().getColor(R.color.primary_text));
                textView2.setText(spendMoneyBean.root.get(i).DSCRP);

                TextView textView3 = new TextView(SpendMoney.this);
                textView3.setWidth(screenWidth / 4);
                textView3.setTextColor(getResources().getColor(R.color.primary_text));
                textView3.setText(spendMoneyBean.root.get(i).TERMNAME);

                TextView textView4 = new TextView(SpendMoney.this);
                textView4.setWidth(screenWidth / 8);
                textView4.setTextColor(getResources().getColor(R.color.primary_text));
                textView4.setText(spendMoneyBean.root.get(i).OPFARE + "");

                TextView textView5 = new TextView(SpendMoney.this);
                textView5.setWidth(screenWidth / 8);
                textView5.setTextColor(getResources().getColor(R.color.primary_text));
                textView5.setText(spendMoneyBean.root.get(i).ODDFARE);

                tableRow.addView(textView1);
                tableRow.addView(textView2);
                tableRow.addView(textView3);
                tableRow.addView(textView4);
                tableRow.addView(textView5);

                table.addView(tableRow);
                changeColor = !changeColor;
            }
        }
    }

    private void changeJson(String str){
        System.out.println("json:"+str);
        Gson gson = new Gson();
        spendMoneyBean = gson.fromJson(str,SpendMoneyBean.class);
    }

    //第一次请求获取iframe网址
    static class GetIframe extends Handler{
        myDialog dialog;
        String iframeUrl;
        Map<String,String> cookieMap;
        DoDo doDo;
        int start;
        GetIframe(myDialog dialog,Map<String,String> cookieMap,DoDo doDo,int start){
            this.dialog = dialog;
            this.cookieMap = cookieMap;
            this.doDo = doDo;
            this.start = start;
            if(dialog!=null){
                dialog.showProgressDialog("正在加载,稍等一会..");
            }
        }
        @Override
        public void handleMessage(Message msg) {
            //请求成功之后解析出iframe网址
            if(msg.what == 1) {
                String result = msg.getData().getString("result");
                Element element = Jsoup.parse(result).body();
                iframeUrl = "http://ecard.zzu.edu.cn"+element.getElementsByTag("iframe").get(0).attr("src");

                //进行下一个请求
                //请求第二个ID
                GetSecondId getSecondId = new GetSecondId(dialog,cookieMap,doDo,start);
                GetPostThread getPostThread = new GetPostThread(getSecondId,iframeUrl,cookieMap,true);
                getPostThread.setOnlyCookie(true);
                getPostThread.start();
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

    //得到第二个IDcookie的handler
    static class GetSecondId extends Handler{
        myDialog dialog;
        Map<String,String> cookieMap;
        DoDo doDo;
        int start;
        GetSecondId(myDialog dialog,Map<String,String> cookieMap,DoDo doDo,int start){
            this.dialog = dialog;
            this.cookieMap = cookieMap;
            this.doDo = doDo;
            this.start = start;
        }
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 5) {
                //这里对得到的cookie进行解析
                CookieStore cookieStore = (CookieStore) msg.obj;
                if(cookieStore.getCookies().toString().contains("JSESSIONID")){
                    List<HttpCookie> list = cookieStore.getCookies();
                    for(int i=0;i<list.size();i++) {
                        if(list.get(i).getName().equals("JSESSIONID")){
                            cookieMap.put("JSESSIONID&1234",list.get(i).getValue());
                        }
                    }
                    //下一步进行最后一个请求
                    String nextUrl = "http://ecard.zzu.edu.cn/query/queryData.do";
                    Map<String,String> map = new HashMap<>();
                    map.put("start",start+"");
                    map.put("themeId","351");
                    map.put("whereSql","%5B%5D");
                    map.put("orderGroup","");
                    map.put("limit","30");
                    map.put("pageSize","30");
                    map.put("varValue","");

                    GetDataHandler getDataHandler = new GetDataHandler(dialog,cookieMap);
                    getDataHandler.setOnDo(doDo);
                    GetPostThread getPostThread = new GetPostThread(getDataHandler,nextUrl,cookieMap,false,map);
                    getPostThread.setNoCookieManager(false);
                    getPostThread.start();
                }
                else {
                    dialog.showDialogWithSure("获取唯一码错误,请稍后重试","确定");
                }
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

    /*static class GetDataHandler extends Handler{
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
    }*/
}
