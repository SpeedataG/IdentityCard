package com.speedata.libid2;

import android.content.Context;

import java.io.IOException;

/**
 * @author xuyan
 */
public interface IHFService {

    boolean initDev(Context context, IDReadCallBack callBack) throws IOException;

    void releaseDev() throws IOException;

    byte[] readSamID();

    int nCSearch();

    int execfasongCmd(String str);

    int random();

    String getResult();

    String getSafeResult();
}
