package com.codeevery.zzudingding;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

/**
 * Created by songchao on 15/8/8.
 *
 */
public class AboutMe extends Activity {
    RelativeLayout relativeLayout;
    ImageButton back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_me_layout);
        relativeLayout = (RelativeLayout) findViewById(R.id.topRelative);
        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        relativeLayout.setBackgroundResource(R.color.transparent);
    }
}
