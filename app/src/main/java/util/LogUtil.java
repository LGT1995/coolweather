package util;

import android.util.Log;

/**
 * Created by MyPC on 2017/11/2.
 */


//自定义log工具类 设置常量来对是否打印log和对打印的log进行控制

public class LogUtil {

    private static final int VERBOSE = 0;
    private static final int DEBUG = 1;
    private static final int INFO = 2;
    private static final int WARN = 3;
    private static final int ERROR = 4;
    private static final int NOTHING = 5;
    private static int level = VERBOSE;

    public static void v(String tag,String mes){

        if (VERBOSE >= level){

            Log.v(tag,mes);

        }

    }

    public static void d(String tag,String mes){

        if (DEBUG >= level){

            Log.d(tag,mes);

        }

    }

    public static void i(String tag,String mes){

        if (INFO >= level){

            Log.i(tag,mes);

        }

    }

    public static void w(String tag,String mes){

        if (WARN >= level){

            Log.w(tag,mes);

        }

    }

    public static void e(String tag,String mes){

        if (ERROR >= level){

            Log.e(tag,mes);

        }

    }


}
