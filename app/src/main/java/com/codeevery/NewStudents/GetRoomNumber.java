package com.codeevery.NewStudents;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.codeevery.zzudingding.R;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.codeevery.NetGetPost.DoPostGet;
import com.codeevery.myElement.myDialog;


public class GetRoomNumber extends Activity implements DoPostGet.DoSomeThing{
    TextView textView;
    EditText editText;
    Button button;
    ImageButton back;
    TextView title;
    DoPostGet doPostGet;
    int year;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_student_get_room);

        //初始化
        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        title = (TextView) findViewById(R.id.title);
        title.setText("新生班级查询");
        editText = (EditText) findViewById(R.id.show_room_edit);
        textView = (TextView) findViewById(R.id.show_room_year);
        button = (Button) findViewById(R.id.show_room_submit);

        doPostGet = new DoPostGet(this);
        doPostGet.setInterface(this);
        Calendar mCalendar = Calendar.getInstance();
        year =  mCalendar.get(Calendar.YEAR);
        textView.setText("年级：" + year);
        final String url = "http://jw.zzu.edu.cn/scripts/newstu.dll/cx";
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String number = editText.getText().toString();
                if(number.equals(""))

                {
                    Toast.makeText(GetRoomNumber.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> map = new HashMap<>();
                map.put("nj",year+"");
                map.put("sfzh",number);
                map.put("xkstep","1");
                doPostGet.doPost(url,"gb2312",map);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDo(String str) {
        if(str.contains("没有检索到")){
            new myDialog(GetRoomNumber.this).showDialogWithSure("没有检索到你的信息，再试一下","确定");
            return;
        }
        Document document = Jsoup.parse(str);
        Element element = document.getElementsByClass("neirongnews").first().getElementsByTag("table").first().select("table[border=1]").first();
        System.out.println(element.outerHtml());
        Elements td = element.getElementsByTag("tr").get(2).getElementsByTag("td");
        String name = td.get(2).text();
        String banji = td.get(6).text();
        new myDialog(GetRoomNumber.this).showDialogWithSure("姓名："+name+"  "+banji,"确定");
    }
}
