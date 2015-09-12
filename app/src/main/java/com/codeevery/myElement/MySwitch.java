package com.codeevery.myElement;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.codeevery.zzudingding.R;

/**
 * Created by songchao on 15/8/22.
 */
public class MySwitch extends CheckBox {

    boolean check = false;

    public MySwitch(Context context) {
        super(context, null);
        init();
    }

    public MySwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setBackgroundResource(R.drawable.switch_checkbox);
    }
}
