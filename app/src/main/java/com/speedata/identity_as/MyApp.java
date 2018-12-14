package com.speedata.identity_as;

import android.app.Application;

/**
 * @author :Reginer in  2018/4/9 15:05.
 * 联系方式:QQ:282921012
 * 功能描述:
 */
public class MyApp extends Application {

    private static MyApp sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static MyApp getInstance() {
        return sInstance;
    }

}
