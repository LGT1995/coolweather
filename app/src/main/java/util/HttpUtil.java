package util;

import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * Created by MyPC on 2017/11/2.
 */

public class HttpUtil {

    private static HttpConnectionListener listener = null;

    public void setListener(HttpConnectionListener listener){

        this.listener = listener;

    }


    //根据地址获取对应的信息 连接成功通过回调方法获取输入流
    public void getInput(final String path){

        LogUtil.v("HttpUtil","getInput is working");

        LogUtil.v("HttpUtil","path is "+path);

        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.v("HttpUtil","Thread is "+Thread.currentThread().getName());
                InputStream in = null;
                HttpURLConnection conn = null;
                try{



                    URL url = new URL(path);

                    conn = (HttpURLConnection) url.openConnection();

                    LogUtil.v("HttpUitl","conn is "+conn.toString());

                    conn.setRequestMethod("GET");

                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.connect();
                    LogUtil.v("HttpUtil","======================");

                    int res = conn.getResponseCode();

                    LogUtil.v("HttpUtil","res is "+res);

                    if (res == 200){



                        LogUtil.v("HttpUtil"," res is 200");

                        in = conn.getInputStream();

                        listener.OnSucceed(in);
                    }

                }catch (Exception e){

                    //打印错误信息
                    LogUtil.e("HttpUtil",e.getMessage());

                    //提示用户网络异常
//                    Toast.makeText(MyApplication.getContext(),"连接网络失败",Toast.LENGTH_SHORT).show();


                    listener.OnFailed();
                }finally {

                    conn.disconnect();

                }


                listener.OnFailed();
            }
        }).start();


    }

}
