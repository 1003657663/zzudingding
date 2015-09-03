package com.codeevery.zzudingding;

import com.codeevery.CheckGrade.ChangePassword;
import com.codeevery.CheckGrade.CheckGradeActivity;
import com.codeevery.CheckGrade.EmptyRoom;
import com.codeevery.InfoShow.SpendMoney;
import com.codeevery.login.CardLoginAuto;
import com.codeevery.login.FoodcardLoginActivity;
import com.codeevery.login.GetLostActivity;
import com.codeevery.login.LoginActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeevery.NetGetPost.GetInternetState;
import com.codeevery.application.AllObject;
import com.codeevery.myElement.GetPostThread;
import com.codeevery.myElement.TextImgButton;
import com.codeevery.myElement.TextImgRightButton;
import com.codeevery.myElement.myDialog;

/**
 * Created by songchao on 15/7/27.
 * 接下来要完成的工作：
 * 1.登陆之后可点击，登陆之前的不可点击，且为灰色状态
 * 2.跳回主activity的不能跳回原fragment问题
 */
public class tabFragment1 extends Fragment {
    private TextImgButton checkGrade;
    private TextImgButton checkEmptyRoom;
    private TextImgButton changePassword;
    private TextImgRightButton getLost;
    private TextImgRightButton checkMoney, spendMoney;
    private Context context;
    private AllObject setting;
    private GetInternetState getInternetState;
    private myDialog dialog;
    private ConnectivityManager cm;//连接检查的系统类
    boolean isClickGetMoneyNum = false;//用来判断是不是点击后在获得饭卡余额还是自动登录获得的

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_first, container, false);
        this.context = inflater.getContext();

        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);//提前获取系统检测类
        //初始化控件
        setting = (AllObject) getActivity().getApplication();
        int width = setting.screenWidth/7;

        checkGrade = (TextImgButton) root.findViewById(R.id.check_grade);
        checkEmptyRoom = (TextImgButton) root.findViewById(R.id.check_empty_room);
        changePassword = (TextImgButton) root.findViewById(R.id.change_password_main_button);
        getLost = (TextImgRightButton) root.findViewById(R.id.get_lost_thing);
        checkMoney = (TextImgRightButton) root.findViewById(R.id.food_card_money);
        spendMoney = (TextImgRightButton) root.findViewById(R.id.food_card_spend_money);

        checkGrade.setTextViewText("成绩查询");
        checkGrade.setImageView(R.drawable.grade);
        checkEmptyRoom.setTextViewText("空教室");
        checkEmptyRoom.setImageView(R.drawable.house);
        changePassword.setTextViewText("修改密码");
        changePassword.setImageView(R.drawable.changepassword);
        getLost.setTextViewText("失物招领");
        getLost.setImageView(R.drawable.get_lost);
        checkMoney.setTextViewText("饭卡余额");
        spendMoney.setTextViewText("消费记录");
        spendMoney.setImageView(R.drawable.spend_money);

        checkGrade.setImageWidth(width);
        checkEmptyRoom.setImageWidth(width);
        changePassword.setImageWidth(width);
        getLost.setImageWidth(width);
        checkMoney.setImageWidth(width);
        spendMoney.setImageWidth(width);

        dialog = new myDialog(context);

        //设置按钮监听
        checkGrade.setOnClickListener(new allButtonListener(context));
        checkEmptyRoom.setOnClickListener(new allButtonListener(context));
        changePassword.setOnClickListener(new allButtonListener(context));
        checkMoney.setOnClickListener(new foodCardListener(context));
        spendMoney.setOnClickListener(new foodCardListener(context));
        getLost.setOnClickListener(new allButtonListener(context));

        //自动登录
        if (!setting.isLoginSuccess && setting.isLoginAutoBox)
            setAutoLogin();
        if (!setting.isCardLoginSuccess && setting.isCardLoginAuto)
            cardLogin();
        isClickGetMoneyNum = false;
        drawMoneyButton(setting.moneyNum);

        //下面这个注释部分是，进度按钮的设置部分，后面会用到
        /*final CircularProgressButton circularButton1 = (CircularProgressButton) root.findViewById(R.id.circulbutton);
        circularButton1.setIndeterminateProgressMode(true);
        circularButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (circularButton1.getProgress() == 0) {
                    circularButton1.setProgress(50);
                } else if (circularButton1.getProgress() == 100) {
                    circularButton1.setProgress(0);
                } else {
                    circularButton1.setProgress(100);
                }
            }
        });*/

        return root;
    }

    @Override
    public void onResume() {
        //在fragment恢复的时候计算时间
        long time2 = System.currentTimeMillis();
        if (time2 - setting.cardLoginTime > 1800000) {//在登陆饭卡中心30分钟之后登陆失效
            setting.isCardLoginSuccess = false;
        }
        super.onResume();
    }

    //饭卡自动登录开始
    private void cardLogin() {
        getInternetState = new GetInternetState(cm);
        if (getInternetState.isNetConnected()) {
            final CardLoginAuto.ForthHandler.SuccessDo successDo = new CardLoginAuto.ForthHandler.SuccessDo() {
                @Override
                public void successDo() {
                    //登陆成功执行的代码
                    setting.isCardLoginSuccess = true;
                    isClickGetMoneyNum = false;
                    getMoneyAndShow();
                }
            };
            final CardLoginAuto cardLoginAuto = new CardLoginAuto(setting, context, false);
            CardLoginAuto.ThirdHandler.SuccessDo1 successDo1 = new CardLoginAuto.ThirdHandler.SuccessDo1() {
                @Override
                public void successDo1() {
                    //请求cookie成功的处理代码
                    cardLoginAuto.startCardLogin(setting.cardXuehao, setting.cardMima, successDo);
                }
            };
            cardLoginAuto.getCheckText(successDo1);
            System.out.println("自动登陆了");
        }
    }

    //自动登录 开始
    private void setAutoLogin() {
        getInternetState = new GetInternetState(cm);
        if (getInternetState.isNetConnected()) {
            loginSuccess = 3;//如果网络正常那么把标志位置为异常
            if (setting.xuehao != null && !setting.xuehao.equals("")) {//在有网络连接的情况先判断是否储存了密码和账号，然后通过网络判断是否过时
                CheckUserInfoAuto();//联网检查账号
            } else {
                //没有存储密码和账号，那么跳转到登陆界面
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setIcon(android.R.drawable.ic_dialog_info);
                builder.setTitle("提醒 自动登陆失败");
                builder.setMessage("是否重新登录呢？");
                builder.setPositiveButton("好的", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(context, LoginActivity.class);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton("不用啦", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.show();
            }
        } else {
            loginSuccess = -1;//网络异常
        }
    }

    //设置一个标志位记录网路或者登陆情况loginsuccess
    //loginsuccess 1表示成功，0表示登陆失败，2表示登陆超时，3表示登陆出现异常，-1表示没有网络
    //检查登陆者信息
    int loginSuccess;

    public void CheckUserInfoAuto() {
        CheckUser checkUser = new CheckUser();
        String url = "http://jw.zzu.edu.cn/scripts/stuinfo.dll/check";
        Map<String, String> map = new HashMap<>();
        map.put("nianji", setting.xuehao.substring(0, 4));
        map.put("xuehao", setting.xuehao);
        map.put("mima", setting.mima);
        GetPostThread loginUserThread = new GetPostThread(checkUser, url, null, false, map);
        loginUserThread.setEncoding("gb2312");
        loginUserThread.start();
    }

    class CheckUser extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                //登陆成功
                if (msg.getData().getString("result").contains("你不是郑州大学学生")) {
                    Toast.makeText(context, "自动登录失败,可能是你修改了密码", Toast.LENGTH_SHORT).show();
                } else {
                    loginSuccess = 1;
                    System.out.println("自动登录成功");
                    setting.webStr = getWebStr(msg.getData().getString("result"));
                    setting.isLoginSuccess = true;
                }
            } else if (msg.what == -1) {//登陆失败
                Toast.makeText(context, "自动登录 服务器异常", Toast.LENGTH_SHORT).show();
                loginSuccess = 0;
            } else if (msg.what == -2) {
                Toast.makeText(context, "自动登录超时", Toast.LENGTH_SHORT).show();
                loginSuccess = 2;//登陆超时
            } else {
                Toast.makeText(context, "自动登录异常", Toast.LENGTH_SHORT).show();
                loginSuccess = 3;//登陆异常
            }
        }
    }

    public String getWebStr(String result) {
        Document document = Jsoup.parse(result);
        String imgSrc = document.select("img[alt=照片]").attr("src");

        //登陆成功
        //获取姓名
        String webStr = "";
        String studentName = document.select("font[color=#0000FF").eq(1).text().substring(7);
        setting.name = studentName;
        webStr = webStr + "<div>"
                + "<img src='" + imgSrc + "'/></div>";

        Elements td = document.select("td");
        for (org.jsoup.nodes.Element t : td) {
            if (t.text() != "" && t.text() != null)
                webStr = webStr + "<p>" + t.text() + "</p>";
        }
        webStr = "<html><head>"
                + "<meta http-equiv=Content-Type content='text/html; charset=utf-8'></head><body>"
                + webStr
                + "</body></html>";
        return webStr;
    }

    class allButtonListener implements View.OnClickListener {
        private Context context;
        private Intent intent;

        public allButtonListener(Context context) {
            this.context = context;
            intent = new Intent();
        }

        @Override
        public void onClick(View v) {
            getInternetState = new GetInternetState(cm);
            if (!getInternetState.isNetConnected()) {
                showdialog("你的网络有问题哦,检查一下吧");
                return;
            }
            if (!setting.isLoginSuccess) {
                switch (v.getId()) {
                    case R.id.check_grade:
                        intent.putExtra("afterDoWhich", 1);
                        break;
                    case R.id.check_empty_room:
                        intent.putExtra("afterDoWhich", 2);
                        break;
                    case R.id.change_password_main_button:
                        intent.putExtra("afterDoWhich", 3);
                        break;
                    case R.id.get_lost_thing:
                        intent.putExtra("afterDoWhich", 4);
                    default:
                        break;
                }
                Toast.makeText(context, "请登录教务中心", Toast.LENGTH_SHORT).show();
                intent.setClass(context, LoginActivity.class);
                startActivity(intent);
            } else {
                switch (v.getId()) {
                    case R.id.check_grade:
                        intent.setClass(context, CheckGradeActivity.class);
                        break;
                    case R.id.check_empty_room:
                        intent.setClass(context, EmptyRoom.class);
                        break;
                    case R.id.change_password_main_button:
                        intent.setClass(context, ChangePassword.class);
                        break;
                    case R.id.get_lost_thing:
                        intent.setClass(context, GetLostActivity.class);
                        break;
                    default:
                        break;
                }
                context.startActivity(intent);
            }
        }
    }

    class foodCardListener implements View.OnClickListener {
        Intent intent;
        Context context;

        public foodCardListener(Context context) {
            this.context = context;
            intent = new Intent();
        }

        @Override
        public void onClick(View v) {
            getInternetState = new GetInternetState(cm);
            if (!getInternetState.isNetConnected()) {
                showdialog("你的网络有问题哦,检查一下吧");
                return;
            }
            switch (v.getId()) {
                case R.id.food_card_money:
                    if (setting.isCardLoginAuto && !setting.isCardLoginSuccess) {
                        showdialog("正在自动登陆,再等一会吧,实在不行就注销了重新登陆哦");
                        return;
                    }
                    intent.putExtra("afterWhich", 1);
                    break;
                case R.id.food_card_spend_money:
                    intent.putExtra("afterWhich", 2);
                    intent.setClass(context, SpendMoney.class);
                    break;
                default:
                    break;
            }
            if (!setting.isCardLoginSuccess) {
                intent.setClass(context, FoodcardLoginActivity.class);
                startActivity(intent);
            } else {
                if (v.getId() == R.id.food_card_money) {
                    //这里直接查询余额显示在按钮上
                    isClickGetMoneyNum = true;
                    getMoneyAndShow();
                } else if (v.getId() == R.id.food_card_spend_money) {
                    startActivity(intent);
                }
            }
        }
    }

    private void getMoneyAndShow() {
        //第七次请求，获取饭卡余额等信息
        final Handler seventhHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                dialog.hideProgressDialog();
                if (msg.what == -1) {
                    showdialog("服务器错误,请稍后重试一下下");
                    return;
                } else if (msg.what == -2) {
                    showdialog("登陆超时,请稍后重试一下下");
                    return;
                }
                //获取到了信息，进行处理，并显示在按钮上面
                String result = msg.getData().getString("result");
                dealMoneyInfo(result);
            }
        };
        //第六此请求，再次获得，第二个ID cookie
        final Handler sixthHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == -1) {
                    showdialog("服务器错误,请稍后重试一下下");
                    return;
                } else if (msg.what == -2) {
                    showdialog("登陆超时,请稍后重试一下下");
                    return;
                }
                CookieStore cookieStore = (CookieStore) msg.obj;
                List<HttpCookie> cookies = cookieStore.getCookies();
                for (HttpCookie cookie : cookies) {
                    if (cookie.getDomain().contains("ecard.zzu.edu.cn")) {
                        String a[] = cookie.toString().split("=");
                        if (a[0].equals("JSESSIONID")) {
                            setting.cardCookieMap.put(a[0] + "&1234", a[1]);
                        } else {
                            setting.cardCookieMap.put(a[0], a[1]);
                        }
                    }
                }
                Map<String, String> map = new HashMap<>();
                map.put("start", "0");
                map.put("themeId", "370");
                map.put("whereSql", "%5B%5D");
                map.put("limit", "30");
                map.put("pageSize", "30");
                String url = "http://ecard.zzu.edu.cn/query/queryData.do";
                cookieStore.removeAll();
                GetPostThread getPostThread1 = new GetPostThread(seventhHandler, url, setting.cardCookieMap, false, map);
                getPostThread1.setNoCookieManager(false);
                getPostThread1.start();
                //new GetPostThread(seventhHandler, url, null, false, map).start();
            }
        };

        //第五次，请求，获取饭卡数据
        final Handler fifthHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == -1) {
                    showdialog("服务器错误,请稍后重试一下下");
                    return;
                } else if (msg.what == -2) {
                    showdialog("登陆超时,请稍后重试一下下");
                    return;
                }
                String result = msg.getData().getString("result");
                String frameUrl = "http://ecard.zzu.edu.cn" + Jsoup.parse(result).getElementsByTag("iframe").first().attr("src");
                new GetPostThread(sixthHandler, frameUrl, setting.cardCookieMap, true).start();
                //new GetPostThread(sixthHandler, frameUrl, null, true).start();
            }
        };

        //继续请求下一个cookie
        if (isClickGetMoneyNum) {
            dialog.showProgressDialog("正在获取余额..");
        }
        String infoUrl = "http://ecard.zzu.edu.cn/web/guest/stu?p_p_id=DataObject_INSTANCE_PDsL&p_p_lifecycle=0&p_p_state=maximized&p_p_mode=view&p_p_col_id=column-1&p_p_col_count=5&_DataObject_INSTANCE_PDsL_forward_=query&_DataObject_INSTANCE_PDsL_themeId_=370&_DataObject_INSTANCE_PDsL_themeName_=%E4%B8%AA%E4%BA%BA%E8%B4%A6%E6%88%B7%E4%BF%A1%E6%81%AF";
        new GetPostThread(fifthHandler, infoUrl, setting.cardCookieMap, true).start();
    }

    private int failNum = 0;
    private boolean isDraw = false;

    private void dealMoneyInfo(String info) {
        //处理数据
        Pattern pattern = Pattern.compile(",\"ODDFARE\":\"([0-9.]*)\",");
        Matcher matcher = pattern.matcher(info);
        int moneyNum = 0;
        if (matcher.find()) {
            failNum = 0;
            moneyNum = (int) Float.parseFloat(matcher.group(1));
            setting.moneyNum = moneyNum;
            //把饭卡余额写入文件
            SharedPreferences spf = context.getSharedPreferences("dingding", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = spf.edit();
            editor.putInt("moneyNum", moneyNum);
            editor.commit();
            if (isClickGetMoneyNum) {
                if (moneyNum <= 10) {
                    Toast.makeText(context, "变穷光蛋啦:" + matcher.group(1), Toast.LENGTH_SHORT).show();
                } else if (moneyNum <= 50 && moneyNum > 10) {
                    Toast.makeText(context, "库存仅剩:" + matcher.group(1) + " 该充钱啦", Toast.LENGTH_SHORT).show();
                } else if (moneyNum > 50 && moneyNum <= 200) {
                    Toast.makeText(context, "小有剩余:" + matcher.group(1), Toast.LENGTH_SHORT).show();
                } else if (moneyNum > 200) {
                    Toast.makeText(context, "库存富余:" + matcher.group(1), Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            failNum++;
            if (failNum == 1) {
                getMoneyAndShow();
                return;
            } else if (failNum > 1 && isDraw) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("请求失败,是否重新请求? 您可以注销饭卡后重新登陆");
                builder.setPositiveButton("重新登陆", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getMoneyAndShow();
                        return;
                    }
                });
                builder.show();
            }
        }
        if (checkMoney != null && failNum == 0) {
            drawMoneyButton(moneyNum);
        }
    }

    private void drawMoneyButton(int moneyNum) {
        //这里设置按钮
        Bitmap bitmap = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setColor(Color.parseColor("#00b4d4"));
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getHeight() / 2, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(bitmap.getWidth() / 3);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("" + moneyNum, bitmap.getWidth() / 2, 5 * bitmap.getHeight() / 8, paint);
        checkMoney.setImageView(bitmap);
        if (failNum == 0) {
            if (checkMoney.getImageView().getAnimation() == null) {
                Animation animation = new ScaleAnimation(1.2f, 0.8f, 1.2f, 0.8f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setDuration(1000);
                animation.setRepeatCount(5);
                animation.setRepeatMode(Animation.REVERSE);
                checkMoney.getImageView().setAnimation(animation);
                animation.start();
            } else {
                checkMoney.getImageView().getAnimation().reset();
                checkMoney.getImageView().getAnimation().startNow();
            }
        }
    }


    private void showdialog(String state) {
        dialog.showDialogWithSure(state, "确定");
    }
}


