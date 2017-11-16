package android.coolweather.mypc.coolweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;
import org.litepal.crud.DataSupport;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import db.City;
import db.County;
import db.Province;
import gson.Foreast;
import gson.LifeStyle;
import gson.Weather;
import util.HttpConnectionListener;
import util.HttpUtil;
import util.InitDB;
import util.LogUtil;

public class MainActivity extends AppCompatActivity{

    //记录是否需要获取定位
    private boolean isLocation = true;

    private String lbsWeatherID;

    //判断是否需要从网络读取数据
    private boolean isSave = false;

    //记录读取返回的数据
    private String jsonData;

    private String impPath = "http://guolin.tech/api/bing_pic";

    private String path = "http://guolin.tech/api/china";

    //和风天气认证的key
    private String key = "key=ebfc2600efa54e8cbadcfe430fa96846";

    //和风天气获取天气信息的网址
    private String HFpath = "https://free-api.heweather.com/s6/weather?location=";

    private CollapsingToolbarLayout coll;


    //活动中的组件
    private SwipeRefreshLayout swipe;
    private Toolbar toolbar;
    private TextView cityText;//当前城市
    private TextView tempText;//包含当前温度
    private TextView condText;//包含天气描述 以及 空气pm2.5
    private ListView lv_forecast;//未来几天的天气信息
    private ListView lv_lifeStyle;//生活建议
    private TextView forecastTemp;
    private TextView forecastDate;
    private TextView forecastTxt;
    private TextView lifestyleType;
    private TextView lifestyleBrf;
    private TextView lifestyleContext;
    private TextView updateText;//更新时间
    private ImageView iv_bing;
    //用于区分是哪个集合
    private static final int LIFE_STYLE = 1;
    private static final int FORECAST = 2;
    private List<String> lbsPremissionList;

    private MyAdapter lifeStyleAdapter;
    private MyAdapter forecastAdapter;

    //记录用户定位位置所在的省 市
    private String cityProvince;
    private String cityPath;

    private SharedPreferences sp;
    private SharedPreferences.Editor edit;

    //初始化百度地图 locationClient
    private LocationClient mlocation;
    private MyLocationListener myLocationListener = new MyLocationListener();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //getUserPremission();
        super.onCreate(savedInstanceState);
        //设置statusBar的颜色透明
        if (Build.VERSION.SDK_INT >= 21){

            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);

        }
        setContentView(R.layout.activity_main);
        getUserPremission();
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coll = (CollapsingToolbarLayout)findViewById(R.id.collapsing) ;
        sp = getSharedPreferences("main",0);
        edit = sp.edit();
        //找到各个组件
        findView();
        String weather_id = null;
        Intent in = getIntent();
        LogUtil.v("MainActivity",in.toString());
        //是否需要定位 如果不用定位代表 是从CityActivity中传回来的信息 也表示不用读取手机中的天气信息
        isSave = sp.getBoolean("isSave",false);
        LogUtil.v("isSave",isSave+"sp");
        isLocation = in.getBooleanExtra("isLocation",true);
        lbsWeatherID = sp.getString("weather_id",null);
        if (!isLocation){
            isSave = false;
            cityPath = in.getStringExtra("name");
            weather_id = in.getStringExtra("weather_id");
            lbsWeatherID = weather_id;
            coll.setTitle(cityPath);
            getTempFromGson(weather_id);
            LogUtil.v("MainActivity",isLocation+cityPath+weather_id);

        }
        LogUtil.v("isSave",isSave+"");
        if (isSave){

            String jsonData = sp.getString("jsonData",null);
            String countyName = sp.getString("countyName",null);
            LogUtil.v("isSave",countyName+"");
            coll.setTitle(countyName);
            isLocation = false;
            //根据手机存储的地名 获取 对应的WeatherID
            lbsWeatherID = getSaveWeatherID(countyName);

            //如果手机没有保存信息 则lsbWeatherID 返回null 此时则获取定位信息
            if (lbsWeatherID == null){

                getUserPoint();
                isLocation = false;

            }

            if (jsonData != null){

                try {
                    Weather weather = paserJson(jsonData);

                    LogUtil.v("paserJsonData",jsonData);

                    showContext(weather);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{

                LogUtil.v("paserJsonData","jsonData is null");
                getUserPoint();
                isLocation = false;

            }


        }

        if (isLocation){

            getUserPoint();

        }



        //获取必应每日一图
        String bing_img = sp.getString("bing",null);

        if (bing_img != null){

            Glide.with(MainActivity.this).load(bing_img).into(iv_bing);
            LogUtil.v("bing---",bing_img);

        }else{

            loadBing();
        }


    }

    //根据手机存储的地名 获取对应的WeatherID
    private String getSaveWeatherID(String countyName) {


        if (countyName == null){

            return null;

        }
        List<County> list = DataSupport.where("countyName = ?",countyName).find(County.class);

        if (list.size()>0){

            String weather_id = list.get(0).getWeather_id();

            return weather_id;
        }else{

            return null;

        }


    }

    //从本地读取天气数据
    private void getTmpData() {

        try {
            String localJsonData = sp.getString("jsonData",null);
            cityPath = sp.getString("countyName",null);

            coll.setTitle(cityPath);

            Weather weather = paserJson(localJsonData);

            showContext(weather);

            loadBing();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //获取必应每日一图
    private void loadBing() {

        HttpUtil util = new HttpUtil();
        util.getInput(impPath);
        util.setListener(new HttpConnectionListener() {
            @Override
            public void OnFailed() {

            }

            @Override
            public void OnSucceed(InputStream in) {

                try {
                    BufferedReader bufr = new BufferedReader(new InputStreamReader(in));

                    StringBuilder sb = new StringBuilder();

                    String temp;

                    while((temp = bufr.readLine())!=null){

                        sb.append(temp);

                        LogUtil.v("InitDb","temp = "+temp);

                    }

                    final String bing = sb.toString();

                    edit.putString("bing",bing);
                    edit.commit();

                    LogUtil.v("bing",bing);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MainActivity.this).load(bing).into(iv_bing);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

    }

    private void findView() {
//        swipe = (SwipeRefreshLayout)findViewById(R.id.swipe);
        updateText = (TextView)findViewById(R.id.mytextview_update);
        iv_bing = (ImageView)findViewById(R.id.bing);
        cityText = (TextView)findViewById(R.id.mytextview_city);
        tempText = (TextView)findViewById(R.id.mytextview_tmp);
        condText = (TextView)findViewById(R.id.mytextview_cond_txt);
        lv_forecast = (ListView)findViewById(R.id.lv_forecast);
        lv_lifeStyle = (ListView)findViewById(R.id.lv_lifestyle);
        forecastDate = (TextView)findViewById(R.id.forecast_date);
        forecastTemp = (TextView)findViewById(R.id.forecast_temp);
        forecastTxt = (TextView)findViewById(R.id.forecast_txt);
        lifestyleBrf = (TextView)findViewById(R.id.lifestyle_brf);
        lifestyleContext = (TextView)findViewById(R.id.lifestyle_context);
        lifestyleType = (TextView)findViewById(R.id.lifestyle_type);

    }

    //activity 获取用户的位置 并从网络获取该地区的天气情况 使用百度定位SDK
    private void getUserPoint() {

        LogUtil.v("point","userpoint");
        //获取定位所需要的权限
        //getUserPremission();
        //初始化百度定位
        mlocation = new LocationClient(getApplicationContext());
        mlocation.registerLocationListener(myLocationListener);
        //设置定位参数 设置成需要获得位置信息
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);
        mlocation.setLocOption(option);
        mlocation.start();
    }

    //获取定位所需要的权限
    private void getUserPremission() {

        lbsPremissionList = new ArrayList<String>();

        //判断用户是否授权过操作sd卡 如果没有授权 则向lbsPremissionList 中添加一条数据

        LogUtil.v("premission11111",ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)+"");
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){

            lbsPremissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            LogUtil.v("premission",lbsPremissionList.size()+"checking");

        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED){

            lbsPremissionList.add(Manifest.permission.READ_PHONE_STATE);

        }

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED){

            lbsPremissionList.add(Manifest.permission.INTERNET);

        }

        //这里判断lbsPremissionList  中是否有数据 有则申请权限 否则调用定位的函数
        if (lbsPremissionList.size() > 0){

            LogUtil.v("premission",lbsPremissionList.size()+"456");

            String[] premissions = lbsPremissionList.toArray(new String[lbsPremissionList.size()]);

            ActivityCompat.requestPermissions(this,premissions,1);

        }else{

            LogUtil.v("premission",lbsPremissionList.size()+"");

            return;

        }

    }


    //获取权限回调方法
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        LogUtil.v("premission","jhjhjhjhjhj===================================================================");

        switch(requestCode){

            case 1:
                if (grantResults.length > 0){

                    //用for-each遍历grantResults 判断用户是否对每个权限都进行授权
                    for (int result: grantResults) {

                        if (result!=PackageManager.PERMISSION_GRANTED){

                            LogUtil.v("premission",result+"");
                            Toast.makeText(MainActivity.this,"获取用户位置信息失败，用户未授权",Toast.LENGTH_SHORT).show();
                            return;

                        }else{

                            LogUtil.v("premission",result+"");

                        }

                    }


                }else{

                    Toast.makeText(MainActivity.this,"发生未知错误",Toast.LENGTH_SHORT).show();

                }
                break;

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar,menu);

        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            case R.id.city_manager:

                Intent in = new Intent(this,CityActivity.class);

                startActivity(in);

                break;
        }

        return true;


    }

    class MyLocationListener implements BDLocationListener{

        //获取用户位置
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            //获取用户所在城市
            cityPath = bdLocation.getCity();

            //获取用户所在省份 如果用户所在地区不能从数据库中获得weatherID 则从网络上加载
            cityProvince = bdLocation.getProvince();

            if (cityProvince.contains("省")||cityProvince.contains("市")){

                cityProvince = cityProvince.substring(0,cityProvince.length()-1);

            }

            if (cityPath.contains("市")){

                cityPath = cityPath.substring(0,cityPath.length()-1);

            }

            coll.setTitle(cityPath);

            //获取用户所在城市WeatherID
            getUserWeatherID();

        }
    }

    //获取用户所在地的weatherID
    private void getUserWeatherID() {

        //首先从数据库中加载
        List<County> countyList = DataSupport.where("countyName = ?",cityPath).find(County.class);

        LogUtil.v("MainActivitys","countyList size is "+countyList.size());

        //判断数据库中是否有该市的数据 有数据则获取该城市的WeatherID
        if (countyList.size() != 0){

            County lbsCounty = countyList.get(0);

            lbsWeatherID = lbsCounty.getWeather_id();

            LogUtil.v("MainActivity","lbsWeatherID is"+lbsWeatherID);

            getTempFromGson(lbsWeatherID);

        }else{

            //从网络获取用户所在地的WeatherID
            getUserWeatherIDFromWeb();

        }

    }

    //从网络获取当前城市的weatherID
    private void getUserWeatherIDFromWeb() {

        HttpUtil util = new HttpUtil();

        util.getInput(path);

        LogUtil.v("MainActivity","path is "+path);

        util.setListener(new HttpConnectionListener() {
            @Override
            public void OnFailed() {

            }

            @Override
            public void OnSucceed(InputStream in) {

                final int provinceID;

                //向数据库中添加数据 并获取对应的路径
                try {
                    InitDB.intiProvince(in);

                    List<Province> provinceList = DataSupport.where("provinceName = ?",cityProvince).find(Province.class);
                    LogUtil.v("MainActivity"," cityProvince is "+cityProvince);
                    provinceID = provinceList.get(0).getProvinceCode();

                    path = path + "/" + provinceID;

                    LogUtil.v("MainActivity","path is "+path);
                } finally {

                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                LogUtil.v("MainActivity","path is "+path);

                HttpUtil util = new HttpUtil();

                util.getInput(path);

                util.setListener(new HttpConnectionListener() {
                    @Override
                    public void OnFailed() {

                    }

                    @Override
                    public void OnSucceed(InputStream in) {

                        final int cityID;

                        //向数据库中添加数据 并获取对应的路径
                        try {
                            InitDB.initCity(in,provinceID);

                            List<City> cityList = DataSupport.where("cityName = ?",cityPath).find(City.class);

                            LogUtil.v("MainActivity","cityList size is "+cityList.size());

                            cityID = cityList.get(0).getCityCode();

                            LogUtil.v("MainActivity","cityID"+cityID);

                            path = path + "/" + cityID;

                            LogUtil.v("MainActivity","Citypath is "+path);
                        } finally {

                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }

                        LogUtil.v("MainActivity","path is "+path);

                        HttpUtil util = new HttpUtil();

                        util.getInput(path);

                        util.setListener(new HttpConnectionListener() {
                            @Override
                            public void OnFailed() {

                            }

                            @Override
                            public void OnSucceed(InputStream in) {

                                //向数据库中添加数据 并获取对应的路径
                                try {
                                    InitDB.initCounty(in,cityID);

                                    List<County> countyList = DataSupport.where("countyName = ?",cityPath).find(County.class);

                                    lbsWeatherID = countyList.get(0).getWeather_id();

                                    String countyName = countyList.get(0).getCountyName();

                                    LogUtil.v("MainActivity","countyName is "+countyName);

                                    getTempFromGson(lbsWeatherID);


                                } finally {
                                    try {
                                        in.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                    }
                });

            }
        });



    }

    //获取天气信息
    private void getTempFromGson(String lbsWeatherID) {

        //TODO 获取用户所在地的天气信息

        String getJsonPath = HFpath+lbsWeatherID+"&"+key;

        HttpUtil util = new HttpUtil();

        util.getInput(getJsonPath);

        util.setListener(new HttpConnectionListener() {
            @Override
            public void OnFailed() {

            }

            @Override
            public void OnSucceed(InputStream in) {

                try {
                    //将获取到的数据输入流转换为字符串
                    jsonData = InitDB.inputStream2String(in);

                    LogUtil.v("MainActivity111",jsonData);

                    //使用JSON将天气数据中的主体内容解析出来
                    Weather weather = paserJson(jsonData);

                    //将获取到的数据显示到对应的控件中
                    showContext(weather);

                    loadBing();

                    LogUtil.v("MainActivity",jsonData);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
        });

    }

    @NonNull
    private Weather paserJson(String jsonData) throws JSONException {
        JSONObject json = new JSONObject(jsonData);

        isSave = true;

        JSONArray jsonArray = json.getJSONArray("HeWeather6");

        String gsonData = jsonArray.get(0).toString();

        LogUtil.v("MainActivity",gsonData);

        Gson gson = new Gson();

        Weather weather = gson.fromJson(gsonData,Weather.class);

        LogUtil.v("MainActivity",weather.toString());
        return weather;
    }

    private void showContext(Weather weather) {

        if (weather.status.equals("ok")){

            final String temp = weather.now.tmp;

            LogUtil.v("MainActivity","tmp is "+temp);

            final String fl = weather.now.sendibleTmp;

            final String vis = weather.now.vis;

            final String condContext = weather.now.condContext;

            final String windDir = weather.now.windDirection;

            final String windPwoer = weather.now.windSpd;

            final List<Foreast> foreasts = weather.foreastList;


            final List<LifeStyle> lifeStyles = weather.lifeStyleList;
            LogUtil.v("MainActivity","forecast size is"+lifeStyles.size());

            final String update = weather.update.locTime;

            forecastAdapter = new MyAdapter(foreasts,FORECAST);

            lifeStyleAdapter = new MyAdapter(lifeStyles,LIFE_STYLE);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tempText.setText(temp+"°");

                    cityText.setText("体感温度："+fl+" | "+"能见度："+vis+"(km)");
                    condText.setText(condContext+" | "+windDir+" 风速："+windPwoer+"(km/h)");
                    updateText.setText(update+"asdasdasd");
                    lv_lifeStyle.setNestedScrollingEnabled(false);
                    lv_forecast.setAdapter(forecastAdapter);
                    lv_lifeStyle.setAdapter(lifeStyleAdapter);

                }
            });

        }






    }

    private class MyAdapter extends BaseAdapter{

        private List<?> list;
        private int which;

        MyAdapter(List<?> list,int which){

            this.which = which;

            this.list = list;

        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            View v = null;

            switch (which){

                //区分传入的集合 然后设置显示数据
                case FORECAST:

                    v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.forecastlist_layout,null);

                    forecastDate = (TextView)v.findViewById(R.id.forecast_date);
                    forecastTemp = (TextView)v.findViewById(R.id.forecast_temp);
                    forecastTxt = (TextView)v.findViewById(R.id.forecast_txt);

                    Foreast foreast = (Foreast) list.get(i);

                    String date = foreast.date;
                    String temp = foreast.tmp_max+" | "+foreast.tmp_min;
                    String txt = foreast.dayContext;

                    LogUtil.v("MainActivity","txt is "+txt);

                    forecastDate.setText(date);
                    forecastTemp.setText(temp);
                    forecastTxt.setText(txt);

                    break;

                case LIFE_STYLE:

                    v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.lifestyle_layout,null);

                    lifestyleBrf = (TextView)v.findViewById(R.id.lifestyle_brf);
                    lifestyleContext = (TextView)v.findViewById(R.id.lifestyle_context);
                    lifestyleType = (TextView)v.findViewById(R.id.lifestyle_type);

                    LifeStyle life = (LifeStyle)list.get(i);

                    String type = life.type;
                    String context = life.txt;
                    String brf = life.intro;

                    //从网络获取的type是 英文简写 将英文简写转化为中文
                    type = typePaser(type);

                    lifestyleType.setText(type);
                    lifestyleContext.setText(context);
                    lifestyleBrf.setText(brf);

                    break;

            }

            return v;
        }
    }

    //将获取到的type 进行 转化
    private String typePaser(String type) {

        String paser = null;

        if ("comf".equals(type)){

            paser = "舒适度指数";

        }else if ("cw".equals(type)){

            paser = "洗车指数";

        }else if ("drsg".equals(type)){

            paser = "穿衣指数";

        }else if ("flu".equals(type)){

            paser = "感冒指数";

        }else if ("sport".equals(type)){

            paser = "运动指数";

        }else if ("trav".equals(type)){

            paser = "旅游指数";

        }else if ("uv".equals(type)){

            paser = "紫外线指数";

        }else if ("air".equals(type)){

            paser = "空气指数";

        }

        return paser;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        edit.putBoolean("isSave",isSave);
        edit.commit();
        LogUtil.v("isSave",isSave+"put");
    }

    @Override
    protected void onPause() {
        super.onPause();
        edit.putString("jsonData",jsonData);
        edit.putString("countyName",cityPath);
        edit.putString("weather_id",lbsWeatherID);
       // LogUtil.v("Weather_id",lbsWeatherID);
        edit.commit();
    }
}
