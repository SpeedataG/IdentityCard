package cn.ywho.api.decode;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

//
//The format of this fle is UTF-8.
//

/**
 * 身份证图片解码
 * @author xqs
 */
public class DecodeWlt
{
	/**
	 *
	 * @param wlt wlt文件数据,1024字节
	 * @param bmp 解析的RGB数据，102*126*3字节，可根据需求生成BMP或者JPG，图像数据BGR格式，需要将B、R值互换。
	 * @param bmpSave 709: bmp file save,
	 *                708: bmp file doesn't save（在内存读写）
	 * @return
	 * <li><b>1</b> 正确</li>
	 * <li><b>0</b> bmpSave参数错误</li>
	 */
	private static native int wlt2bmp(byte[] wlt, byte[] bmp, int bmpSave);

	//加载照片解码库
	static
	{
		try
		{
			System.loadLibrary("DecodeWlt");
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	//身份证图片解码
	public static int hxgc_Wlt2Bmp(byte[] wlt, byte[] bmp, int bmpSave)
	{
		return wlt2bmp(wlt, bmp, bmpSave);
	}

	/**
	 * 身份证图片解码
	 * @param wlt
	 * @return Bitmap 对象
	 */
	public static Bitmap hxgc_Wlt2Bmp2(byte[] wlt)
	{
		if(wlt == null)
		{
			return null;
		}
		if(wlt.length < 1024){
			return null;
		}

		//saveWlt(wlt);
		/* 14: bitmap file header; 40: bitmap info header */
		byte[] bmp = new byte[14 + 40 + 308 * 126]; //308 = 102*3+2 四个字节对齐,不足要补0
		int ret = wlt2bmp(wlt, bmp, 708);
		if(ret == 1)
		{
			Bitmap bitmap = BitmapFactory.decodeByteArray(bmp, 0, bmp.length);
			//savePhoto(bitmap);
			return bitmap;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 保存rgb图像
	 * @param wlt
	 */
	private static void saveWlt(byte[] wlt)
	{
		String fileName=getSDPath()+"/photo.wlt";
		File f = new File(fileName);
		if (f.exists())
		{
			f.delete();
		}
		try
		{
			DataOutputStream out=new DataOutputStream(
					new BufferedOutputStream(
							new FileOutputStream(fileName)));
			out.write(wlt);
			out.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 保存图片
	 * @param bitmap
	 */
	private static void savePhoto(Bitmap bitmap)
	{
		String fileName=getSDPath()+"/photo.bmp";
		File f = new File(fileName);
		if (f.exists())
		{
			f.delete();
		}

		try
		{
			f.createNewFile();
			FileOutputStream out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.flush();
			out.close();
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 获取SD路径
	 * @return sd卡路径
	 */
	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // determine whether sd												// card is exist
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// get the root directory
		}else{
			sdDir = Environment.getRootDirectory();
		}
		return sdDir.getAbsolutePath();
	}
}
