package com.speedata.identity_as.utils;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * @author xuyan  显示等待
 */
public class ProgressUtils {
    private static ProgressDialog mProgressDialog;

    /**
     * 显示ProgressDialog
     * @param context
     * @param message
     */
    public static void showProgressDialog(Context context, CharSequence message) {
        if (mProgressDialog == null) {
            mProgressDialog = ProgressDialog.show(context, "", message);
        } else {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    /**
     * 关闭ProgressDialog
     */
    public static void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
}
