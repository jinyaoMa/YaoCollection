package ca.jinyao.ma.video;

import android.app.Application;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by oceanzhang on 2017/9/28.
 */

public class AppName extends Application{

    public static AppName instance = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static AppName appInstance() {
        return instance;
    }
    @Override
    public String getPackageName() {
        if(Log.getStackTraceString(new Throwable()).contains("com.xunlei.downloadlib")) {
            return "com.xunlei.downloadprovider";
        }
        return super.getPackageName();
    }
    @Override
    public PackageManager getPackageManager() {
        if(Log.getStackTraceString(new Throwable()).contains("com.xunlei.downloadlib")) {
            return new DelegateApplicationPackageManager(super.getPackageManager(), super.getPackageName());
        }
        return super.getPackageManager();
    }
}
