package util;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import db.City;
import db.County;
import db.Province;

/**
 * Created by MyPC on 2017/11/3.
 */

public class InitDB {

    private static Province province;
    private static City city;
    private static County county;
    private static JSONArray jsonArray;
    private static JSONObject json;

    //向数据库中添加省级的数据
    public static List<Province> intiProvince(InputStream in){

        List<Province> provinceList = new ArrayList<Province>();

        //读取存储的初始化状态

        try {
            String jsonData = inputStream2String(in);

            jsonArray = new JSONArray(jsonData);

            for (int i = 0;i<jsonArray.length();i++){

                province = new Province();

                json = jsonArray.getJSONObject(i);

                province.setProvinceCode(json.getInt("id"));

                province.setProvinceName(json.getString("name"));


                provinceList = DataSupport.where("provinceCode = ?",province.getProvinceCode()+"").find(Province.class);

                if (provinceList.size() == 0){

                    province.save();

                }
            }

            LogUtil.i("Province Size",provinceList.size()+"");

        } catch (Exception e) {

            LogUtil.e("InitDB",e.getMessage());
        }
        finally {

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }


        return provinceList;
    }


    //向数据库中添加市级的数据
    public static List<City> initCity(InputStream in ,int procinceCode){

        List<City> cityList = new ArrayList<City>();


        try {
            String jsonData = inputStream2String(in);

            jsonArray = new JSONArray(jsonData);

            for (int i= 0;i<jsonArray.length();i++){

                city = new City();

                json = jsonArray.getJSONObject(i);

                city.setCityCode(json.getInt("id"));

                city.setCityName(json.getString("name"));


                //这里添加省的代码是为了方便查询   查找同个省的市直接用provinceCode来约束
                city.setProvinceID(procinceCode);

                LogUtil.v("InitDB","add city");

                cityList = DataSupport.where("cityCode = ?",city.getCityCode()+"").find(City.class);

                LogUtil.v("InitDB","list size = " + cityList.size());

                if (cityList.size() == 0){

                    LogUtil.v("InitDB","save city");

                    city.save();

                }

            }

            LogUtil.i("City Size",cityList.size()+"");

        } catch (Exception e) {
           LogUtil.e("InitDB",e.getMessage());
        }

        finally {

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return cityList;
    }

    //向数据库中添加县级数据
    public static List<County> initCounty(InputStream in ,int cityCode){

        List<County> countyList = new ArrayList<County>();


        try{

            String jsonData = inputStream2String(in);

            jsonArray = new JSONArray(jsonData);

            for (int i= 0;i<jsonArray.length();i++){

                county = new County();

                json = jsonArray.getJSONObject(i);

                county.setCityID(cityCode);

                county.setCountyName(json.getString("name"));

                county.setWeather_id(json.getString("weather_id"));

                county.setCountyCode(json.getInt("id"));


                //判断数据库中是否已经添加过该数据
                countyList = DataSupport.where("countyCode = ?",county.getCountyCode()+"").find(County.class);

                if (countyList.size() == 0){

                    county.save();

                }


            }

            LogUtil.i("Countys Size",countyList.size()+"");

        }catch (Exception e){

            LogUtil.e("InitDB",e.getMessage());

        }

        finally {

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return countyList;

    }


    //将流转化为字符串数组
    public static String inputStream2String(InputStream in)throws IOException {

        BufferedReader bufr = new BufferedReader(new InputStreamReader(in));

        StringBuilder sb = new StringBuilder();

        String temp;

        while((temp = bufr.readLine())!=null){

            sb.append(temp);

            LogUtil.v("InitDb","temp = "+temp);

        }

        return sb.toString();

    }

}
