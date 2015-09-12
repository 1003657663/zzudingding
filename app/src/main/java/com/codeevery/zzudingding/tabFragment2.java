package com.codeevery.zzudingding;

import com.codeevery.InfoShow.FirstInfo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.TextViewCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codeevery.InfoShow.ShowInfoMore;
import com.codeevery.NetGetPost.DoPostGet;
import com.codeevery.application.AllObject;
import com.codeevery.myElement.AlignTextView;
import com.codeevery.myElement.DatabaseHelper;
import com.codeevery.myElement.TextImgButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by songchao on 15/7/27.
 * 对信息从数据库进行读取并展示
 */
public class tabFragment2 extends Fragment implements DoPostGet.DoSomeThing{
    private Context context;
    private AllObject setting;
    private LinearLayout newsLayout;
    public static NewsHandler newsHandler;
    private DoPostGet doPostGet;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_second, container, false);
        this.context = inflater.getContext();
        setting = (AllObject) context.getApplicationContext();
        init(root);//初始化组件
        doPostGet = new DoPostGet(context);
        doPostGet.setInterface(this);
        if(setting.isFirst){
            getNewsFirst();
        }else {
            newsHandler = new NewsHandler(context, newsLayout);
            newsHandler.sendEmptyMessage(6);
        }
        return root;
    }


    static class NewsHandler extends Handler {
        Context context;
        AllObject setting;
        LinearLayout newsLayout;
        public NewsHandler(Context context,LinearLayout newsLayout){
            this.context = context;
            this.newsLayout = newsLayout;
            setting = (AllObject) context.getApplicationContext();
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what!=6){
                return;
            }
            List<String []> list;
            if((list = checkDB())!=null){
                updateUI(list);
            }
        }

        private List<String []> checkDB(){
            DatabaseHelper databaseHelper = new DatabaseHelper(context, "dingding", null, 1);
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String selSql = "select max(id) as maxId from news";
            Cursor cursor = db.rawQuery(selSql, null);
            int maxId = 0;
            if (cursor.moveToFirst()) {
                maxId = cursor.getInt(cursor.getColumnIndex("maxId"));
            }
            List<String[]> list = new ArrayList<>();
            int h = 0;
            if (maxId - 4 <= 0 && maxId > 0) {
                h = 1;
            } else if (maxId <= 0) {
                return null;
            } else if (maxId - 4 > 0) {
                h = maxId - 4;
            }
            String selSql1 = "select type,title,time,content,site from news where id=" + h + " or id=" + (h + 1 > maxId ? maxId : h + 1) + " or id=" + (h + 2 > maxId ? maxId : h + 2) + " or id=" + (h + 3 > maxId ? maxId : h + 3) + " or id=" + (h + 4 > maxId ? maxId : h + 4) + " order by id asc";
            Cursor cursor1 = db.rawQuery(selSql1, null);
            while (cursor1.moveToNext()) {
                String a[] = new String[5];
                a[0] = cursor1.getString(cursor1.getColumnIndex("type"));
                a[1] = cursor1.getString(cursor1.getColumnIndex("title"));
                a[2] = cursor1.getString(cursor1.getColumnIndex("time"));
                a[3] = cursor1.getString(cursor1.getColumnIndex("content"));
                a[4] = cursor1.getString(cursor1.getColumnIndex("site"));
                list.add(a);
            }
            return list;
        }

        private void updateUI(List<String []> list) {

            if (list == null) {
                return;
            }
            //在这里更新UI线程 使用list
            newsLayout.removeAllViews();
            for (int i = 0; i < list.size(); i++) {
                final String a[] = list.get(i);

                LinearLayout all = new LinearLayout(context);
                all.setClickable(true);
                all.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
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
                        intent.putExtra("doWhich", doWhich);
                        intent.putExtra("charaset", charaset);
                        intent.putExtra("moreInfoUrl", a[4]);
                        intent.setClass(context, ShowInfoMore.class);
                        context.startActivity(intent);
                    }
                });
                all.setPadding(25, 10, 25, 0);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 0, 20);
                all.setLayoutParams(params);
                all.setOrientation(LinearLayout.VERTICAL);
                all.setBackgroundResource(R.drawable.imgtextbutton);

                //设置顶部的LinearLayout
                LinearLayout top = new LinearLayout(context);
                top.setPadding(0, 20, 0, 30);
                top.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                //设置一个TextView,显示目录文字
                TextView flagTextView = new TextView(context);
                flagTextView.setPadding(0, 0, 10, 0);
                LinearLayout.LayoutParams flagParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                flagTextView.setLayoutParams(flagParams);
                flagTextView.setTextColor(context.getResources().getColor(R.color.primary));
                flagTextView.setText("[" + a[0] + "]");

                //这个textView用来显示时间
                TextView timeTextView = new TextView(context);
                timeTextView.setText(a[2]);
                LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                timeTextView.setLayoutParams(params2);
                timeTextView.setTextColor(context.getResources().getColor(R.color.darkgray));

                //设置一个textView显示题目
                TextView titleTextView = new TextView(context);
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
                titleTextView.setLayoutParams(params1);
                titleTextView.setSingleLine();
                titleTextView.setEllipsize(TextUtils.TruncateAt.END);
                titleTextView.setText(a[1]);

                top.setVerticalGravity(RelativeLayout.CENTER_VERTICAL);
                top.addView(flagTextView);
                top.addView(titleTextView);
                top.addView(timeTextView);

                //设置内容TextView
                AlignTextView alignTextView = new AlignTextView(context);
                alignTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                alignTextView.setPadding(0, 10, 0, 10);
                alignTextView.setLineSpacing(0, 1.2f);
                alignTextView.setTextColor(context.getResources().getColor(R.color.secondary_text));
                alignTextView.setText(a[3]);

                all.addView(top);
                all.addView(alignTextView);
                newsLayout.addView(all, 0);
            }
        }
    }

    private void init(View root) {
        Button searchButton = (Button) root.findViewById(R.id.search_button);
        newsLayout = (LinearLayout) root.findViewById(R.id.fragment2_news);
        TextImgButton textImgButton1 = (TextImgButton) root.findViewById(R.id.second_button1);
        textImgButton1.setTextViewText("教务公告");
        TextImgButton textImgButton2 = (TextImgButton) root.findViewById(R.id.second_button2);
        textImgButton2.setTextViewText("通知公告");
        TextImgButton textImgButton3 = (TextImgButton) root.findViewById(R.id.second_button3);
        textImgButton3.setTextViewText("学术动态");
        TextImgButton textImgButton4 = (TextImgButton) root.findViewById(R.id.second_button4);
        textImgButton4.setTextViewText("教务要闻");
        TextImgButton textImgButton5 = (TextImgButton) root.findViewById(R.id.second_button5);
        textImgButton5.setTextViewText("招聘信息");
        TextImgButton textImgButton6 = (TextImgButton) root.findViewById(R.id.second_button6);
        //设置监听
        textImgButton1.setOnClickListener(new SixButtonListener());
        textImgButton2.setOnClickListener(new SixButtonListener());
        textImgButton3.setOnClickListener(new SixButtonListener());
        textImgButton4.setOnClickListener(new SixButtonListener());
        textImgButton5.setOnClickListener(new SixButtonListener());
        textImgButton6.setOnClickListener(new SixButtonListener());
        searchButton.setOnClickListener(new SixButtonListener());
    }


    class SixButtonListener implements View.OnClickListener {
        Intent intent;

        public SixButtonListener() {
            this.intent = new Intent();
            intent.setClass(context, FirstInfo.class);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.second_button1:
                    intent.putExtra("doWhich", 1);
                    intent.putExtra("url", "http://jw.zzu.edu.cn/scripts/news.dll/morenews?type=1&pn=1");
                    intent.putExtra("charaset", "gb2312");
                    startActivity(intent);
                    break;
                case R.id.second_button2:
                    intent.putExtra("doWhich", 2);
                    intent.putExtra("url", "http://www16.zzu.edu.cn/msgs/vmsgisapi.dll/vmsglist?mtype=m&lan=101,102,103");
                    intent.putExtra("charaset", "utf-8");
                    startActivity(intent);
                    break;
                case R.id.second_button3:
                    intent.putExtra("doWhich", 3);
                    intent.putExtra("url", "http://www16.zzu.edu.cn/msgs/vmsgisapi.dll/vmsglist?mtype=m&lan=105");
                    intent.putExtra("charaset", "utf-8");
                    startActivity(intent);
                    break;
                case R.id.second_button4:
                    intent.putExtra("doWhich", 4);
                    intent.putExtra("url", "http://jw.zzu.edu.cn/scripts/news.dll/morenews?type=2&pn=1");
                    intent.putExtra("charaset", "gb2312");
                    startActivity(intent);
                    break;
                case R.id.second_button5:
                    intent.putExtra("doWhich", 5);
                    intent.putExtra("url", "http://job.zzu.edu.cn/MoreJobFairs.aspx");
                    intent.putExtra("charaset", "utf-8");
                    startActivity(intent);
                    break;
                case R.id.second_button6:
                    break;
                case R.id.search_button:
                    Intent intent = new Intent();
                    intent.setClass(context, SearchResultActivity.class);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private void getNewsFirst(){
        String url = "http://www16.zzu.edu.cn/msgs/vmsgisapi.dll/vmsglist?mtype=m&lan=101,102,103";
        String charaset = "utf-8";
        doPostGet.doGet(url, charaset);
    }

    List<String> timeList,titleList,titSiteList;
    @Override
    public void onDo(String str) {
        timeList = new ArrayList<>();
        titleList = new ArrayList<>();
        titSiteList = new ArrayList<>();
        try {
            Document document = Jsoup.parse(str);
            Elements mainHtml = document.select("div[class=zzj_5]").first().select("div[class=zzj_5a]");
            Pattern pattern;
            Matcher matcher;
            for (int i = 0; i < mainHtml.size(); i++) {
                Element inHtml = mainHtml.get(i);
                String time = inHtml.text();
                pattern = Pattern.compile("[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}");
                matcher = pattern.matcher(time);
                if (matcher.find()) {
                    timeList.add(matcher.group(0));
                } else {
                    timeList.add("0000-00-00");//找到文章时间
                }
                String tit = inHtml.select("span[class~=zzj_f6_*]").text();
                titleList.add(tit);
                String site = inHtml.select("a[href]").attr("href");
                titSiteList.add(site);
            }
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        //接下来记录到数据库,记录5条信息
        DatabaseHelper databaseHelper = new DatabaseHelper(context, "dingding", null, 1);
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        //得到数据库的长度
        String selSql = "select max(id) as maxId from news";
        Cursor cursor = db.rawQuery(selSql, null);
        int maxId = 0;
        if (cursor.moveToFirst()) {
            maxId = cursor.getInt(cursor.getColumnIndex("maxId"));
        }
        if(maxId<5) {
            maxId = 5-maxId;
            int ii = titleList.size() >= maxId ? maxId : titleList.size();
            for (int i = ii - 1; i >= 0; i--) {
                db.execSQL("insert into news values (null,'" + "通知公告" + "','" + titleList.get(i) + "','" + timeList.get(i)
                        + "','" + "" + "','" + titSiteList.get(i) + "')");
            }
        }
        newsHandler = new NewsHandler(context, newsLayout);
        newsHandler.sendEmptyMessage(6);
    }

}
