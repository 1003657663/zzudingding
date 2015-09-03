package com.codeevery.myElement;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codeevery.zzudingding.R;

/**
 * Created by songchao on 15/8/4.
 */
public class TextImgRightButton extends LinearLayout {

    private ImageView imageView;
    private TextView textView;

    Context context;

    public TextImgRightButton(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public TextImgRightButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public TextImgRightButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public void setImageWidth(int width){
        imageView.setLayoutParams(new LinearLayout.LayoutParams(width, width));
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.imgtextbutton_right, this, true);
        imageView = (ImageView) findViewById(R.id.my_img_button_right);
        textView = (TextView) findViewById(R.id.my_text_button_right);
        this.setClickable(true);
        this.setBackgroundResource(R.drawable.imgtextbutton);
    }

    public void setImageView(int resId) {
        imageView.setImageResource(resId);
    }

    public void setImageView(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
    }

    public void setTextViewText(String text) {
        textView.setText(text);
    }

    public ImageView getImageView(){
        return imageView;
    }
}
