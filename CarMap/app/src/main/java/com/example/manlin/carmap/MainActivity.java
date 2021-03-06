package com.example.manlin.carmap;

import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.trace.LBSTraceClient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private Button button2;
    private carCfg cfg;
    private Intent intentService = new Intent("com.example.manlin.carmap.RECEIVER");

    private MsgReceiver msgReceive = null;

    public static MainActivity self;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        self = this;
        if(!global.getInstance().login) {
            startActivity(new Intent(MainActivity.this, Main2Activity.class));
        }
        else {
            loadAll();
        }

    }

    @Override
    protected void onDestroy(){
        if(msgReceive != null)
            unregisterReceiver(msgReceive);
        super.onDestroy();
        if(global.getInstance().login &&global.getInstance().cfg.getValue("bg_type")!=1){
            intentService.putExtra("type",3);
            sendBroadcast(intentService);
        }
        System.exit(0);
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

    public void loadAll(){
        File file = getDir("config", MODE_PRIVATE);
        cfg = new carCfg(file.getPath(), this);
        global.getInstance().cfg = cfg;
        msgReceive = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.manlin.carmap.REC_SERVICE");
        registerReceiver(msgReceive,intentFilter);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cfg.saveCfg();
                sendSetting();
            }
        });

        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentService.putExtra("type",2);
                sendBroadcast(intentService);
                System.out.println("获取设置");
            }
        });
//        scanBlueTooth();
        startTrace();
    }

    private void startTrace(){
        if(!MainActivity.isStart(getApplicationContext()))
            startTrace(getApplicationContext(),1);
    }
    public static boolean isStart(Context context){
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);
        if (serviceList.size()>0){
            for(int i = 0;i<serviceList.size();i++){
                if(serviceList.get(i).service.getClassName().equals("com.example.manlin.carmap.MyService")){
                    isRunning = true;
                    break;
                }
            }
        }
        return isRunning;
    }
    public static void startTrace(Context context,int startType) {
        Intent intent = new Intent();
        //设置设备名
        if(!global.getInstance().cfg.imiName.equals("")) {
            intent.putExtra("imei", global.getInstance().cfg.imiName);
            intent.putExtra("startType",startType);
            for (Map.Entry<String, Integer> entry : global.getInstance().cfg.list()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(context, MyService.class);
            context.startService(intent);
        }
    }

    private void sendSetting(){
        intentService.putExtra("type",1);
        for (Map.Entry<String,Integer> entry:cfg.list()) {
            intentService.putExtra(entry.getKey(),entry.getValue());
        }
        sendBroadcast(intentService);
        System.out.println("发送设置");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_BACK){
            exitBy2Click();
        }
        return false;
    }

    private static boolean isExit = false;

    private void exitBy2Click(){
        Timer tExit = null;
        if(isExit == false){
            isExit = true;
            Toast.makeText(getApplicationContext(),"再按一次推出程序",Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;
                }
            },3000);
        }else{
            finish();
        }
    }

//    private void scanBlueTooth(){
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
//        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(msgReceive, intentFilter);
//
//        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
//        if(adapter.isEnabled()){
//            //BluetoothAdapter.ACTION_REQUEST_ENABLE为启动蓝牙的action
//            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivity(intent);
//        }
//        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        //设置蓝牙可见性的时间，方法本身规定最多可见300秒
//        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
//        startActivity(intent);
//        TextView a = (TextView) findViewById(R.id.textView);
//        a.setText("adfasdf");
//        Set<BluetoothDevice> devices = adapter.getBondedDevices();
//        adapter.startDiscovery();
//        if(devices.size()>0) {
//            for(Iterator iterator = devices.iterator(); iterator.hasNext();) {
//                BluetoothDevice device = (BluetoothDevice) iterator.next();
//                try {
//                    // 连接
//                    connect(device,adapter);
//                    a.setText("连接成功");
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    a.setText("连接失败"+device.getName());
//                }
//            }
//        }
//
//    }
//    private void connect(BluetoothDevice device,BluetoothAdapter adapter) throws IOException {
        // 固定的UUID
//        final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
//        UUID uuid = UUID.fromString(SPP_UUID);
//        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
//        socket.connect();
//        BluetoothSocket tmp;
//        BluetoothSocket mmSocket;
//        try {
//            Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
//            try{
//                tmp = (BluetoothSocket)m.invoke(device, Integer.valueOf(2));
//            }catch(Exception e){
//                e.printStackTrace();
//                return;
//            }
//        }catch(NoSuchMethodException e){
//            e.printStackTrace();
//            return;
//        }
//
//        mmSocket = tmp;
//        adapter.cancelDiscovery();
//        try {
//            mmSocket.connect();
//        }catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public class MsgReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context,Intent intent){
            String action = intent.getAction();
            if(action.equals("com.example.manlin.carmap.REC_SERVICE")) {
                String tmp = intent.getStringExtra("value");
                System.out.println(tmp);
                Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_SHORT).show();
            }else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice devices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                TextView a = (TextView) findViewById(R.id.textView);
                a.setText(a.getText()+"\n"+devices.getAddress());
            }else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)){

            }
        }
    }
}
