package com.codeevery.InfoShow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.codeevery.zzudingding.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeevery.NetGetPost.DoPostGet;
import com.codeevery.myElement.LoadMoreListView;

/**
 * Created by songchao on 15/8/9.
 */
public class FirstInfo extends Activity implements com.codeevery.NetGetPost.DoPostGet.DoSomeThing, com.codeevery.myElement.LoadMoreListView.IReflashListener {
    ImageButton backButton;
    LoadMoreListView loadMoreListView;
    TextView title;
    DoPostGet doPostGet;
    boolean isFirst = true;
    int pageNum = 0;
    LayoutInflater inflater;
    String url,charaset;

    ArrayList<String> timeList, titleList, titSiteList;
    BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_info);

        timeList = new ArrayList<>();
        titleList = new ArrayList<>();
        titSiteList = new ArrayList<>();

        inflater = this.getLayoutInflater();
        title = (TextView) findViewById(R.id.title);
        backButton = (ImageButton) findViewById(R.id.back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        loadMoreListView = (LoadMoreListView) findViewById(R.id.show_info_listview);
        loadMoreListView.setInterface(this);
        doPostGet = new DoPostGet(this);
        doPostGet.setInterface(this);

        Intent intent = this.getIntent();
        this.doWhich = intent.getIntExtra("doWhich", 1);
        this.url = intent.getStringExtra("url");
        System.out.println("url:"+url);
        this.charaset = intent.getStringExtra("charaset");
        doPostGet.doGet(url, charaset);
    }

    //得到的信息每页30个共，26页
    int everyPageNum;//总页数
    int allNum;
    String nextSite;//下一页网址
    int doWhich = 1;

    @Override
    public void onDo(String str) {
        pageNum++;
        switch (doWhich){
            case 1:
                title.setText("教务公告");
                onDo1(str);
                break;
            case 2:
                title.setText("通知公告");
                onDo2(str);
                break;
            case 3:
                title.setText("学术动态");
                //onDo2和第三个要解析的网页结构一样
                onDo2(str);
                break;
            case 4:
                title.setText("教务要闻");
                onDo1(str);
                break;
            case 5:
                title.setText("招聘信息");
                onDo5(str);
                break;
            case 6:
                //onDo6(str);
                break;
            default:
                break;
        }
        onDoAll();
        loadMoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("click:" + position);
                Intent intent = new Intent();
                intent.setClass(FirstInfo.this, ShowInfoMore.class);
                intent.putExtra("moreInfoUrl", titSiteList.get(position));
                intent.putExtra("charaset", charaset);
                intent.putExtra("doWhich", doWhich);
                startActivity(intent);
            }
        });
        loadMoreListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return false;
            }
        });
    }

    private void onDo1(String siteStr) {
        try {
            Document document = Jsoup.parse(siteStr);
            Element table2 = document.getElementById("02").select("table[id!=02]").get(1).getElementsByTag("td").first();
            if(table2.getElementsContainingOwnText("下一页").first().hasAttr("href")) {
                nextSite = table2.getElementsContainingOwnText("下一页").first().attr("href");
                System.out.println("next site；" + nextSite);
            }
            Pattern pattern = Pattern.compile("共(.*?)条,每页(.*?)条");
            Matcher matcher = pattern.matcher(table2.text());
            if (matcher.find()) {
                allNum = Integer.parseInt(matcher.group(1));
                everyPageNum = Integer.parseInt(matcher.group(2));
            }
            Element table = document.getElementById("02").select("table[id!=02]").first();
            Elements tr = table.getElementsByTag("tr");
            for (int i = 0; i < tr.size(); i++) {
                Element td = tr.get(i).getElementsByTag("td").first();
                String time = td.text();
                pattern = Pattern.compile("[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}");
                matcher = pattern.matcher(time);
                if (matcher.find()) {
                    timeList.add(matcher.group(0));
                } else {
                    timeList.add("0000-00-00");
                }
                String tit = td.select("a[href]").text();
                titleList.add(tit);
                String site = td.select("a[href]").attr("href");
                titSiteList.add(site);
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        loadMoreListView.setPage(allNum%everyPageNum!=0 ? allNum / everyPageNum + 1 : allNum / everyPageNum, pageNum,everyPageNum);
    }

    private void onDo2(String str){
        try {
            Document document = Jsoup.parse(str);
            Element pageHtml = document.select("div[class=zzj_4]").first();
            nextSite = pageHtml.select("a[href]").first().attr("href");
            Pattern pattern = Pattern.compile("共(.*?)条.每页(.*?)条");
            Matcher matcher = pattern.matcher(pageHtml.text());
            if(matcher.find()){
                allNum = Integer.parseInt(matcher.group(1));
                everyPageNum = Integer.parseInt(matcher.group(2));
            }
            else {
                //这里时读取错误的代码
            }
            Elements mainHtml = document.select("div[class=zzj_5]").first().select("div[class=zzj_5a]");
            for (int i = 0; i < mainHtml.size(); i++) {
                Element inHtml = mainHtml.get(i);
                String time = inHtml.text();
                pattern = Pattern.compile("[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}");
                matcher = pattern.matcher(time);
                if (matcher.find()) {
                    timeList.add(matcher.group(0));
                } else {
                    timeList.add("0000-00-00");
                }
                String tit = inHtml.select("span[class~=zzj_f6_*]").text();
                titleList.add(tit);
                String site = inHtml.select("a[href]").attr("href");
                titSiteList.add(site);
            }
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        loadMoreListView.setPage(allNum%everyPageNum!=0 ? allNum / everyPageNum + 1 : allNum / everyPageNum, pageNum,everyPageNum);
    }

    private void onDo5(String str){
        Element body = Jsoup.parse(str).body();
        body.setBaseUri("http://job.zzu.edu.cn");
        Element submain = body.getElementsByClass("submain").first();
        Elements li = submain.getElementsByClass("jobfairlist").first().getElementsByTag("li");
        for(int i=0;i<li.size();i++){
            //循环获得招聘主题
            String title = li.get(i).select("a[href]").text();
            titleList.add(title);
            String titSite = li.get(i).select("a[href]").attr("abs:href");
            titSiteList.add(titSite);
            String time = li.get(i).getElementsByTag("span").text();
            timeList.add(time);
        }
        nextSite = body.getElementsByClass("page").get(1).select("span").first().nextElementSibling().attr("abs:href");
        everyPageNum = 15;
        //包含下一页数目的字符串
        String getNum = body.getElementsByClass("page").get(1).text();
        Pattern pattern = Pattern.compile("共([0-9]*)页");
        Matcher matcher = pattern.matcher(getNum);
        int allPageNum = 1;
        if(matcher.find()){
            allPageNum= Integer.parseInt(matcher.group(1));
        }
        loadMoreListView.setPage(allPageNum, pageNum,everyPageNum);
    }

    public void onDoAll() {
        if (isFirst) {
            adapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    if (pageNum == (allNum%everyPageNum!=0 ? allNum / everyPageNum + 1 : allNum / everyPageNum)) {
                        return ((pageNum-1) * everyPageNum)+allNum % everyPageNum;
                    }
                    return pageNum * everyPageNum;
                }

                @Override
                public Object getItem(int position) {
                    return null;
                }

                @Override
                public long getItemId(int position) {
                    return 0;
                }

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    convertView = inflater.inflate(R.layout.one_new_info, null);
                    TextView titleText = (TextView) convertView.findViewById(R.id.show_info_one_title);
                    TextView timeText = (TextView) convertView.findViewById(R.id.show_info_one_time);
                    titleText.setText(titleList.get(position));
                    timeText.setText(timeList.get(position));
                    return convertView;
                }
            };
            loadMoreListView.setAdapter(adapter);
            doPostGet.setDialogNull();
            isFirst = false;
        } else {
            adapter.notifyDataSetChanged();
            loadMoreListView.refreshComplete();
        }
    }

    @Override
    public void onReflash() {
        doPostGet.doGet(nextSite, charaset);
    }
}
