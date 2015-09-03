package com.codeevery.zzudingding;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codeevery.InfoShow.MyInfo;
import com.codeevery.application.AllObject;
import com.codeevery.login.FoodcardLoginActivity;
import com.codeevery.login.LoginActivity;
import com.codeevery.myElement.MySwitch;
import com.codeevery.myElement.myDialog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by songchao on 15/8/21.
 * 设置界面
 */
public class SetActivity extends Activity {
    MySwitch jiaowuSwitch;
    AllObject setting;
    LinearLayout jiaowuLayout;
    LinearLayout cardLayout, jiaowuResetLayout, cardResetLayout;
    MySwitch cardSwitch;
    TextView jiaowuTitle, cardTitle;
    SharedPreferences spf;
    SharedPreferences.Editor editor;
    ImageButton backButton;
    ImageView photoButton;
    TextView title, jiaowuLoginText, cardLoginText, topName, topXuehao;
    RelativeLayout relativeLayout;
    Button jiaowuLogin, cardLogin, topLoginButton, aboutMe, feedbackButton;
    myDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_layout);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        everySet();
    }

    private void init() {
        //获取读写入口
        spf = this.getSharedPreferences("dingding", Context.MODE_PRIVATE);
        editor = spf.edit();
        dialog = new myDialog(SetActivity.this);
        setting = (AllObject) getApplication();
        photoButton = (ImageView) findViewById(R.id.set_my_photo);
        jiaowuSwitch = (MySwitch) findViewById(R.id.set_switch_jiaowu);
        jiaowuLayout = (LinearLayout) findViewById(R.id.set_jiaowu);
        cardLayout = (LinearLayout) findViewById(R.id.set_card);
        cardSwitch = (MySwitch) findViewById(R.id.set_switch_card);
        jiaowuTitle = (TextView) findViewById(R.id.set_jiaowu_title);
        cardTitle = (TextView) findViewById(R.id.set_card_title);
        jiaowuResetLayout = (LinearLayout) findViewById(R.id.set_reset_jiaowu);
        cardResetLayout = (LinearLayout) findViewById(R.id.set_reset_card);
        backButton = (ImageButton) findViewById(R.id.back);
        jiaowuLogin = (Button) findViewById(R.id.set_jiaowu_reset_button);
        cardLogin = (Button) findViewById(R.id.set_card_reset_button);
        title = (TextView) findViewById(R.id.title);
        jiaowuLoginText = (TextView) findViewById(R.id.set_jiaowu_reset_text);
        cardLoginText = (TextView) findViewById(R.id.set_card_reset_text);
        topName = (TextView) findViewById(R.id.set_my_name);
        topXuehao = (TextView) findViewById(R.id.set_my_xuehao);
        title.setText("");
        relativeLayout = (RelativeLayout) findViewById(R.id.topRelative);
        relativeLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
        topLoginButton = (Button) findViewById(R.id.set_top_jiaowu_login);
        feedbackButton = (Button) findViewById(R.id.set_feedback_submit);
        aboutMe = (Button) findViewById(R.id.set_about_me_button);
        aboutMe.setOnClickListener(new photoListener());
        feedbackButton.setOnClickListener(new photoListener());
        Bitmap bitmap;
        if ((bitmap = readPhotoFromFile("photo.png")) != null)
            photoButton.setImageBitmap(bitmap);
        else {
            Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.photo);
            bitmap1 = drawphoto(bitmap1);
            photoButton.setImageBitmap(bitmap1);
        }
        photoButton.setClickable(true);
        photoButton.setOnClickListener(new photoListener());
    }

    //每次进入页面resume都会执行reset
    private void everySet() {
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        if (setting.isLoginAutoBox) {
            jiaowuSwitch.setChecked(true);
            jiaowuTitle.setText("教务中心自动登陆 已开启");
            jiaowuTitle.setTextColor(getResources().getColor(R.color.snow));
            jiaowuLayout.setBackgroundColor(getResources().getColor(R.color.lightgreen));
        } else {
            jiaowuTitle.setText("教务中心自动登陆 已关闭");
            jiaowuTitle.setTextColor(getResources().getColor(R.color.primary));
            jiaowuSwitch.setChecked(false);
            jiaowuLayout.setBackgroundColor(getResources().getColor(R.color.snow));
        }

        if (setting.isCardLoginAuto) {
            cardSwitch.setChecked(true);
            cardTitle.setText("校卡中心自动登录 已开启");
            cardTitle.setTextColor(getResources().getColor(R.color.snow));
            cardLayout.setBackgroundColor(getResources().getColor(R.color.lightskyblue));
        } else {
            cardSwitch.setChecked(false);
            cardTitle.setText("校卡中心自动登录 已关闭");
            cardTitle.setTextColor(getResources().getColor(R.color.primary));
            cardLayout.setBackgroundColor(getResources().getColor(R.color.snow));
        }

        if (setting.isLoginSuccess) {
            jiaowuLogin.setText("注销");
            topLoginButton.setVisibility(View.GONE);
            jiaowuLoginText.setTextColor(getResources().getColor(R.color.snow));
            jiaowuResetLayout.setBackgroundResource(R.color.lightblue);
            topName.setVisibility(View.VISIBLE);
            topXuehao.setVisibility(View.VISIBLE);
            topName.setText(setting.name);
            topXuehao.setText(setting.xuehao);
        } else {
            jiaowuLogin.setText("登陆");
            jiaowuLoginText.setTextColor(getResources().getColor(R.color.primary));
            jiaowuResetLayout.setBackgroundResource(R.color.snow);
            topLoginButton.setVisibility(View.VISIBLE);
            topName.setVisibility(View.GONE);
            topXuehao.setVisibility(View.GONE);
            topLoginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(SetActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            });
        }

        jiaowuLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setting.isLoginSuccess) {
                    setting.isLoginSuccess = false;
                    changeColorAni(jiaowuResetLayout, R.color.lightblue, R.color.snow, 21);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(SetActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        if (setting.isCardLoginSuccess) {
            cardLogin.setText("注销");
            cardLoginText.setTextColor(getResources().getColor(R.color.snow));
            cardResetLayout.setBackgroundResource(R.color.light_indigo);
        } else {
            cardLogin.setText("登陆");
            cardLoginText.setTextColor(getResources().getColor(R.color.primary));
            cardResetLayout.setBackgroundResource(R.color.snow);
        }

        cardLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (setting.isCardLoginSuccess) {
                    setting.isCardLoginSuccess = false;
                    changeColorAni(cardResetLayout, R.color.light_indigo, R.color.snow, 31);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(SetActivity.this, FoodcardLoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        cardSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(setting.cardXuehao.equals("") || !setting.isCardRemember){
                        cardSwitch.setChecked(false);
                        dialog.showDialogWithSure("您还没有登陆或者开启记住密码哦,登陆或记住密码后才能开启自动登陆哦", "确定");
                    }else {
                        changeColorAni(cardLayout, R.color.snow, R.color.lightskyblue, 11);
                    }
                } else {
                    changeColorAni(cardLayout, R.color.lightskyblue, R.color.snow, 12);
                }
            }
        });

        jiaowuSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(setting.xuehao.equals("") || !setting.isRememberBox) {
                        jiaowuSwitch.setChecked(false);
                        dialog.showDialogWithSure("您还没有登陆或者开启记住密码哦,登陆或记住密码后才能开启自动登陆哦", "确定");
                    }
                    else {
                        changeColorAni(jiaowuLayout, R.color.snow, R.color.lightgreen, 1);
                    }
                } else {
                    changeColorAni(jiaowuLayout, R.color.lightgreen, R.color.snow, 2);
                }
            }
        });
    }

    private void changeColorAni(final View view, int fromColor, int toColor, final int type) {
        Integer startColor = getResources().getColor(fromColor);
        Integer endColor = getResources().getColor(toColor);
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
        colorAnimation.setRepeatMode(0);
        colorAnimation.setDuration(1000);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                view.setBackgroundColor((Integer) animation.getAnimatedValue());
            }
        });
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                switch (type) {
                    case 1://教务中心打开自动登陆
                        jiaowuTitle.setTextColor(getResources().getColor(R.color.snow));
                        setting.isLoginAutoBox = true;
                        editor.putBoolean("isLoginAutoBox", true);
                        editor.commit();
                        jiaowuTitle.setText("教务中心自动登陆 已开启");
                        break;
                    case 2://教务中心关闭自动登陆
                        jiaowuTitle.setTextColor(getResources().getColor(R.color.primary));
                        setting.isLoginAutoBox = false;
                        editor.putBoolean("isLoginAutoBox", false);
                        editor.commit();
                        jiaowuTitle.setText("教务中心自动登陆 已关闭");
                        break;
                    case 11://饭卡中心打开自动登陆
                        cardTitle.setTextColor(getResources().getColor(R.color.snow));
                        setting.isCardLoginAuto = true;
                        editor.putBoolean("isCardLoginAuto", true);
                        editor.commit();
                        cardTitle.setText("校卡中心自动登录 已开启");
                        break;
                    case 12://饭卡中心关闭自动登陆
                        cardTitle.setTextColor(getResources().getColor(R.color.primary));
                        setting.isCardLoginAuto = false;
                        editor.putBoolean("isCardLoginAuto", false);
                        editor.commit();
                        cardTitle.setText("校卡中心自动登录 已关闭");
                        break;
                    case 21://注销教务中心的账号，把记住密码消掉
                        jiaowuLogin.setText("登陆");
                        SharedPreferences spf = getSharedPreferences("dingding",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = spf.edit();
                        editor.putBoolean("isRememberBox",false);
                        editor.putBoolean("isLoginAutoBox", false);
                        editor.commit();
                        setting.isRememberBox = false;
                        setting.isLoginAutoBox = false;
                        jiaowuLoginText.setTextColor(getResources().getColor(R.color.primary));
                        topLoginButton.setVisibility(View.VISIBLE);
                        topName.setVisibility(View.GONE);
                        topXuehao.setVisibility(View.GONE);
                        topLoginButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.setClass(SetActivity.this, LoginActivity.class);
                                startActivity(intent);
                            }
                        });
                        break;
                    case 31://注销饭卡中心账号
                        SharedPreferences spf1 = getSharedPreferences("dingding",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor1 = spf1.edit();
                        editor1.putBoolean("isCardLoginAuto",false);
                        editor1.putBoolean("isCardRemember",false);
                        editor1.commit();
                        setting.isCardRemember = false;
                        setting.isCardLoginAuto = false;
                        cardLogin.setText("登陆");
                        cardLoginText.setTextColor(getResources().getColor(R.color.primary));
                        break;
                    default:
                        break;
                }
            }
        });
        colorAnimation.start();
    }

    final int IMAGE_REQUEST_CODE = 0;
    final int RESULT_REQUEST_CODE = 2;

    private class photoListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.set_my_photo:
                    showOptionDialog();
                    break;
                case R.id.set_about_me_button:
                    Intent intent1 = new Intent();
                    intent1.setClass(SetActivity.this,AboutMe.class);
                    startActivity(intent1);
                    break;
                case R.id.set_feedback_submit:
                    Intent intent2 = new Intent();
                    intent2.setClass(SetActivity.this,FeedBack.class);
                    startActivity(intent2);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData());
                    break;
                case RESULT_REQUEST_CODE:
                    if (data != null) {
                        showImageToView(data);
                    }
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 320);
        intent.putExtra("outputY", 320);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    private void showImageToView(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            //在这里显示图片
            Bitmap bitmap = bundle.getParcelable("data");
            bitmap = drawphoto(bitmap);
            setting.photoHasChange = true;
            writeToFile(bitmap, "photo.png");
            photoButton.setImageBitmap(bitmap);
            bitmap = drawSmallPhoto(bitmap,setting.actionbarHeight);
            writeToFile(bitmap, "smallPhoto.png");
        }
    }

    public static Bitmap drawphoto(Bitmap bitmap) {
        int bitmapHeight = bitmap.getHeight();
        int radius = bitmapHeight / 2;
        Bitmap newBitmap = Bitmap.createBitmap(bitmapHeight, bitmapHeight, Bitmap.Config.ARGB_8888);
        BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);//设置描边画笔
        strokePaint.setColor(0xFFffffff);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(6);
        Paint paint = new Paint();//设置填充画笔
        paint.setShader(bitmapShader);
        paint.setAntiAlias(true);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawCircle(radius, radius, radius, paint);
        canvas.drawCircle(radius, radius, radius - 3, strokePaint);
        return newBitmap;
    }

    public static Bitmap drawSmallPhoto(Bitmap bitmap,float smallHeight) {
        smallHeight = smallHeight*1.5f;
        int height = bitmap.getHeight();
        float scale = smallHeight / height;
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, height, height, matrix, true);//缩小bitmap
        //平移画圆
        BitmapShader bitmapShader = new BitmapShader(newBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Matrix matrix1 = new Matrix();
        int trans = (int) (smallHeight/3);
        matrix1.setTranslate(trans, 0);
        bitmapShader.setLocalMatrix(matrix1);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(bitmapShader);
        height = newBitmap.getHeight();
        Bitmap bitmap1 = Bitmap.createBitmap(trans+height, height, Bitmap.Config.ARGB_8888);//重建bitmap
        Canvas canvas = new Canvas(bitmap1);
        canvas.drawCircle(height / 2 + trans, height / 2, height / 2, paint);//画圆在右边
        //开始画点
        paint.reset();
        paint.setColor(0xFFffffff);//设置画笔为黑色
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPoints(new float[]{trans/3, smallHeight/3, trans/3, smallHeight/2, trans/3, smallHeight*2/3}, paint);
        return bitmap1;
    }

    private void writeToFile(Bitmap bitmap, String fileName) {
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(getFileStreamPath(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Bitmap readPhotoFromFile(String fileName) {
        if (getFileStreamPath(fileName).exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(getFileStreamPath(fileName).getPath());
            return bitmap;
        }
        return null;
    }

    private void showOptionDialog(){
        LinearLayout linearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.option_dialog,null);
        Button changePhoto = new Button(SetActivity.this);
        Button myInfo = new Button(SetActivity.this);
        Button close = new Button(SetActivity.this);
        myInfo.setText("查看个人信息");
        changePhoto.setText("更改头像");
        close.setText("关闭");
        myInfo.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        changePhoto.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        close.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        myInfo.setGravity(View.TEXT_ALIGNMENT_CENTER);
        changePhoto.setGravity(View.TEXT_ALIGNMENT_CENTER);
        close.setGravity(Gravity.CENTER);
        myInfo.setPadding(20, 40, 20, 40);
        changePhoto.setPadding(20, 40, 20, 40);
        close.setPadding(20, 40, 20, 20);
        myInfo.setBackgroundResource(R.drawable.background);
        changePhoto.setBackgroundResource(R.drawable.background);
        close.setBackgroundResource(R.drawable.background);
        linearLayout.addView(myInfo);
        linearLayout.addView(changePhoto);
        linearLayout.addView(close);
        AlertDialog.Builder builder = new AlertDialog.Builder(SetActivity.this);
        builder.setView(linearLayout);
        final AlertDialog optionDialog = builder.create();
        myInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.cancel();
                if(setting.isLoginSuccess) {
                    Intent intent = new Intent();
                    intent.setClass(SetActivity.this, MyInfo.class);
                    startActivity(intent);
                }else{
                    dialog.showDialogWithSure("您还没有登陆教务系统,不能查看信息哦","确定");
                }
            }
        });
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.cancel();
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, IMAGE_REQUEST_CODE);
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog.cancel();
            }
        });
        optionDialog.show();
    }
}
