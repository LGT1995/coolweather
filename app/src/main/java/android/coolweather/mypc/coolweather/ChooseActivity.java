package android.coolweather.mypc.coolweather;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import db.City;
import db.County;
import db.Province;
import util.ActivityUtil;
import util.BaseActivity;
import util.HttpConnectionListener;
import util.HttpUtil;
import util.InitDB;
import util.LogUtil;

/**
 * Created by MyPC on 2017/11/4.
 */

//继承BaseActivity 当用户最后选择完成后 将所有选择城市的页面关闭
public class ChooseActivity extends BaseActivity {

    private ListView lv;
    private ArrayAdapter<String> adapter;
    private List<String> list;
    private List<? extends DataSupport> DBList;

    //设置地址
    private static String provincePath = "http://guolin.tech/api/china";

    //保存传入的地址
    private String savePath;

    //用于区别当前获取的是哪个级别的数据
    private final int PROVINCE = 1;
    private final int CITY = 2;
    private final int COUNTY = 3;
    private int level;

    private Intent launcherIntent;

    //
    private int provinceCode;
    private int cityCode;

    private String location;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);

        Toolbar toolbar = (Toolbar)findViewById(R.id.choose_toolbar);
        TextView toolbarTitle = (TextView)findViewById(R.id.choose_toolbar_title);
        lv = (ListView)findViewById(R.id.lv);

        LitePal.getDatabase();

        //获取上一个页面传递过来的省 市信息
        launcherIntent = getIntent();
        location = launcherIntent.getStringExtra("location");

        toolbarTitle.setText(location);

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null){

            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
            actionBar.setDisplayHomeAsUpEnabled(true);

        }

        level = launcherIntent.getIntExtra("level",PROVINCE);

        LogUtil.v("ChooseActivity","level is " + level);

        //判断等级 进行转换
        if (level == CITY){

            provinceCode = launcherIntent.getIntExtra("id",0);
            provincePath = launcherIntent.getStringExtra("path")+"/"+provinceCode;

        }else if (level == COUNTY){

            cityCode = launcherIntent.getIntExtra("id",0);
            provincePath = launcherIntent.getStringExtra("path")+"/"+cityCode;

        }

        savePath = provincePath;

        LogUtil.v("ChooseActivity","provincePath is "+provincePath);

        initStringList();
    }

    private void initAdapter() {
        adapter = new ArrayAdapter<String>(ChooseActivity.this, R.layout.support_simple_spinner_dropdown_item,list.toArray(new String[list.size()]));

        LogUtil.i("ChooseActivity","list  ===== "+list.size()+"");

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lv.setAdapter(adapter);
            }
        });

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Object object = DBList.get(i);

                String name = list.get(i);

                int code = 0;

                //判断等级 进行转换
                if (level == PROVINCE){

                    code = ((Province)object).getProvinceCode();

                }else if (level == CITY){

                    code = ((City)object).getCityCode();

                    //当用户选择到 county级别的时候 讲述度读取出来 并将所有ChooseActivity 页面关闭
                }else if (level == COUNTY){

                    Intent in = new Intent(ChooseActivity.this,CityActivity.class);

                    String countyName = list.get(i);

                    in.putExtra("countyName",countyName);

                    provincePath = "http://guolin.tech/api/china";

                    LogUtil.v("ChooseActivity","countyName is "+countyName);

                    startActivity(in);

                    ActivityUtil.rmoveAll();

                    return;


                }



                Intent in = new Intent(ChooseActivity.this,ChooseActivity.class);

                in.putExtra("location",name);

                //传入相应的代码
                in.putExtra("id",code);

                in.putExtra("level",level + 1);

                in.putExtra("path",provincePath);

                startActivity(in);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        provincePath = savePath;
    }

    private void initStringList() {

        list = new ArrayList<String>();

        //根据等级加载不同的数据库
        if (level == PROVINCE){
            DBList = DataSupport.findAll(Province.class);
        }else if (level == CITY){
            LogUtil.v("ChooseActivity","location is "+location);
            DBList = DataSupport.where("provinceID = ?",provinceCode+"").find(City.class);
        }else if (level == COUNTY){
            DBList = DataSupport.where("cityID = ?",cityCode+"").find(County.class);
        }


        //如果数据库中查询不到数据 就去网络上加载
        if(DBList.size() == 0){
            getDataFromJson();
        }else{

            for (Object object : DBList) {

                String name = null;

                LogUtil.v("ChooseActivity","for");

                //判断等级 进行转换
                if (level == PROVINCE){

                    name = ((Province)object).getProvinceName();

                }else if (level == CITY){

                    name = ((City)object).getCityName();

                }else if (level == COUNTY){

                    name = ((County)object).getCountyName();

                }

                list.add(name);

            }

            LogUtil.v("ChooseActivity","DBList"+DBList.size());

            initAdapter();

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case android.R.id.home:

                finish();

                break;


        }

        return true;

    }

    //获取全国省的数据并添加到数据库当中
    private void getDataFromJson() {

        HttpUtil util = new HttpUtil();

        LogUtil.v("ChooseActivity","path is "+provincePath);

        util.getInput(provincePath);

        LogUtil.v("ChooseActivity","getDataFromJson");

        util.setListener(new HttpConnectionListener() {
            @Override
            public void OnFailed() {

            }

            @Override
            public void OnSucceed(InputStream in) {

                Log.e("ChooseActivityConne","connection succeed");

                //判断当前的信息等级
                try {
                    if (level == PROVINCE){
                        InitDB.intiProvince(in);
                    }else if (level == CITY){
                        InitDB.initCity(in,provinceCode);
                    }else if (level == COUNTY){
                        InitDB.initCounty(in,cityCode);
                    }

                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //从网络上获取了数据以后再调用初始化Adapter的方法
                initStringList();
            }
        });

    }

}
