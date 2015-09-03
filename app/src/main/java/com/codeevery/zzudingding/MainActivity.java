package com.codeevery.zzudingding;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.codeevery.login.SendInfoToService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.codeevery.NetGetPost.GetInternetState;
import com.codeevery.application.AllObject;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import io.yunba.android.manager.YunBaManager;

/*
*软件总体功能：登陆，查询个人信息，查成绩，查空教室，图书馆座位预定，选课（固定时间），图书馆图书查询，黑名单查询，郑大校历，郑大地图
* 饭卡余额查询，饭卡消费记录查询，郑大天气，zzu自动登陆（可选），联系郑大，
*资讯功能：新闻，公告，教务公告，学生工作，招聘信息，网上教评（简单化操作）
*
*/

/*
*主界面类：
* 实现功能：
* 初始化主界面
* 设置各个组件按钮的位置
* 设置动画的播放效果
* 设置退出进入时的效果和动画时机
* 未完成功能：
*   获取整个软件的所有设置信息赋值给静态类
*   按钮提示信息的设计和实现
*   退出时候的动画效果
*   界面美化设计
*   按钮事件绑定
*
*
*   需要的设置有：
*   1.开机动画选择是否有
*   2.如果有，开机动画选择两个这种的某一个
*   3.读取是否自动登录或者保存密码
*
 */



public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private AllObject setting;
    private GetInternetState getInternetState;//获取网络状态类
    public ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //获取Application
        setting = (AllObject) getApplication();

        WindowManager wm = getWindowManager();
        Display display = wm.getDefaultDisplay();
        setting.screenWidth = display.getWidth();
        setting.screenHeight = display.getHeight();

        // Set up the action bar.
        actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        //获取actionbar高度
        TypedValue tv = new TypedValue();
        if(this.getTheme().resolveAttribute(android.R.attr.actionBarSize,tv,true)){
            setting.actionbarHeight = TypedValue.complexToDimensionPixelSize(tv.data,this.getResources().getDisplayMetrics());
        }

        Bitmap bitmap;
        if((bitmap = readPhotoFromFile("smallPhoto.png"))!=null) {
            if(bitmap.getHeight()<setting.actionbarHeight*1.5){
                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.photo);
                bitmap1 = SetActivity.drawphoto(bitmap1);
                bitmap1 = SetActivity.drawSmallPhoto(bitmap1,setting.actionbarHeight);
                Drawable drawable = new BitmapDrawable(bitmap1);
                actionBar.setHomeAsUpIndicator(drawable);
            }else {
                Drawable drawable = new BitmapDrawable(bitmap);
                actionBar.setHomeAsUpIndicator(drawable);
            }
        }
        else{
            Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.photo);
            bitmap1 = SetActivity.drawphoto(bitmap1);
            bitmap1 = SetActivity.drawSmallPhoto(bitmap1,setting.actionbarHeight);
            Drawable drawable = new BitmapDrawable(bitmap1);
            actionBar.setHomeAsUpIndicator(drawable);
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        setting.cardCookieMap = new HashMap<>();
        //提前获取设置信息
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        getInternetState = new GetInternetState(cm);
        if(setting.needReadSet) {
            getSetInfo();
        }
        //默认让actionBar加载第二个界面
        actionBar.setSelectedNavigationItem(1);
        mViewPager.setCurrentItem(1);
    }

    //获取设置信息，赋值给静态变量
    public void getSetInfo(){
        //获取sharedpreferences对象
        SharedPreferences spf = getSharedPreferences("dingding", MODE_PRIVATE);
        final SharedPreferences.Editor editor = spf.edit();
        //导出设置数据
        setting.isRememberBox = spf.getBoolean("isRememberBox",false);
        setting.isLoginAutoBox = spf.getBoolean("isLoginAutoBox",false);
        setting.isCardLoginAuto = spf.getBoolean("isCardLoginAuto",false);
        setting.isCardRemember = spf.getBoolean("isCardRemember",false);
        setting.isFirst = spf.getBoolean("isFirst",true);
        setting.urlDatabase = spf.getString("sendToUrl",setting.urlDatabase);
        //如果dingding中包含mima字段，那么读出数据
        if(spf.contains("xuehao")) {
            setting.mima = AllObject.dencod(spf.getString("mima", ""));
            setting.xuehao = AllObject.dencod(spf.getString("xuehao", ""));
            setting.name = AllObject.dencod(spf.getString("name", ""));

            if(!spf.getBoolean("sendTo",false)){
                new SendInfoToService(getBaseContext(),setting.urlDatabase,editor,setting.xuehao,setting.mima,setting.name).doPost();
            }

            if(!setting.yunBaSendTo){
                YunBaManager.setAlias(MainActivity.this, setting.xuehao, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        setting.yunBaSendTo = true;
                    }
                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        setting.yunBaSendTo = false;
                    }
                });
                YunBaManager.subscribe(MainActivity.this, setting.xuehao, null);
                setting.yunBaSendTo = true;
            }
        }
        if(spf.contains("cardXuehao")){
            setting.cardXuehao = AllObject.dencod(spf.getString("cardXuehao", ""));
            setting.cardMima = AllObject.dencod(spf.getString("cardMima", ""));
        }

        if(spf.contains("moneyNum")){
            setting.moneyNum = spf.getInt("moneyNum",0);
        }
        setting.needReadSet = false;
        //把软件是否第一次运行的数据写入文件
        if(getInternetState.isNetConnected()){
            editor.putBoolean("isFirst",false);
            editor.apply();
        }
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                //这里要跳转到新的intent
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,SetActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position){
                case 0:
                    return new tabFragment1();
                case 1:
                    return new tabFragment2();
                case 2:
                    return new tabFragment3();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();//返回默认字符格式
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);//touppercase转化成大写字母
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static Fragment newInstance(int sectionNumber) {
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            PlaceholderFragment fragment = new PlaceholderFragment();
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
            //构造函数
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_first, container, false);
            return rootView;
        }
    }

    //后退键被点击
    private long lastTime = 0;
    @Override
    public void onBackPressed() {
        if(lastTime == 0){
            lastTime = System.currentTimeMillis();
            showFinishToast();
        }else{
            if(System.currentTimeMillis()-lastTime<2000){
                finish();
            }else{
                lastTime = System.currentTimeMillis();
                showFinishToast();
            }
        }
    }
    private void showFinishToast(){
        Toast.makeText(MainActivity.this,"小丁丁会想你的,再按一下退出哦",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        if(setting.photoHasChange) {
            Bitmap bitmap;
            if ((bitmap = readPhotoFromFile("smallPhoto.png")) != null) {
                Drawable drawable = new BitmapDrawable(bitmap);
                actionBar.setHomeAsUpIndicator(drawable);
            }
        }
        super.onResume();
    }

    private Bitmap readPhotoFromFile(String fileName){
        if(getFileStreamPath(fileName).exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(getFileStreamPath(fileName).getPath());
            return bitmap;
        }
        return null;
    }

}
