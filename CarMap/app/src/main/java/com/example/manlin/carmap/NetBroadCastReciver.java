package com.example.manlin.carmap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.io.File;

public class NetBroadCastReciver extends BroadcastReceiver {
    public NetBroadCastReciver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //此处是主要代码，
        //如果是在开启wifi连接和有网络状态下
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            if(NetworkInfo.State.CONNECTED==info.getState()){

                //执行后续代码
                //new AutoRegisterAndLogin().execute((String)null);
                //ps:由于boradCastReciver触发器组件，他和Service服务一样，都是在主线程的，所以，如果你的后续操作是耗时的操作，请new Thread获得AsyncTask等，进行异步操作
                File file = context.getDir("config", context.MODE_PRIVATE);
                global.getInstance().cfg = new carCfg(file.getPath());
                boolean started = MainActivity.isStart(context);
                if(!started&&global.getInstance().cfg.getValue("bg_type")==1) {
                    Toast.makeText(context, "网络连接成功,启动carmap中:" + global.getInstance().cfg.imiName, Toast.LENGTH_SHORT).show();
                    MainActivity.startTrace(context, 1);
                    Toast.makeText(context, "启动carmap成功", Toast.LENGTH_SHORT).show();
                }else if(started){
                    Intent intentService = new Intent("com.example.manlin.carmap.RECEIVER");
                    intentService.putExtra("type",5);
                    context.sendBroadcast(intentService);
                }
            }else{

            }
        }
    }
}
