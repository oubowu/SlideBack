package com.oubowu.slideback.demo;

import android.app.Application;

import com.oubowu.slideback.ActivityHelper;

/**
 * Created by Oubowu on 2016/9/22 0022 18:13.
 */
public class MyApplication extends Application {

    private ActivityHelper mActivityHelper;

    private static MyApplication sMyApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        mActivityHelper = new ActivityHelper();
        registerActivityLifecycleCallbacks(mActivityHelper);

        sMyApplication = this;

    }

    public static ActivityHelper getActivityHelper(){
        return sMyApplication.mActivityHelper;
    }

}
