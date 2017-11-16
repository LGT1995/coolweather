package util;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

/**
 * Created by MyPC on 2017/11/2.
 */


//用于获取全局Context
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        LitePal.initialize(context);
    }

    public static Context getContext(){

        return context;

    }
}
