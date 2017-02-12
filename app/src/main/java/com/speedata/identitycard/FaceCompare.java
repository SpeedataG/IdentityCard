package com.speedata.identitycard;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.megvii.licencemanage.sdk.LicenseManager;
import com.speedata.face.FaceUtil;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by brxu on 2017/1/5.
 */

public abstract class FaceCompare {
    private byte[] photo1;
    private byte[] photo2;
    private Context mContext;

    public FaceCompare(Context mContext) {
        this.mContext = mContext;
        licenseManager = new LicenseManager(mContext);
//        network();
    }

    private LicenseManager licenseManager;

    public void compare(File photo1, File photo2) {
//        String uuid = ConUtil.getUUIDString(mContext);
//        long[] apiName = {Facepp.getApiName()};
//        String content = licenseManager.getContent(uuid, LicenseManager.DURATION_30DAYS, apiName);

        AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
        String url = "https://api-cn.faceplusplus.com/facepp/v3/compare";
        RequestParams params = new RequestParams();
        params.put("api_key", FaceUtil.API_KEY);
        params.put("api_secret", FaceUtil.API_SECRET);
        try {
            params.put("image_file1", photo1);
            params.put("image_file2", photo2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        Log.w("ceshi", "content:" + content);
        mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {
                String successStr = new String(responseByte);
                try {
                    JSONObject jsonObject = new JSONObject(successStr);
                    String fazhi = jsonObject.getString("confidence");
                    if (fazhi != null) {
                        if (Double.parseDouble(fazhi) > 65.3) {
                            FaceCompare(true);
                            showMsg("确认本人，对比相似度" + fazhi);
                        } else {
                            FaceCompare(false);
                            showMsg("非本人，对比相似度" + fazhi);
                        }

                    } else {
                        FaceCompare(false);
                        showMsg("照片识别错误");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                authState(true);
//                showMsg(successStr);
                Log.w("ceshi", "onSuccess" + successStr);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                                  Throwable error) {
                error.printStackTrace();
                authState(false);
            }
        });
    }

    //    private void network() {
//        showMsg("正在联网授权中...");
//        licenseManager.setAuthTime(Facepp.getApiExpication(mContext) * 1000);
//        // licenseManager.setAgainRequestTime(againRequestTime);
//
//        String uuid = ConUtil.getUUIDString(mContext);
//        long[] apiName = { Facepp.getApiName() };
//        String content = licenseManager.getContent(uuid, LicenseManager.DURATION_30DAYS, apiName);
//
//        String errorStr = licenseManager.getLastError();
//        Log.w("ceshi", "getContent++++errorStr===" + errorStr);
//
//        boolean isAuthSuccess = licenseManager.authTime();
//        Log.w("ceshi", "isAuthSuccess===" + isAuthSuccess);
//        if (isAuthSuccess) {
//            authState(true);
//        } else {
//            AsyncHttpClient mAsyncHttpclient = new AsyncHttpClient();
//            String url = "https://api.megvii.com/megviicloud/v1/sdk/auth";
//            RequestParams params = new RequestParams();
//            params.put("api_key", FaceUtil.API_KEY);
//            params.put("api_secret", FaceUtil.API_SECRET);
//            params.put("auth_msg", content);
//            Log.w("ceshi", "content:" + content);
//            mAsyncHttpclient.post(url, params, new AsyncHttpResponseHandler() {
//                @Override
//                public void onSuccess(int statusCode, Header[] headers, byte[] responseByte) {
//                    String successStr = new String(responseByte);
//                    boolean isSuccess = licenseManager.setLicense(successStr);
//                    if (isSuccess)
//                        authState(true);
//                    else
//                        authState(false);
//
//                    String errorStr = licenseManager.getLastError();
//                    Log.w("ceshi", "setLicense++++errorStr===" + errorStr);
//                }
//
//                @Override
//                public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
// Throwable error) {error.printStackTrace();
//                    authState(false);
//                }
//            });
//        }
//
//    }
    private void authState(boolean isSuccess) {

        if (isSuccess) {
            showMsg("授权成功");// + Facepp.getVersion() + " ; " + ConUtil.getFormatterDate(Facepp
//                    .getApiExpication(mContext) * 1000));
        } else {
            showMsg("授权失败");
        }
    }


    public abstract void FaceCompare(boolean result);

    public abstract void showMsg(String msg);
}
