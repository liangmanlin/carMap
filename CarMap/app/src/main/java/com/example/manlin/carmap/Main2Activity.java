package com.example.manlin.carmap;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.trace.I;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

public class Main2Activity extends AppCompatActivity {
    private EditText name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        File file = getDir("config", MODE_PRIVATE);
        final carCfg cfg = new carCfg(file.getPath());
        name = (EditText) findViewById(R.id.editText);
        final EditText pw = (EditText) findViewById(R.id.editText5);
        if(cfg.imiName.equals("")){
            name.setText("");
        }else{
            name.setText(cfg.imiName);
            name.setEnabled(false);
            TextView tv = (TextView) findViewById(R.id.textView5);
            tv.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent e) {
                    writeBy2Click();
                    return false;
                }
            });
            pw.setFocusable(true);
            pw.setFocusableInTouchMode(true);
            pw.requestFocus();
            pw.requestFocusFromTouch();
        }
        Button button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String a = Base64.encodeToString("1".getBytes(),1);
                String strName = name.getText().toString();
                String b = Base64.encodeToString(pw.getText().toString().getBytes(),1);
                boolean c = a.equals(b);
                if(a.equals(b)&&!strName.equals("")){
                    global.getInstance().login = true;
                    if(!cfg.imiName.equals(strName))
                        cfg.saveName(strName);
                    startActivity(new Intent(Main2Activity.this,MainActivity.class));
                    finish();
                }else{
                    Toast.makeText(getApplicationContext(),"密码错误或设备标识为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            MainActivity.self.finish();
        }
        return false;
    }
    private static boolean isExit = false;

    private void writeBy2Click(){
        Timer tExit = null;
        if(isExit == false){
            isExit = true;
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            },600);
        }else{
            name.setEnabled(true);
            name.setFocusable(true);
            name.setFocusableInTouchMode(true);
            name.requestFocus();
            name.requestFocusFromTouch();
            Spannable spanText = (Spannable) name.getText();
            Selection.setSelection(spanText,name.getText().length());
            InputMethodManager ipm = (InputMethodManager) name.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            ipm.showSoftInput(name,name.getText().length());
        }
    }

}
