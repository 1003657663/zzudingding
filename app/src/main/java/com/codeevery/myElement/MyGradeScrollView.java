package com.codeevery.myElement;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.codeevery.zzudingding.R;

/**
 * Created by songchao on 15/8/5.
 */
public class MyGradeScrollView extends ScrollView {
    private Context context;
    private TextView textView;
    private ImageButton imageButton;
    private TableLayout tableLayout;
    public MyGradeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public MyGradeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MyGradeScrollView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    private void init(){
        LayoutInflater.from(context).inflate(R.layout.showgrade,this,true);
        imageButton = (ImageButton) findViewById(R.id.grade_back_button);
        textView = (TextView) findViewById(R.id.the_grade_all);
        tableLayout = (TableLayout) findViewById(R.id.show_grade_table_layout);
        textView.setTextSize(16);
    }

    public void setBackListener(OnClickListener onClickListener){
        imageButton.setOnClickListener(onClickListener);
    }

    public void addTabRow(TableRow tableRow){
        tableLayout.addView(tableRow);
    }

    public void setAllGradeText(String text){
        textView.setText(text);
    }
}
