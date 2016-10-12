package com.example.manlin.carmap;

import android.content.Context;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by manlin on 2016/9/28.
 */

public class carCfg  {
    private Map<String,Integer> cfg = new HashMap<>();
    private Map<String,RadioGroup> groupList = new HashMap<>();
    private String path;
    private MainActivity sup;

    public String imiName = "";

    public carCfg(String pth) {
        this.path = pth;
        loadCfg();
    }
    public carCfg(String pth,MainActivity sup){
        this.path = pth;
        this.sup = sup;
        loadCfg();
        loadActivity();
    }

    public String getStrCfg(){
        if(cfg.isEmpty())
            return "";
        String tmp = "";
        for (Map.Entry<String,Integer> entry:cfg.entrySet()) {
            tmp += entry.getKey() + ":" + entry.getValue().toString() + ";";
        }
        return tmp.substring(0,tmp.length()-1);
    }

    public void loadCfg(){
        cfg.put("inv",10);
        cfg.put("push",60);
        cfg.put("bg_type",2);
        try {
            FileInputStream fio = new FileInputStream(path + "/setting.car");
            int len = fio.available();
            byte[] buff = new byte[len];
            fio.read(buff);
            fio.close();
            String str = new String(buff);
            cfg2map(str);
        }catch (IOException e) {
        }
        try{
            FileInputStream fio2 = new FileInputStream(path + "/imiName.car");
            int len2 = fio2.available();
            byte[] buff2 = new byte[len2];
            fio2.read(buff2);
            fio2.close();
            imiName = new String(buff2);
        }catch (IOException e){

        }

    }

    public void setCheck(String key,RadioGroup group){
        int value = cfg.get(key);
        for(int i=0;i< group.getChildCount();i++){
            RadioButton tb = (RadioButton) group.getChildAt(i);
            if(value == Integer.parseInt(tb.getContentDescription().toString())) {
                tb.setChecked(true);
            }else
                tb.setChecked(false);
        }
    }

    public Set<Map.Entry<String,Integer>> list(){
        return cfg.entrySet();
    }

    private void cfg2map(String str){
        String[] list = str.split(";");
        if(list.length > 0) {
            for (String one : list) {
                String[] tmp = one.split(":");
                if (tmp.length == 2)
                    cfg.put(tmp[0], Integer.parseInt(tmp[1]));
            }
        }
    }

    private void binddingGroup(){
        bindingGroup("inv",R.id.group);
        bindingGroup("push",R.id.group2);
        bindingGroup("bg_type",R.id.group3);
    }

    private RadioGroup bindingGroup(String name, int id){
        RadioGroup group = (RadioGroup) sup.findViewById(id);
        groupList.put(name,group);
        return group;
    }

    public void saveCfg(){
        for (Map.Entry<String,RadioGroup> entry:groupList.entrySet()) {
            String key = entry.getKey();
            RadioGroup rg = entry.getValue();
            int id = rg.getCheckedRadioButtonId();
            RadioButton rb = (RadioButton) sup.findViewById(id);
            cfg.put(key,Integer.parseInt(rb.getContentDescription().toString()));
        }
        File f = new File(path, "setting.car");
        try {
            FileOutputStream fio = new FileOutputStream(f);
            fio.write(getStrCfg().getBytes());
            fio.close();
        } catch (IOException e) {
        }
    }

    public int getValue(String key){
        return cfg.get(key);
    }

    public void loadActivity(){
        binddingGroup();
        for (Map.Entry<String,RadioGroup> entry:groupList.entrySet()) {
            String key = entry.getKey();
            RadioGroup rg = entry.getValue();
            setCheck(key,rg);
        }
    }

    public void saveName(String name){
        imiName = name;
        File f = new File(path, "imiName.car");
        try {
            FileOutputStream fio = new FileOutputStream(f);
            fio.write(name.getBytes());
            fio.close();
        } catch (IOException e) {
        }
    }
}
