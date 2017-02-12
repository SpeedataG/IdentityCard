package com.speedata.identitycard.utils;

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtils {
	private static ProgressDialog mProgressDialog;

	/**
	 * 显示ProgressDialog
	 * @param context
	 * @param message
	 */
	public static void showProgressDialog(Context context, CharSequence message){
		if(mProgressDialog == null){
			mProgressDialog = ProgressDialog.show(context, "", message);
		}else{
			mProgressDialog.setMessage(message);
			mProgressDialog.show();
		}
	}

	/**
	 * 关闭ProgressDialog
	 * 这里出现的崩溃应该是因为，Activity已经销毁，
	 * 之后才执行mProgressDialog.dismiss();
	 * 应该判断context的Activity是否已经销毁
	 */
	public static void dismissProgressDialog(){
		if(mProgressDialog != null&&mProgressDialog.isShowing()){
			mProgressDialog.dismiss();
			mProgressDialog=null;
		}
	}
}
