package com.codeevery.zzudingding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import com.codeevery.NetGetPost.getPhoto;

/**
 * Created by songchao on 15/8/2.
 * 适配器，图书列表的
 */
public class BookAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater minflater;
    private getPhoto getPhotos;
    private List<String[]> titleTextList;
    private int pageNum;
    public BookAdapter(Context context , LayoutInflater inflater,List<String[]> titleTextList ,int pageNum){
        this.context = context;
        this.minflater = inflater;
        getPhotos = new getPhoto();
        this.titleTextList = titleTextList;
        this.pageNum = pageNum;
    }

    public void setArrayAndNum(List<String[]> titleTextList,int pageNum){
        this.titleTextList = titleTextList;
        this.pageNum = pageNum;
    }

    static class ViewHolder{
        TextView[] oneTextView = new TextView[7];
    }

    @Override
    public int getCount() {
        return pageNum;
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
        ViewHolder viewHolder = null;
        if(convertView==null){
            viewHolder = new ViewHolder();
            convertView = minflater.inflate(R.layout.onebooklistview,null);
            viewHolder.oneTextView[0] = (TextView)convertView.findViewById(R.id.bookinfo1);
            viewHolder.oneTextView[1] = (TextView)convertView.findViewById(R.id.bookinfo2);
            viewHolder.oneTextView[2] = (TextView)convertView.findViewById(R.id.bookinfo3);
            viewHolder.oneTextView[3] = (TextView)convertView.findViewById(R.id.bookinfo4);
            viewHolder.oneTextView[4] = (TextView)convertView.findViewById(R.id.bookinfo5);
            viewHolder.oneTextView[5] = (TextView)convertView.findViewById(R.id.bookinfo6);
            viewHolder.oneTextView[6] = (TextView)convertView.findViewById(R.id.bookinfo7);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        //取出动态数组存储的东西
        String[] getTitle = titleTextList.get(position);
        //动态网络加载图片
        ImageView imageView = (ImageView)convertView.findViewById(R.id.one_list_imageview);

        //获取定义好的TextView

        getPhotos.DisplayImage(getTitle[7], imageView);

        String addType = null;
        for (int i = 0; i < 7; i++) {
            viewHolder.oneTextView[i].setTextColor(context.getResources().getColor(R.color.dimgrey));
            //当i不同的时候给字符串加上不同的字符串，如：作者：张灿，给文字不同的颜色
            switch (i) {
                case 0:
                    viewHolder.oneTextView[i].setTextSize(16);
                    viewHolder.oneTextView[i].setPadding(viewHolder.oneTextView[i].getPaddingLeft(), viewHolder.oneTextView[i].getPaddingTop(), viewHolder.oneTextView[i].getPaddingRight(), 20);
                    addType = "";
                    break;
                case 1:
                    addType = "作者:";
                    break;
                case 2:
                    viewHolder.oneTextView[i].setTextColor(context.getResources().getColor(R.color.cpb_red));
                    addType = "索书号:";
                    break;
                case 3:
                    addType = "出版年份:";
                    break;
                case 4:
                    addType = "出版社:";
                    break;
                case 5:
                    viewHolder.oneTextView[i].setTextColor(context.getResources().getColor(R.color.cpb_blue));
                    addType = "";
                    break;
                case 6:
                    //if(!getTitle[position][i].contains("中心馆"))
                    viewHolder.oneTextView[i].setTextColor(context.getResources().getColor(R.color.cpb_green));
                    addType = "馆藏地:";
                    break;
                default:
                    addType = "";
                    break;
            }
            viewHolder.oneTextView[i].setText(addType + getTitle[i]);
        }
        return convertView;
    }
}
