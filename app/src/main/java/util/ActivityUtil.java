package util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MyPC on 2017/11/9.
 */


//用于管理Activity 的移除
public class ActivityUtil {

    public static List<Activity> list = new ArrayList<Activity>();

    public static void addActivity(Activity activity){

        list.add(activity);

    }

    public static void removeActivity(Activity activity){


        list.remove(activity);

    }

    public static void rmoveAll(){

        for (Activity activity: list) {

            if (!activity.isFinishing()){

                activity.finish();

            }

        }

        list.clear();



    }

}
