package gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MyPC on 2017/11/6.
 */

public class Now {

    //体感温度
    @SerializedName("fl")
    public String sendibleTmp;

    //温度
    public String tmp;

    //天气状况描述
    @SerializedName("cond_txt")
    public String condContext;

    //风向
    @SerializedName("wind_dir")
    public String windDirection;

    //风力
    @SerializedName("wind_sc")
    public String windPower;

    //风速
    @SerializedName("wind_spd")
    public String windSpd;

    //能见度
    public String vis;

}
