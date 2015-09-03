package com.codeevery.zzudingding;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.codeevery.InfoShow.BlackList;
import com.codeevery.NewStudents.GetRoomNumber;
import com.codeevery.login.BookLoginActivity;
import com.codeevery.zzudingding.R;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by songchao on 15/7/27.
 */
public class tabFragment3 extends Fragment {
    Button button1,button2,button3;
    Context context;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = inflater.getContext();
        View root = inflater.inflate(R.layout.fragment_third,container,false);

        button1 = (Button) root.findViewById(R.id.fragment3_button1);
        button2 = (Button) root.findViewById(R.id.fragment3_button2);
        button3 = (Button) root.findViewById(R.id.fragment3_button3);

        button1.setText("校网黑名单");

        button1.setOnClickListener(new ForButtonListener());
        button2.setOnClickListener(new ForButtonListener());
        button3.setOnClickListener(new ForButtonListener());

        return root;
    }


    class ForButtonListener implements View.OnClickListener{
        Intent intent;
        ForButtonListener(){
            intent = new Intent();
        }
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.fragment3_button1:
                    intent.setClass(context, BlackList.class);
                    break;
                case R.id.fragment3_button2:
                    intent.setClass(context, BookLoginActivity.class);
                    break;
                case R.id.fragment3_button3:
                    intent.setClass(context, GetRoomNumber.class);
                    break;
                default:
                    return;
            }
            startActivity(intent);
        }
    }
}
