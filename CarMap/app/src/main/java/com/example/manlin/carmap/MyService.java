package com.example.manlin.carmap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.LocationMode;
import com.baidu.trace.OnStartTraceListener;
import com.baidu.trace.OnStopTraceListener;
import com.baidu.trace.OnTrackListener;
import com.baidu.trace.Trace;
import com.baidu.trace.OnGeoFenceListener;
import com.baidu.trace.OnEntityListener;
import com.baidu.trace.TraceLocation;

import  android.content.BroadcastReceiver;
import android.widget.Toast;

public class MyService extends Service {
    private static final String TAG = "MyService";

    private final Handler handler = new Handler();
    private Runnable r ;

    private int failNet = 0;

    // 轨迹服务
    protected static Trace trace = null;

    // 鹰眼服务ID，开发者创建的鹰眼服务对应的服务ID
    public static final long serviceId = 126200;

    // 轨迹服务类型
    //0 : 不建立socket长连接，
    //1 : 建立socket长连接但不上传位置数据，
    //2 : 建立socket长连接并上传位置数据）
    private int traceType = 2;

    // 轨迹服务客户端
    public static LBSTraceClient client = null;

    // Entity监听器
    public static OnEntityListener entityListener = null;

    // 开启轨迹服务监听器
    protected OnStartTraceListener startTraceListener = null;

    // 停止轨迹服务监听器
    protected static OnStopTraceListener stopTraceListener = null;

    // 采集周期（单位 : 秒）
    private int gatherInterval = 10;//global.getInstance().cfg.getValue("inv");

    // 设置打包周期(单位 : 秒)
    private int packInterval = 20;//global.getInstance().cfg.getValue("push");

    protected static boolean isTraceStart = false;

    // 手机IMEI号设置为唯一轨迹标记号,只要该值唯一,就可以作为轨迹的标识号,使用相同的标识将导致轨迹混乱
    private String imei;

    private MsgReceive msgReceive ;

    private Intent  intents = new Intent("com.example.manlin.carmap.REC_SERVICE");

    private int bg_type=1;

    public IBinder onBind(Intent arg0) {
        return new MsgBinder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null && intent.getExtras() != null){
            imei= intent.getStringExtra("imei");
            gatherInterval = intent.getIntExtra("inv",10);
            packInterval = intent.getIntExtra("push",20);
            bg_type = intent.getIntExtra("bg_type",1);
        }
        if(intent.getIntExtra("startType",1)==1)
            init();

        r = new Runnable() {
            @Override
            public void run() {
                checkNetWork();
                handler.postDelayed(this,30000);
            }
        };
        handler.postDelayed(r,30000);
        return super.onStartCommand(intent, START_STICKY, startId);
    }

    //被销毁时反注册广播接收器
    @Override
    public void onDestroy() {
        unregisterReceiver(msgReceive);
        super.onDestroy();
        handler.removeCallbacks(r);
        stopTrace();
    }

    /**
     * 初始化
     */
    private void init() {
        // 初始化轨迹服务客户端
        client = new LBSTraceClient(this);

        // 设置定位模式
        client.setLocationMode(LocationMode.High_Accuracy);

        // 初始化轨迹服务
        trace = new Trace(this, serviceId, imei, traceType);

// 采集周期,上传周期
        client.setInterval(gatherInterval, packInterval);

        // 设置http请求协议类型0:http,1:https
        client.setProtocolType(0);

        // 初始化监听器
        initListener();

        // 启动轨迹上传
        startTrace();

        //注册消息接收器
        regMsgRec();
    }
    // 开启轨迹服务
    private void startTrace() {
        // 通过轨迹服务客户端client开启轨迹服务
        client.startTrace(trace, startTraceListener);
    }

    // 停止轨迹服务
    public static void stopTrace() {
        // 通过轨迹服务客户端client停止轨迹服务
        //LogUtil.i(TAG, "stopTrace(), isTraceStart : " + isTraceStart);

        if(isTraceStart){
            client.stopTrace(trace, stopTraceListener);
        }
    }

    // 初始化监听器
    private void initListener() {

        initOnEntityListener();

        // 初始化开启轨迹服务监听器
        initOnStartTraceListener();

        // 初始化停止轨迹服务监听器
        initOnStopTraceListener();
    }


    /**
     * 初始化OnStartTraceListener
     */
    private void initOnStartTraceListener() {
        // 初始化startTraceListener
        startTraceListener = new OnStartTraceListener() {

            // 开启轨迹服务回调接口（arg0 : 消息编码，arg1 : 消息内容，详情查看类参考）
            public void onTraceCallback(int arg0, String arg1) {
                System.out.println("开启轨迹回调接口 [消息编码 : " + arg0 + "，消息内容 : " + arg1 + "]");
                if (0 == arg0 || 10006 == arg0) {
                    isTraceStart = true;
                }
            }

            // 轨迹服务推送接口（用于接收服务端推送消息，arg0 : 消息类型，arg1 : 消息内容，详情查看类参考）
            public void onTracePushCallback(byte arg0, String arg1) {
                System.out.println("轨迹服务推送接口消息 [消息类型 : " + arg0 + "，消息内容 : " + arg1 + "]");
            }
        };
    }

    // 初始化OnStopTraceListener
    private void initOnStopTraceListener() {
        stopTraceListener = new OnStopTraceListener() {

            // 轨迹服务停止成功
            public void onStopTraceSuccess() {
                System.out.println("停止轨迹服务成功");
                isTraceStart = false;
                //stopSelf();
            }

            // 轨迹服务停止失败（arg0 : 错误编码，arg1 : 消息内容，详情查看类参考）
            public void onStopTraceFailed(int arg0, String arg1) {
                System.out.println("停止轨迹服务接口消息 [错误编码 : " + arg0 + "，消息内容 : " + arg1 + "]");
            }
        };
    }

    // 初始化OnEntityListener
    private void initOnEntityListener() {

        entityListener = new OnEntityListener() {

            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                System.out.println("entity请求失败回调接口消息 : " + arg0);
            }

            // 添加entity回调接口
            @Override
            public void onAddEntityCallback(String arg0) {
                System.out.println("添加entity回调接口消息 : " + arg0);
            }

            // 查询entity列表回调接口
            @Override
            public void onQueryEntityListCallback(String message) {
                System.out.println("onQueryEntityListCallback : " + message);
            }

            @Override
            public void onReceiveLocation(TraceLocation location) {

            }
        };
    }

    public void setInterval(int inv , int push){
        client.setInterval(inv,push);
    }

    private void regMsgRec(){
        msgReceive = new MsgReceive();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.manlin.carmap.RECEIVER");
        registerReceiver(msgReceive,intentFilter);
    }

    private void checkNetWork(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(getApplication().CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        boolean isCT = ni!=null && ni.isConnected();
        //Toast.makeText(getApplicationContext(), "检查网络状态:" + isCT, Toast.LENGTH_SHORT).show();
        if(isCT){
            if(!isTraceStart) {
                Toast.makeText(getApplicationContext(), "网络连接成功，启动鹰眼服务", Toast.LENGTH_SHORT).show();
                init();
            }
            failNet = 0;
        }else{
            failNet++;
            if(failNet >= 3){
                if(isTraceStart) {
                    Toast.makeText(getApplicationContext(), "无网络，关闭鹰眼服务", Toast.LENGTH_SHORT).show();
                    stopTrace();
                }
                failNet = 0;
            }

        }
    }


    public class MsgBinder extends Binder{
        /**
         * 获取当前Service的实例
         * @return
         */
        public MyService getService(){
            return MyService.this;
        }
    }

    public class MsgReceive extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){

            if(intent.getAction().equals("com.example.manlin.carmap.RECEIVER")) {
                int type = intent.getIntExtra("type",2);
                if(type == 1) {
                    gatherInterval = intent.getIntExtra("inv", 10);
                    packInterval = intent.getIntExtra("push", 20);
                    bg_type = intent.getIntExtra("bg_type", 1);
                    client.setInterval(gatherInterval, packInterval);
                    intents.putExtra("value", "设置成功");
                    sendBroadcast(intents);
                }else if(type==2){
                    //接受到app查询消息
                    intents.putExtra("value","setting| inv:" + gatherInterval + ",push:" + packInterval+
                            ",bg_type:"+bg_type+",name:"+imei);
                    sendBroadcast(intents);
                }else if(type==3){
                    //接受到app退出消息
                    if(bg_type != 1){
                        Toast.makeText(context,"接收到退出消息",Toast.LENGTH_SHORT).show();
                        handler.removeCallbacks(r);
                        stopTrace();
                        stopSelf();
                    }
                }else if(type == 4){
                    stopTrace();
                }else if(type == 5){
                    if(!isTraceStart) {
                        Toast.makeText(getApplicationContext(), "收到网络消息，启动鹰眼服务", Toast.LENGTH_SHORT).show();
                        init();
                    }
                }
            }else {
            }
        }
    }
}
