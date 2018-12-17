package com.speedata.libid2;

import android.content.Context;

import java.io.IOException;

/**
 * @author xuyan  ID2读非接卡接口
 */
public interface IHFService {

    /**
     *
     * @param context    入参context
     * @param callBack   用于返回结果
     * @return           是否初始化成功
     * @throws IOException
     */
    boolean initDev(Context context, IDReadCallBack callBack) throws IOException;

    void releaseDev();

    byte[] readSamID();

    int nCSearch();

    int execfasongCmd(String str);

    int random();

    String getResult();

    String getSafeResult();
}
