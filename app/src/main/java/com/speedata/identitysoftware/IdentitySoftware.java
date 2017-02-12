package com.speedata.identitysoftware;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ivsign.android.IDCReader.IDCReaderSDK;
import com.speedata.identitycard.utils.ProgressDialogUtils;
import com.zqd.idcard.ConsantHelper;
import com.zqd.idcard.Util;
import com.zqd.idcard.ZqdReadIdCard;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * Created by brxu on 2017/1/16.
 * 网络软解身份证信息
 */


public class IdentitySoftware {
    public ZqdReadIdCard zqdreadId;
    private Context mContext;

    private IdentitySoftware(Context mContext) {
        this.mContext = mContext;
        zqdreadId = new ZqdReadIdCard(mContext, myHandler);
    }

    private static IdentitySoftware identitySoftware;

    public static IdentitySoftware getIntance(Context mContext) {
        if (identitySoftware == null) {
            identitySoftware = new IdentitySoftware(mContext);
        }
        return identitySoftware;
    }

    private Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            ProgressDialogUtils.dismissProgressDialog();
            switch (msg.what) {
                case ConsantHelper.NFC_CARD_TYPE_ERR:
                    sendBroadcastData("不支持此卡片");
                    break;
                case ConsantHelper.NFC_CARD_ERR:
                    sendBroadcastData("NFC连接卡片失败、请重新放卡");
                    break;
                case ConsantHelper.NFC_CONNECT_ERR:
                    sendBroadcastData("NFC连接卡片失败、请重新放卡");
                    break;
                case ConsantHelper.NET_CONNECT_SERVER_ERR:
                    sendBroadcastData("连接服务器异常、请检查网络");
                    break;
                case ConsantHelper.NET_RECIVE_ERR:
                    //接收错误，再次开启读卡流程
                    sendBroadcastData("接收异常、请检查网络");
                    break;
                case ConsantHelper.Process_state:
                    sendBroadcastData("Process_state");
                    break;
                case ConsantHelper.Process_state_ok:
                    sendBroadcastData("Process_state_ok");
                    break;
                case ConsantHelper.GHCbt_inti_ERROR: //
                    Log.e("hjs", "初始化异常");
                    sendBroadcastData("初始化异常");
                    break;
                case ConsantHelper.Clinet_regist_failed: //
                    Log.e("hjs", "SDK验证失败");
                    sendBroadcastData("SDK验证失败");
                    break;
                case ConsantHelper.Clinet_regist_ok: //
                    Log.e("hjs", "SDK验证成功");
                    sendBroadcastData("SDK验证成功");
                    break;
                case ConsantHelper.Clinet_UnRegist: //
                    Log.e("hjs", "SDK验证客户端不存在");
                    sendBroadcastData("SDK验证客户端不存在");
                    break;
                case ConsantHelper.Clinet_State_Error: //
                    Log.e("hjs", "SDK验证无效状态");
                    sendBroadcastData("SDK验证无效状态");
                    break;
                case ConsantHelper.Clinet_Money_out: //
                    Log.e("hjs", "SDK验证欠费状态");
                    sendBroadcastData("SDK验证欠费状态");
                    break;
                case ConsantHelper.Clinet_Verfiy_Error: //
                    Log.e("hjs", "SDK验证认证失败");
                    sendBroadcastData("SDK验证认证失败");
                    break;
                case ConsantHelper.Clinet_SAM_Error: //
                    Log.e("hjs", "SDK验证资源获取失败");
                    sendBroadcastData("SDK验证资源获取失败");
                    break;
                case ConsantHelper.Socket_Slow:
                    Log.e("hjs", "网络太差、无法读证");
                    sendBroadcastData("网络太差、无法读证");
                case 99:
                    sendBroadcastData("99");
                    break;
                case ConsantHelper.READ_CARD_SUCCESS:
                    try {
                        byte[] bitresult = (byte[]) msg.obj;
//                        ParseIDInfor idInfor=new ParseIDInfor(mContext);
//                        String testid = handleID2(0, bitresult);
//                        Bitmap jizp = jizp(bitresult);
//                        if (jizp != null) {
                            sendBroadcastBytes(bitresult);
//                        }
//                        sendBroadcastData(testid);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    sendBroadcastData("未知" + msg.what);
                    break;
            }

        }
    };

    private void sendBroadcastData(String data) {
        Intent intent = new Intent();
        intent.setAction(COM_SPEEDATA_SOFTWARE_ID);
        intent.putExtra("id_data", data);
        mContext.sendBroadcast(intent);
    }

    public static String COM_SPEEDATA_SOFTWARE_ID = "com_speedata_software_id";

    private void sendBroadcastBytes(byte[] data) {
        Intent intent = new Intent();
        intent.setAction(COM_SPEEDATA_SOFTWARE_ID);
        intent.putExtra("byte_data", data);
        mContext.sendBroadcast(intent);
    }

    public  Bitmap bitmap;

    private final static String encoding = "unicode";
    private static String[] showinfo = {"姓名:", "性别:", "民族:", "出生日期:", "住址:", "身份证号:", "签发机关:",
            "起始日期:", "失效日期:"};

    /**
     * 解析身份证信息
     *
     * @param len
     * @param data
     * @return
     */
    public static String handleID2(int len, byte[] data) {
        StringBuffer sb = new StringBuffer();
//		if(((data[1]&0xff)==0xaa)||((data[2]&0xff)==0xaa)){
        if (true) {
            byte[] head = new byte[8];
            byte[] xm = new byte[30];
            byte[] xb = new byte[2];
            byte[] mz = new byte[4];
            byte[] cs = new byte[16];
            byte[] zz = new byte[70];
            byte[] id = new byte[36];
            byte[] qfzg = new byte[30];
            byte[] start = new byte[16];
            byte[] end = new byte[16];
            int pos = 10 - 4;
            System.arraycopy(data, pos, head, 0, head.length);
            pos += head.length;
            System.arraycopy(data, pos, xm, 0, xm.length);
            pos += xm.length;
            System.arraycopy(data, pos, xb, 0, xb.length);
            pos += xb.length;
            System.arraycopy(data, pos, mz, 0, mz.length);
            pos += mz.length;
            System.arraycopy(data, pos, cs, 0, cs.length);
            pos += cs.length;
            System.arraycopy(data, pos, zz, 0, zz.length);
            pos += zz.length;
            System.arraycopy(data, pos, id, 0, id.length);
            pos += id.length;
            System.arraycopy(data, pos, qfzg, 0, qfzg.length);
            pos += qfzg.length;
            System.arraycopy(data, pos, start, 0, start.length);
            pos += start.length;
            System.arraycopy(data, pos, end, 0, end.length);
            pos += end.length;

            xm = Util.bigtosmall(xm);
            xb = Util.bigtosmall(xb);
            mz = Util.bigtosmall(mz);
            cs = Util.bigtosmall(cs);
            zz = Util.bigtosmall(zz);
            id = Util.bigtosmall(id);
            qfzg = Util.bigtosmall(qfzg);
            start = Util.bigtosmall(start);
            end = Util.bigtosmall(end);

            try {
                sb.append(showinfo[0] + new String(xm, encoding) + "\n");
                sb.append(showinfo[1] + decodesex(new String(xb, encoding)) + "\n");
                sb.append(showinfo[2] + decodeNation(Integer.parseInt(new String(mz, encoding)))
                        + "\n");
                sb.append(showinfo[3] + new String(cs, encoding) + "\n");
                sb.append(showinfo[4] + new String(zz, encoding) + "\n");
                sb.append(showinfo[5] + new String(id, encoding) + "\n");
                sb.append(showinfo[6] + new String(qfzg, encoding) + "\n");
                sb.append(showinfo[7] + new String(start, encoding) + "\n");
                sb.append(showinfo[8] + new String(end, encoding) + "\n");
                System.out.print(sb.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }


        return sb.toString();
    }


    public static Bitmap jizp(byte[] to) {
        if (to == null) return null;
        System.arraycopy(to, 0, recData, 0, to.length);
        try {
            bit();
            if (Readflage == 1) {
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(Environment.getExternalStorageDirectory() +
                            "/wltlib/zp.bmp");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap bmp = BitmapFactory.decodeStream(fis);
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bmp;
            }
            return null;

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


    static int Readflage = -99;
    static byte[] recData = new byte[1500];
    static String[] decodeInfo = new String[10];

    private static void bit() throws UnsupportedEncodingException {

        if (recData[9] == -112) {

            byte[] dataBuf = new byte[256];
            for (int i = 0; i < 256; i++) {
                dataBuf[i] = recData[14 + i];
            }
            String TmpStr = new String(dataBuf, "UTF16-LE");
            TmpStr = new String(TmpStr.getBytes("UTF-8"));
            decodeInfo[0] = TmpStr.substring(0, 15);
            decodeInfo[1] = TmpStr.substring(15, 16);
            decodeInfo[2] = TmpStr.substring(16, 18);
            decodeInfo[3] = TmpStr.substring(18, 26);
            decodeInfo[4] = TmpStr.substring(26, 61);
            decodeInfo[5] = TmpStr.substring(61, 79);
            decodeInfo[6] = TmpStr.substring(79, 94);
            decodeInfo[7] = TmpStr.substring(94, 102);
            decodeInfo[8] = TmpStr.substring(102, 110);
            decodeInfo[9] = TmpStr.substring(110, 128);
            if (decodeInfo[1].equals("1"))
                decodeInfo[1] = "男";
            else
                decodeInfo[1] = "女";
            try {
                int code = Integer.parseInt(decodeInfo[2].toString());
                decodeInfo[2] = decodeNation(code);
            } catch (Exception e) {
                decodeInfo[2] = "";
            }

            //照片解码
            try {
                IDCReaderSDK instance = IDCReaderSDK.getInstance();
                String filepath = Environment.getExternalStorageDirectory() + "/wltlib";
                int ret = instance.Init(filepath);
                if (ret == 0) {
                    byte[] datawlt = new byte[1384];
//                    byte[] byLicData = {(byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0x00,
//                            (byte) 0x5B, (byte) 0x03, (byte) 0x33, (byte) 0x01, (byte) 0x5A,
//                            (byte) 0xB3, (byte) 0x1E, (byte) 0x00};
                    for (int i = 0; i < 1295; i++) {
                        datawlt[i] = recData[i];
                    }
                    int t = instance.unpack(datawlt);
                    if (t == 1) {
                        Readflage = 1;//读卡成功
                    } else {
                        Readflage = 6;//照片解码异常
                    }
                } else {
                    Readflage = 6;//照片解码异常
                }
            } catch (Exception e) {
                Readflage = 6;//照片解码异常
            }

        } else {
            Readflage = -5;//读卡失败！
        }

    }

    private static String decodesex(String code) {
        if (code.equals("1")) {
            return "男";
        } else {
            return "女";
        }
    }

    private static String decodeNation(int code) {
        String nation;
        switch (code) {
            case 1:
                nation = "汉";
                break;
            case 2:
                nation = "蒙古";
                break;
            case 3:
                nation = "回";
                break;
            case 4:
                nation = "藏";
                break;
            case 5:
                nation = "维吾尔";
                break;
            case 6:
                nation = "苗";
                break;
            case 7:
                nation = "彝";
                break;
            case 8:
                nation = "壮";
                break;
            case 9:
                nation = "布依";
                break;
            case 10:
                nation = "朝鲜";
                break;
            case 11:
                nation = "满";
                break;
            case 12:
                nation = "侗";
                break;
            case 13:
                nation = "瑶";
                break;
            case 14:
                nation = "白";
                break;
            case 15:
                nation = "土家";
                break;
            case 16:
                nation = "哈尼";
                break;
            case 17:
                nation = "哈萨克";
                break;
            case 18:
                nation = "傣";
                break;
            case 19:
                nation = "黎";
                break;
            case 20:
                nation = "傈僳";
                break;
            case 21:
                nation = "佤";
                break;
            case 22:
                nation = "畲";
                break;
            case 23:
                nation = "高山";
                break;
            case 24:
                nation = "拉祜";
                break;
            case 25:
                nation = "水";
                break;
            case 26:
                nation = "东乡";
                break;
            case 27:
                nation = "纳西";
                break;
            case 28:
                nation = "景颇";
                break;
            case 29:
                nation = "柯尔克孜";
                break;
            case 30:
                nation = "土";
                break;
            case 31:
                nation = "达斡尔";
                break;
            case 32:
                nation = "仫佬";
                break;
            case 33:
                nation = "羌";
                break;
            case 34:
                nation = "布朗";
                break;
            case 35:
                nation = "撒拉";
                break;
            case 36:
                nation = "毛南";
                break;
            case 37:
                nation = "仡佬";
                break;
            case 38:
                nation = "锡伯";
                break;
            case 39:
                nation = "阿昌";
                break;
            case 40:
                nation = "普米";
                break;
            case 41:
                nation = "塔吉克";
                break;
            case 42:
                nation = "怒";
                break;
            case 43:
                nation = "乌孜别克";
                break;
            case 44:
                nation = "俄罗斯";
                break;
            case 45:
                nation = "鄂温克";
                break;
            case 46:
                nation = "德昂";
                break;
            case 47:
                nation = "保安";
                break;
            case 48:
                nation = "裕固";
                break;
            case 49:
                nation = "京";
                break;
            case 50:
                nation = "塔塔尔";
                break;
            case 51:
                nation = "独龙";
                break;
            case 52:
                nation = "鄂伦春";
                break;
            case 53:
                nation = "赫哲";
                break;
            case 54:
                nation = "门巴";
                break;
            case 55:
                nation = "珞巴";
                break;
            case 56:
                nation = "基诺";
                break;
            case 97:
                nation = "其他";
                break;
            case 98:
                nation = "外国血统中国籍人士";
                break;
            default:
                nation = "";
                break;
        }
        return nation;
    }
}
