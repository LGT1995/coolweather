package util;

import java.io.InputStream;

/**
 * Created by MyPC on 2017/11/3.
 */

public interface HttpConnectionListener {

    public void OnFailed();

    public void OnSucceed(InputStream in);

}
