package android.coolweather.mypc.coolweather;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import db.County;
import util.LogUtil;

/**
 * Created by MyPC on 2017/11/4.
 */

public class CityActivity extends AppCompatActivity {

    private String countyName;
    private ListView lv;
    private String[] names;
    private ArrayAdapter<String> adapter;
    private SharedPreferences sp;
    private SharedPreferences.Editor edit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        //读取用户选择过的城市
        Intent launcherIntent = getIntent();

        sp = getSharedPreferences("save",0);

        edit = sp.edit();

        lv = (ListView)findViewById(R.id.city_lv);

        countyName = getSaveName();


        LogUtil.v("CityActivity","countyName is " + countyName);

        //用户选择城市后 保存到手机中 并重新读取用户选择的城市
        if (launcherIntent.getStringExtra("countyName")!=null){

            saveName(launcherIntent.getStringExtra("countyName"));

            countyName = getSaveName();

        }

        //如果用户选择的城市不为空 时 将用户选择过的城市 展示到 ListView上
        if (countyName != null){

            names = countyName.split(",");
            adapter = new ArrayAdapter<String>(CityActivity.this,R.layout.support_simple_spinner_dropdown_item,names);

            lv.setAdapter(adapter);

            LogUtil.v("chooseIndex","ssssssssssssssssssssss_++_+_ssss");

            //设置点击事件 将用户点击选择的城市的信息 传递到 显示天气的activity中
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    //获取用户点击的下标
                    int chooseIndex = adapterView.getAdapter().getCount();

                    LogUtil.v("chooseIndex","ssssssssssssssssssssssssss");

                    String chooseName = names[i];

                    List<County> list = DataSupport.where("countyName = ?",chooseName).find(County.class);

                    if (list.size() != 0){

                        County county = list.get(0);

                        String weather_id = county.getWeather_id();

                        Intent in = new Intent(CityActivity.this,MainActivity.class);

                        LogUtil.v("658",chooseName+weather_id);

                        in.putExtra("weather_id",weather_id);
                        in.putExtra("name",chooseName);
                        in.putExtra("isLocation",false);
                        in.putExtra("isSave",false);

                        startActivity(in);

                        finish();

                    }


                }
            });

        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.city_toolbar);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        //设置返回的按钮和图标
        if (actionBar != null){

            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }
    }


    //从存储文件中 获取 用户选择的城市
    private String getSaveName() {

        String name = sp.getString("name",null);

        return name;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.city_toolbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){


            //设置添加城市按钮的点击事件
            case R.id.add:

                Intent in = new Intent(CityActivity.this,ChooseActivity.class);

                in.putExtra("location","中国");

                in.putExtra("level",1);

                startActivity(in);

                finish();

                break;


            //设置返回按钮的点击事件
            case android.R.id.home:

                finish();

                break;
        }

        return true;

    }

    //保存用户选择的城市信息
    private void saveName(String result) {


        //保存的方式 是 将字符串组成一个字符串 中间 用 逗号分隔 便于取的时候转换为 数组
        if (countyName == null){

            countyName = result;

        }else{

            //看是否已经添加过当前所选的城市 已添加则不再添加
            if (countyName.contains(result)){

                return;

            }
            countyName = countyName+","+result;

        }

        edit.putString("name",countyName);

        edit.commit();

    }
}
