package gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by MyPC on 2017/11/6.
 */

public class LifeStyle {

    //生活指数简介
    @SerializedName("brf")
    public String intro;

    //生活指数详细信息
    public String txt;

    //生活指数类型
    public String type;

}
