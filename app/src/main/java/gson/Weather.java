package gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by MyPC on 2017/11/6.
 */

public class Weather {

    public Basic basic;

    public Update update;

    public String status;

    public Now now;

    @SerializedName("lifestyle")
    public List<LifeStyle> lifeStyleList;

    @SerializedName("daily_forecast")
    public List<Foreast> foreastList;

    @Override
    public String toString() {

        return now.tmp;

    }
}
