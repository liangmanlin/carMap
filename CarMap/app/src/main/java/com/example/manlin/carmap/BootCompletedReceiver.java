package com.example.manlin.carmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.io.File;


public class BootCompletedReceiver extends BroadcastReceiver {
    static final String ACTION = "android.intent.action.BOOT_COMPLETED";
    public BootCompletedReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        if(intent.getAction().equals(ACTION)) {
            File file = context.getDir("config", context.MODE_PRIVATE);
            global.getInstance().cfg = new carCfg(file.getPath());
            if(global.getInstance().cfg.getValue("bg_type")==1&&!global.getInstance().cfg.imiName.equals("")) {
                if(MainActivity.isStart(context)){
                    Intent intentService = new Intent("com.example.manlin.carmap.RECEIVER");
                    intentService.putExtra("type",4);
                    context.sendBroadcast(intentService);
                }else {
                    Toast.makeText(context, "启动carmap中:" + global.getInstance().cfg.imiName, Toast.LENGTH_SHORT).show();
                    MainActivity.startTrace(context, 2);
                    Toast.makeText(context, "启动carmap成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
