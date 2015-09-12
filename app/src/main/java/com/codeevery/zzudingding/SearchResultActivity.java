package com.codeevery.zzudingding;

import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.HttpHeaderParser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.http.protocol.HTTP;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeevery.NetGetPost.VolleyErrorHelper;

import com.codeevery.application.AllObject;
import com.codeevery.myElement.LoadMoreListView;

/**
 * Created by songchao on 15/7/28.
 */

public class SearchResultActivity extends Activity implements LoadMoreListView.IReflashListener {
    private String search;
    private String language;
    private int searchType;
    private String searchTypeString;
    private ImageButton back;
    private Button searchButton;
    private EditText searchText;
    private Button searchHighButton;
    private LinearLayout linearLayout;
    private Spinner searchSpinner;
    private RadioGroup searchRadioGroup;
    private RequestQueue requestQueue;
    private LoadMoreListView bookListView;
    private ProgressDialog progressDialog;
    private AllObject setting;
    private View view;

    private int isFirst = 0;
    //pageNum是书的页数
    private int pageNum = 0;//当前页数
    private int thisPageNum = 0;//当前页的书的数目
    private List<String[]> titleTextList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view = getLayoutInflater().inflate(R.layout.search_result,null);
        setContentView(view);
        setting = (AllObject) getApplication();

        requestQueue = Volley.newRequestQueue(this);
        Intent intent = getIntent();
        search = intent.getStringExtra("search");
        language = intent.getStringExtra("language");
        searchType = intent.getIntExtra("searchType", 0);

        searchButton = (Button) findViewById(R.id.search_button_in);
        searchText = (EditText) findViewById(R.id.search_text);
        searchHighButton = (Button) findViewById(R.id.search_high);
        back = (ImageButton) findViewById(R.id.search_result_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bookListView = (LoadMoreListView) findViewById(R.id.book_listView);
        bookListView.setInterface(this);
        progressDialog = new ProgressDialog(this);


        final View highLinear = LinearLayout.inflate(SearchResultActivity.this, R.layout.highsearch, null);
        searchRadioGroup = (RadioGroup) highLinear.findViewById(R.id.search_radiogroup);
        searchSpinner = (Spinner) highLinear.findViewById(R.id.search_spinner);

        //oneListIn = (LinearLayout) getLayoutInflater().inflate(R.layout.onebooklistview,null).findViewById(R.id.one_list_linear_in);
        //给下拉菜单添加适配器
        final String type[] = {"所有字段", "题名关键词", "作者", "索书号"};
        List<String> list = new ArrayList<>();
        for (int i = 0; i < type.length; i++) {
            list.add(type[i]);
        }
        ArrayAdapter<String> adapterSpin = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, list);

        searchSpinner.setAdapter(adapterSpin);
        //创建自定义弹窗
        AlertDialog.Builder ab = new AlertDialog.Builder(SearchResultActivity.this);
        ab.setTitle("高级选项");
        ab.setView(highLinear);
        final AlertDialog alertDialog = ab.create();
        titleTextList = new ArrayList<>();
        //添加监听事件
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchText.clearFocus();
                search = searchText.getText().toString();
                searchType = searchSpinner.getSelectedItemPosition();
                if (searchRadioGroup.getCheckedRadioButtonId() == R.id.search_lan_china)
                    language = "zzu01";
                else
                    language = "zzu09";
                searchTypeString = getType();

                //各种参数设置好后，开始搜索
                //方法get url=action="http://202.197.191.171:8991/F/92ED4PYTJPPMK3G9EPEBE4I83F5GGPILM287K28BLXDC447GA8-77347
                //拼合url
                //实例化网络请求类
                String url = "http://202.197.191.171:8991/F";
                //String url = "http://www.codeevery.com/dingding";
                //初始化参数
                pageNum = 0;
                isFirst = 0;
                thisPageNum = 0;
                //清空列表
                titleTextList.clear();
                //开始请求
                doGet(url);
                showProgressDialog(0, 0);
            }
        });

        searchText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    setting.hideKeyboard(view);
                }
            }
        });


        searchHighButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //点击之后弹出自定义弹窗
                alertDialog.show();
            }
        });
    }

    //显示进度条的方法
    private void showProgressDialog(int i, int all) {
        if (i == 0 && all == 0) {
            progressDialog.setMessage("正在加载目录....稍后哦");
        } else {
            progressDialog.setMessage("正在加载第" + i + "页 共" + all + "页");
        }
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    //隐藏进度条
    private void hideProgressDialog() {
        progressDialog.cancel();
    }

    public void doGet(String url) {
        //isFirst标志着doget进行了几次。如果次数太多陷入死循环，那么跳出。
        isFirst++;
        if (isFirst > 200) {
            return;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.contains("func=sso")) {
                    //创建pattern对象
                    //Pattern r = Pattern.compile("\\\\s*|\\t|\\r|\\n");
                    //创建matcher对象
                    //Matcher matcher = r.matcher(s);
                    Pattern r = Pattern.compile("http://202.197.191.171.*\\?");
                    Matcher matcher = r.matcher(s);
                    if (matcher.find())
                        s = matcher.group(0);
                    try {
                        search = URLEncoder.encode(search, "UTF-8");
                        Log.i("search Encoder result", "result is:" + search);
                    } catch (UnsupportedEncodingException e) {
                        Log.e("Error", "search URLEncode to UTF-8 is:" + e.getMessage());
                    }
                    s = s + "pds_handle=GUEST&func=find-b&find_code=" + searchTypeString + "&request=" + search + "&local_base=" + language;
                    //获取网址之后进行第二次请求
                    doGet(s);
                } else {
                    //把字符串解析放到一个方法中
                    deals(s);

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                bookListView.refreshComplete();
                Toast.makeText(getBaseContext(), VolleyErrorHelper.getMessage(volleyError, getBaseContext()), Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parsed;
                try {
                    parsed = new String(response.data, HTTP.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    parsed = new String(response.data);
                }
                return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
            }
        };
        requestQueue.add(stringRequest);
    }

    public String getType() {
        //不同的搜索类型int换成不同的类型代码
        switch (searchType) {
            case 0:
                return "WRD";
            case 1:
                return "WTI";
            case 2:
                return "WAU";
            case 3:
                return "CAL";
            default:
                return "WRD";
        }
    }


    private BookAdapter bookAdapter;
    private boolean isScroll = false;
    //下一页的网址
    private String containNextPage;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void deals(String s) {
        //第二次请求获得数据后，提取数据，并且继续请求图片的代码
        Document document = Jsoup.parse(s);
        final Elements hasResult1 = document.select("div[id=hitnum]");
        final Elements hasResult0 = document.select("td[class=text3]");
        //判断请求的数据有没有结果
        if (hasResult0.isEmpty() && hasResult1.isEmpty()) {
            //搜索结果一本书也没有
            hideProgressDialog();
            Toast.makeText(getBaseContext(), "好像没有这本书哦", Toast.LENGTH_SHORT).show();
        }
        //判断搜索结果有书，并且书是很多本，即返回的网页是列表形式
        if (!hasResult1.isEmpty()) {
            //当前页数是第一页
            //搜索结果有多本书的时候
            Pattern pattern;
            Matcher matcher;
            //判断总共结果有多少本书，一页显示多少本书
            String books = hasResult1.first().text();
            pattern = Pattern.compile("of\\s*(.*[^\\s*])\\s*\\(");
            matcher = pattern.matcher(books);
            //获取书的总数，每页显示10本
            int booNum = 10;
            if (matcher.find()) {
                booNum = Integer.parseInt(matcher.group(1));
            }
            //得到有多少书
            final int bookNum = booNum;
            //获取目录下一页的网址
            //匹配下一页的网址的预先字符串
            String nextScript = document.getElementById("nav").getElementsByTag("script").first().html();
            pattern = Pattern.compile("(http://.*).\"\\)");
            matcher = pattern.matcher(nextScript);
            String containNext = null;
            if (matcher.find()) {
                containNext = matcher.group(1);
            }
            //下一页的网址
            containNextPage = containNext;
            //把图书信息写入动态list
            final Elements elements = document.getElementsByClass("items");
            for (int i = 0; i < elements.size(); i++) {
                String titleText[] = new String[8];
                //0是书题目，1是作者，2是索书号，3是出版社，4是年份，5是馆藏地，6是馆藏数量已借数，7是图片网址
                Element tempEle = elements.eq(i).first();
                Element col2 = tempEle.getElementsByClass("col2").first();
                Elements infoAll = col2.getElementsByTag("table").first().getElementsByTag("tr");
                //时用jsoup提取数据，赋值给二维数组，用正则表达式提取数据赋值给二维数组
                titleText[0] = col2.getElementsByClass("itemtitle").first().select("a[href]").first().text();
                titleText[1] = infoAll.eq(0).first().getElementsByTag("td").eq(1).text();
                titleText[2] = infoAll.eq(0).first().getElementsByTag("td").eq(3).text();
                titleText[3] = infoAll.eq(1).first().getElementsByTag("td").eq(3).text();
                titleText[4] = infoAll.eq(1).first().getElementsByTag("td").eq(1).text();
                titleText[5] = infoAll.eq(3).first().getElementsByTag("td").eq(2).select("a[href]").first().text();
                titleText[7] = "http://202.197.191.171:8991" + tempEle.getElementsByClass("cover").first().getElementsByTag("img").first().attr("src");
                String text6 = infoAll.eq(3).first().getElementsByTag("td").eq(2).select("A[href]").first().outerHtml();
                pattern = Pattern.compile("sub_library=.*?>(.*?)</A>*");
                matcher = pattern.matcher(text6);
                titleText[6] = "";
                //用while向外提取数据
                while (matcher.find()) {
                    titleText[6] = titleText[6] + " " + matcher.group(1);
                }
                titleTextList.add(titleText);
            }
            pageNum++;
            int allPageNum;//书的总页数
            if(bookNum%10==0){//如果是10的倍数，那么每一页的书都是10本
                allPageNum=bookNum/10;
                thisPageNum = 10;
            }
            else{//如果不是10的倍数，那么最后一页是除以10的余数
                allPageNum=bookNum/10+1;
                if(pageNum==allPageNum){
                    thisPageNum=bookNum%10;
                }
                else
                    thisPageNum=10;
            }
            if(pageNum>allPageNum){
                Toast.makeText(SearchResultActivity.this,"就搜到这几本书，再没有啦",Toast.LENGTH_SHORT).show();
                bookListView.refreshComplete();
                return;
            }
            //添加ListView
            //添加适配器
            bookListView.setPage(allPageNum,pageNum,thisPageNum);
            if(pageNum==1) {
                LayoutInflater inflater = getLayoutInflater();
                bookAdapter = new BookAdapter(SearchResultActivity.this, inflater, titleTextList, thisPageNum+(pageNum-1)*10);
                bookListView.setAdapter(bookAdapter);
            }
            else {
                bookAdapter.setArrayAndNum(titleTextList, thisPageNum+(pageNum-1)*10);
                bookAdapter.notifyDataSetChanged();
            }

            bookListView.refreshComplete();
            hideProgressDialog();
        }
        if (!hasResult0.isEmpty()) {
            String url = document.getElementById("operate").select("a[href]").first().attr("href");
            doGet(url);
        }
    }

    @Override
    public void onReflash() {
        //这里写上拉加载要加载的东西
        //获取数据
        //通知界面显示
        //通知刷新数据
        doGet(containNextPage + (pageNum * 10 + 1));
    }//我是一朵花
}