package com.codeevery.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codeevery.zzudingding.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeevery.NetGetPost.DoPostGet;
import com.codeevery.myElement.LoadMoreListView;

/**
 * Created by songchao on 15/8/11.
 */
public class GetLostActivity extends Activity implements DoPostGet.DoSomeThing,LoadMoreListView.IReflashListener{
    ImageButton back;
    Button get,lost;
    Button iWillDo;
    String url;

    DoPostGet doPostGet;
    LoadMoreListView loadMoreListView;
    BaseAdapter baseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_lost_thing);
        back = (ImageButton) findViewById(R.id.get_lost_back);
        get = (Button) findViewById(R.id.get_lost_get);
        lost = (Button) findViewById(R.id.get_lost_lost);
        iWillDo = (Button) findViewById(R.id.get_lost_iwill);

        back.setOnClickListener(new GetLostListener());
        get.setOnClickListener(new GetLostListener());
        lost.setOnClickListener(new GetLostListener());
        iWillDo.setOnClickListener(new GetLostListener());
        iWillDo.setText("捡到东西 我要招领");

        get.setTextColor(getResources().getColor(R.color.primary));
        get.setBackgroundResource(R.drawable.left_radius_border);

        titleList = new ArrayList<>();
        timeList = new ArrayList<>();
        siteList = new ArrayList<>();
        responseList = new ArrayList<>();

        loadMoreListView = (LoadMoreListView) findViewById(R.id.get_lost_content);
        loadMoreListView.setInterface(this);

        url = "http://szhq.zzu.edu.cn/home/GetLostList/2?PageIndex=";
        doPostGet = new DoPostGet(this);
        doPostGet.setInterface(this);
        doPostGet.doGet(url+pageNum,"utf-8");
    }

    ArrayList<String> titleList;
    ArrayList<String> timeList;
    ArrayList<String> siteList;
    ArrayList<String> responseList;
    int pageNum = 0;
    int everyPageNum = 15;
    int allNum;
    boolean isFirst = true;


    @Override
    public void onDo(String str) {
        pageNum++;
        Pattern pattern;
        Matcher matcher;
        try {
            Element body = Jsoup.parse(str).body();
            Element allList = body.getElementsByClass("list_addborder").first();
            body.setBaseUri("http://szhq.zzu.edu.cn");
            Elements listTop = allList.select("ul[class=list-item]");
            for (int i = 0; i < listTop.size(); i++) {
                Element a = listTop.get(i).select("li[class=cloumn-desc]").first().select("a[href]").first();
                String title = a.text();
                String site = a.attr("abs:href");
                titleList.add(title);
                siteList.add(site);
                String time = listTop.get(i).select("li[class=cloumn-utime]").text();
                timeList.add(time);
                String response = listTop.get(i).select("li[class=cloumn-stime]").text();
                pattern = Pattern.compile("([0-9]*)/[0-9]");
                matcher = pattern.matcher(response);
                if(matcher.find()){
                    response = matcher.group(1);
                }
                responseList.add("查看 "+response);
            }
            String allNumText = body.select("div[class=context-wrap]").html();

            pattern = Pattern.compile("PageData\\(\"([0-9]*)\".*?\"[0-9]*\".*\\)");
            matcher = pattern.matcher(allNumText);
            if(matcher.find()){
                allNum = Integer.parseInt(matcher.group(1));
            }
            settAdapter();
            loadMoreListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent();
                    intent.putExtra("lostMoreUrl",siteList.get(position));
                    intent.setClass(GetLostActivity.this, GetLostMore.class);
                    startActivity(intent);
                }
            });
        }catch (NullPointerException ex){
            Toast.makeText(GetLostActivity.this,"解析信息失败，稍后重试",Toast.LENGTH_SHORT).show();
            ex.printStackTrace();
        }
    }

    public void settAdapter(){
        loadMoreListView.setPage(allNum%everyPageNum==0?allNum/everyPageNum:allNum/everyPageNum+1,pageNum,allNum<everyPageNum?allNum:everyPageNum);
        if(isFirst) {
            baseAdapter = new BaseAdapter() {
                @Override
                public int getCount() {
                    return pageNum * everyPageNum > allNum ? allNum : pageNum * everyPageNum;
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
                    convertView = GetLostActivity.this.getLayoutInflater().inflate(R.layout.one_list_getlost, null);
                    TextView tit = (TextView) convertView.findViewById(R.id.one_list_getlost_title);
                    tit.setText(titleList.get(position));
                    TextView tim = (TextView) convertView.findViewById(R.id.one_list_getlost_time);
                    tim.setText(timeList.get(position));
                    TextView respon = (TextView) convertView.findViewById(R.id.one_list_getlost_response);
                    respon.setText(responseList.get(position));
                    return convertView;
                }
            };
            isFirst=false;
            loadMoreListView.setAdapter(baseAdapter);
        }else{
            baseAdapter.notifyDataSetChanged();
            loadMoreListView.refreshComplete();
        }
    }

    @Override
    public void onReflash() {
        doPostGet.setDialogNull();
        doPostGet.doGet(url+(pageNum+1),"utf-8");
    }

    //设定标志位判断是lost还是get
    private boolean islostGet = true;//true is get;false is lost
    class GetLostListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.get_lost_back:
                    finish();
                    break;
                case R.id.get_lost_get:
                    get.setTextColor(getResources().getColor(R.color.primary));
                    lost.setTextColor(Color.parseColor("#ffffff"));
                    get.setBackgroundResource(R.drawable.left_radius_border);
                    lost.setBackgroundResource(R.drawable.right_radius_border_white);
                    islostGet = true;
                    isFirst = true;
                    titleList.clear();
                    timeList.clear();
                    siteList.clear();
                    responseList.clear();
                    iWillDo.setText("捡到东西 我要招领");
                    url = "http://szhq.zzu.edu.cn/home/GetLostList/2?PageIndex=";
                    pageNum = 0;
                    doPostGet.getDialog();
                    doPostGet.doGet(url+pageNum,"utf-8");
                    break;
                case R.id.get_lost_lost:
                    lost.setTextColor(getResources().getColor(R.color.primary));
                    get.setTextColor(Color.parseColor("#ffffff"));
                    lost.setBackgroundResource(R.drawable.right_radius_border);
                    get.setBackgroundResource(R.drawable.left_radius_border_white);
                    islostGet = false;
                    isFirst = true;
                    titleList.clear();
                    timeList.clear();
                    siteList.clear();
                    responseList.clear();
                    iWillDo.setText("丢失东西 我要报失");
                    url = "http://szhq.zzu.edu.cn/home/GetLostList/1?PageIndex=";
                    pageNum = 0;
                    doPostGet.getDialog();
                    doPostGet.doGet(url+pageNum,"utf-8");
                    break;
                case R.id.get_lost_iwill:
                    Intent intent = new Intent();
                    intent.setClass(GetLostActivity.this,GetLostIWill.class);
                    intent.putExtra("isLostGet",islostGet);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }
}
