package com.example.manlin.carmap;

/**
 * Created by manlin on 2016/9/28.
 */

public class global {
    private static global instance;

    public static boolean login = false;

    public static carCfg cfg = null;

    private global (){}
    public static synchronized global getInstance() {
        if (instance == null) {
            instance = new global();
        }
        return instance;
    }
}
