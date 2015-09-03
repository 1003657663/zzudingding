package com.codeevery.myElement;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.codeevery.zzudingding.R;

/**
 * Created by songchao on 15/8/3.
 */
public class LoadMoreListView extends ListView implements ListView.OnScrollListener {
    private ViewGroup foot = null;
    private int footHeight = 150;//ListView的脚的高度；
    private float ydown, yup;//按下的和拖动，起来的y坐标
    private int state = -1;
    private boolean isTouch = true;//用来判断是否到达底部并且停止滚动
    private final int NONE = 0;//正常状态
    private final int PULL = 1;//提示上拉加载的状态
    private final int RELESH = 2;//提示松开加载的状态
    private final int REFLASHING = 3;//提示正在加载的状态
    private int scrollState;//当前滚动状态
    private Animation refreRotate;//动画

    private ImageView footImageView;
    private TextView footTextView;

    public LoadMoreListView(Context context) {
        super(context);
        init();
    }

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /*
    *添加初始化代码
     */
    private void init() {
        this.setOnScrollListener(this);
        foot = (ViewGroup) inflate(getContext(),R.layout.book_list_foot,null);
        foot.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, footHeight));
        if(this.getFooterViewsCount()==0)
            this.addFooterView(foot);
        footImageView = (ImageView) foot.findViewById(R.id.foot_image_view);
        footTextView = (TextView) foot.findViewById(R.id.foot_text_view);

        refreRotate = AnimationUtils.loadAnimation(getContext(),R.anim.refreshrotate);
        LinearInterpolator lin = new LinearInterpolator();
        refreRotate.setInterpolator(lin);
        setPad(0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isTouch) {
                    ydown = event.getY();
                    state=NONE;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                isTouch=true;
                if (state == RELESH) {
                    state = REFLASHING;
                    //这里是刷新动画、并显示结果
                    if(pageNum < allPageNum){
                        footImageView.setImageDrawable(getResources().getDrawable(R.drawable.refreshicon));
                        if(refreRotate!=null)
                            footImageView.startAnimation(refreRotate);
                        setPad(footHeight);
                        iReflashListener.onReflash();
                    }
                    else if(pageNum == allPageNum){
                        setPad(0);
                    }
                } else if (state == PULL) {
                    state = NONE;
                    //松开后这里是返回正常状态动画
                    setPad(0);
                }
                state = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                if(state!=-1) {
                    yup = event.getY();
                    onMove(event);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void onMove(MotionEvent event) {
        int distance = (int) ((ydown - yup)*0.5);
        switch (state) {
            case NONE:
                if (distance > 0)
                    state = PULL;
                break;
            case PULL:
                setPad(distance);
                if (distance > footHeight + 60 && scrollState == SCROLL_STATE_TOUCH_SCROLL)//高度大于给定高度，且正在滚动
                    state = RELESH;
                break;
            case RELESH:
                setPad(distance);
                if (distance < footHeight + 60)
                    state = PULL;
                else if (distance <= 0) {
                    state = NONE;
                }
                break;
            case REFLASHING:
                break;
            default:
                break;
        }
        showByState();
    }

    private void showByState() {
        switch (state) {
            case NONE:
                setPad(0);
                break;
            case PULL:
                footTextView.setText("继续拉，加载下一页");
                footImageView.setImageDrawable(getResources().getDrawable(R.drawable.more01));
                break;
            case RELESH:
                footTextView.setText(warnStr);
                footImageView.setImageDrawable(getResources().getDrawable(R.drawable.more02));
                break;
            case REFLASHING:
                break;
            default:
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE&& view.getLastVisiblePosition() + 1 == view.getCount()) {
            //如果滚动停止判断是否到底部,如果到底部那么
            isTouch = false;
        }
        else{
            isTouch=true;
        }
        this.scrollState = scrollState;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(visibleItemCount>everyPageNum && allPageNum>1 &&pageNum==1){
            iReflashListener.onReflash();
        }

        if(!hasSet) {
            if (visibleItemCount > everyPageNum && allPageNum == 1) {
                if (this.getFooterViewsCount() > 0)
                    this.removeFooterView(foot);
            } else if ((visibleItemCount <= everyPageNum && allPageNum == 1) || allPageNum > 1) {
                if (this.getFooterViewsCount() == 0) {
                    this.addFooterView(foot);
                    setPad(0);
                }
            }
            hasSet = true;
        }
    }//我是一朵花


    private void setPad(int padding) {
        if(refreRotate!=null) {
            if (padding == 0) {
                if (refreRotate.hasStarted())
                    footImageView.clearAnimation();
            }
        }
        if (this.getFooterViewsCount() == 0) {
            this.setPadding(0, this.getPaddingTop(), 0, padding);
        } else {
            this.setPadding(0, this.getPaddingTop(), 0, (footHeight * -1) + padding);
        }
    }



    IReflashListener iReflashListener;
    public void setInterface(IReflashListener iReflashListener){
        this.iReflashListener = iReflashListener;
    }

    //刷新数据接口
    public interface IReflashListener{
        public void onReflash();
    }
    //刷新成功调用这个函数
    public void refreshComplete(){
        setPad(0);
        if(refreRotate.hasStarted())
            footImageView.clearAnimation();
    }
    //设置，有多少页书，现在是多少页
    private String warnStr;
    private int allPageNum=0,pageNum=0;
    private int everyPageNum;
    private boolean hasSet = false;

    public void setPage(int allPageNum,int pageNum,int everyPageNum){
        this.allPageNum = allPageNum;
        this.pageNum = pageNum;
        this.everyPageNum = everyPageNum;
        if(pageNum<allPageNum)
            warnStr = "共"+allPageNum+"页 松开加载第"+(pageNum+1)+"页";
        else if(pageNum==allPageNum)
            warnStr = "共"+allPageNum+"页 没有啦";
        hasSet = false;
    }//我是一朵花
}
