package com.codeevery.CheckGrade;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.codeevery.zzudingding.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codeevery.NetGetPost.VolleyErrorHelper;
import com.codeevery.application.AllObject;
import com.codeevery.myElement.MyGradeScrollView;

/**
 * Created by songchao on 15/8/5.
 */
public class CheckGradeActivity extends Activity {
    private RequestQueue requestQueue;
    private String mima = "";
    private String xuehao = "";
    private String nianji = "";
    private AllObject setting;
    private int screenWidth;
    private ProgressDialog progressDialog;
    private ViewPager viewPager;
    private List<View> viewLists;
    private PagerAdapter pagerAdapter;
    private int pageNum = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(CheckGradeActivity.this);
        progressDialog.setMessage("正在获取信息..");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        //变成横屏
        if (getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            viewPager = (ViewPager) getLayoutInflater().inflate(R.layout.gradeviewpager, null);
            setContentView(viewPager);
            //新建一个网络请求类
            requestQueue = Volley.newRequestQueue(this);
            //获取application
            setting = (AllObject) getApplication();
            if (setting.isLoginSuccess) {
                this.mima = setting.mima;
                this.xuehao = setting.xuehao;
                this.nianji = setting.xuehao.substring(0, 4);
            }
            //初始化界面

            //初始化组件
            viewLists = new ArrayList<>();

            //获取屏幕宽度
            WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            screenWidth = wm.getDefaultDisplay().getWidth();
            //接下来开始网络请求
            //第一次请求的网址
            Toast.makeText(CheckGradeActivity.this, "可以左右滑动哦！", Toast.LENGTH_SHORT).show();
            String url = "http://jw.zzu.edu.cn/scripts/qscore.dll/search";
            doPost(url);
            showDialog();
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void showDialog() {
        //初始化进度对话框
        progressDialog.show();
    }

    private void hideDialog() {
        progressDialog.hide();
    }

    boolean isFirst = true;
    int flags = 0;//适配器的返回值，每一次++
    String[] links = null;//记录每学期的链接
    String[] termNum = null;//记录每一学期的学期数字

    private void doPost(String url) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //如果请求成功，判断请求是否返回有效信息
                if (s.contains("第1学期")) {
                    MyGradeScrollView myGradeScrollView = new MyGradeScrollView(CheckGradeActivity.this);
                    myGradeScrollView.setBackListener(new backClickListener());
                    Document document = Jsoup.parse(s, "utf-8");
                    String theGrade = document.getElementsContainingOwnText("学期总绩点").text();
                    Element table = document.getElementsByTag("table").first();
                    Elements tr = table.getElementsByTag("tr");
                    for (int i = 0; i < tr.size(); i++) {
                        Elements td = tr.eq(i).first().getElementsByTag("td");
                        TableRow tableRow = new TableRow(CheckGradeActivity.this);
                        tableRow.setPadding(0,10,0,10);
                        tableRow.setBackgroundResource(R.drawable.border);
                        for (int h = 0; h < td.size(); h++) {
                            TextView textView = new TextView(CheckGradeActivity.this);
                            if (h == 0) {
                                textView.setWidth((int) (screenWidth * (3.0 / (td.size() + 2))));
                            } else {
                                textView.setWidth((int) (screenWidth * (1.0 / (td.size() + 2))));
                            }
                            String text = td.eq(h).first().text();
                            textView.setTextSize(16);
                            textView.setTextColor(getResources().getColor(R.color.primary_text));
                            textView.setText(text);
                            tableRow.addView(textView);
                        }
                        myGradeScrollView.addTabRow(tableRow);
                    }
                    viewLists.add(myGradeScrollView);
                    flags++;

                    if (isFirst) {
                        pagerAdapter = new PagerAdapter() {
                            @Override
                            public int getCount() {
                                return flags;
                            }

                            @Override
                            public boolean isViewFromObject(View view, Object object) {
                                return view == object;
                            }

                            @Override
                            public void destroyItem(ViewGroup container, int position, Object object) {
                                container.removeView(viewLists.get(position));
                            }

                            @Override
                            public Object instantiateItem(ViewGroup container, int position) {
                                container.addView(viewLists.get(position));
                                return viewLists.get(position);
                            }
                        };
                        viewPager.setAdapter(pagerAdapter);

                        Elements linksElements = document.getElementsContainingOwnText("第1学期").first().parent().select("a[href]");
                        pageNum = linksElements.size();
                        links = new String[pageNum];
                        termNum = new String[pageNum];
                        for (int r = 0; r < pageNum - 1; r++) {
                            links[r] = linksElements.eq(r).first().attr("href");
                            termNum[r] = linksElements.eq(r).first().text();
                        }
                        myGradeScrollView.setAllGradeText("第" + pageNum + "学期   " + theGrade);
                        hideDialog();
                        isFirst = false;
                    } else {
                        myGradeScrollView.setAllGradeText("第" + (pageNum - flags + 1) + "学期   " + theGrade);
                    }
                    if (flags != pageNum && links[pageNum - flags - 1] != null) {
                        doPost(links[pageNum - flags - 1]);
                    }
                    pagerAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CheckGradeActivity.this, "系统没有找到你的信息，可能是出了什么差错，到电脑上试试吧", Toast.LENGTH_LONG).show();
                    progressDialog.hide();
                    finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(CheckGradeActivity.this, VolleyErrorHelper.getMessage(volleyError, getBaseContext()), Toast.LENGTH_SHORT).show();
                progressDialog.hide();
            }
        }) {
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parase;
                try {
                    parase = new String(response.data, "gb2312");
                } catch (UnsupportedEncodingException e) {
                    parase = new String(response.data);
                    e.printStackTrace();
                }
                return Response.success(parase, HttpHeaderParser.parseCacheHeaders(response));
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();
                map.put("nianji", nianji);
                map.put("xuehao", xuehao);
                map.put("mima", mima);
                return map;
            }
        };
        requestQueue.add(stringRequest);
    }

    class backClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        progressDialog.dismiss();
        super.onDestroy();
    }
}
