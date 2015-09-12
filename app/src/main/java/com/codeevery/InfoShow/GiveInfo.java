package com.codeevery.InfoShow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codeevery.zzudingding.R;

/**
 * Created by songchao on 15/9/2.
 */
public class GiveInfo extends Activity {

    TextView content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.give_info);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String contentText = intent.getStringExtra("content");

        ImageButton back = (ImageButton) findViewById(R.id.back);
        content = (TextView) findViewById(R.id.give_info_content);
        content.setText(contentText);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView titleView = (TextView) findViewById(R.id.title);
        titleView.setText(title);
    }
}
