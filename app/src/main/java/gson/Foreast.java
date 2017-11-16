package gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MyPC on 2017/11/6.
 */

public class Foreast {

    //预报时间
    public String date;

    //最高温度
    public String tmp_max;

    //最低温度
    public String tmp_min;

    //白天天气描述
    @SerializedName("cond_txt_d")
    public String dayContext;

}
