package com.speedata.libid2;

import android.content.Context;
import android.util.Log;

import com.speedata.libid2.utils.DataConversionUtils;
import com.speedata.libid2.utils.ShellExe;
import com.speedata.libutils.ConfigUtils;
import com.speedata.libutils.ReadBean;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android_serialport_api.SerialPort;


/**
 * @author xuyan  高频卡API
 */
public class CpuCardApi implements IHFService {

    private static final String TAG = "function_DEV";

    private int mIrecvbufsize = 1024 * 3;
    private SerialPort mSerialport = null;
    private InputStream mInputstream = null;
    private OutputStream mOutputstream = null;
    private boolean mBispoweron = false;
    private int m_iRecvSize = 0;
    private int mIrecvoffset = 0;
    private byte m_bysRecvBuffer[] = new byte[mIrecvbufsize];
    private int m_iCmdSize = 0;
    private byte m_bysCmd[] = null;

    //识别是寻卡还是输入的指令
    private int shibie = 0;
    private int geshi = 0;
    //存放命令
    private byte[] execmingling = null;


    //activity显示用的str,str2
    private String str = "";
    private String str2 = "";

    CpuCardApi() {
        super();
    }


    /**
     * 1.初始化模块，包括设备上电，打开串口操作
     */
    @Override
    public boolean initDev(Context context, IDReadCallBack callBack) throws IOException {

        ReadBean mConfig = ConfigUtils.readConfig(context);
        ReadBean.Id2Bean id2Bean = mConfig.getId2();

        List<Integer> gpio1 = id2Bean.getGpio();
        int[] gpio = new int[gpio1.size()];
        for (int i = 0; i < gpio.length; i++) {
            gpio[i] = gpio1.get(i);
        }

        //if (openSerialPort("/dev/ttyMT1", id2Bean.getBraut()) != 0) {
        if (openSerialPort(id2Bean.getSerialPort(), id2Bean.getBraut()) != 0) {
            Log.d(TAG, "===openSerialPort===false===");
            Log.d(TAG, "===id2Bean.getSerialPort()===" + id2Bean.getSerialPort() + "===id2Bean.getBraut()===" + id2Bean.getBraut());
            return false;
        }

        int gp = gpioPower();
        Log.d(TAG, "===gpioPower===" + gp);
        return true;
    }

    /**
     * 2.读取SAMID安全模块号
     */
    @Override
    public byte[] readSamID() {
        byte byscmd[] = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE};
        try {
            mOutputstream.write(byscmd);
            mOutputstream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        getCmdWholeResp(300);
        onReadSamID(m_bysRecvBuffer);
        return m_bysRecvBuffer;

    }


    /**
     * 3.非接触卡寻卡操作
     */
    @Override
    public int nCSearch() {
        shibie = 1;
        byte byscmd[] = {0x02, 0x30, 0x30, 0x30, 0x34, 0x33, 0x32, 0x32,
                0x34, 0x30, 0x30, 0x30, 0x30, 0x31, 0x36, 0x03};
        execCompleteCmd(byscmd);

        return 0;

    }


    /**
     * 4.发送标准指令 内部实现自动打包成华旭二代证模块接收的指令格式
     */
    public byte[] execCmd(byte[] byscmd) {
        shibie = 2;
        geshi = 3;
        byscmd = execdabaoCmd(byscmd);
        execCompleteCmd(byscmd);
        return execmingling;
    }


    /**
     * 5.释放模块，包括关闭串口，设备下电
     */
    @Override
    public void releaseDev() {
        closeSerialPort();
        gpioPowerOff();
    }


    //按钮功能实现
    /**
     *  发送指令按钮
     */
    @Override
    public int execfasongCmd(String str) {
        shibie = 2;
        int iStrLen;
        iStrLen = str.length();
        if (0 == iStrLen) {
            return -1;
        }
        if (iStrLen < 2) {

            return -1;
        }
        if (0 != iStrLen % 2) {

            return -1;
        }
        // 去掉指令空格
        String strCmd02 = str.replace(" ", "");

        // 命令封装方法,把apdu封装成完成长度如把0084000008封装成
        // 0230303038333232363f3f30303834303030303038363703这样的完整命令
        // 封装
        byte[] bysRealCmd = DataConversionUtils.packageCommand(strCmd02);

        execCompleteCmd(bysRealCmd);

        return 0;
    }


    /**
     * 非接卡随机数按钮
     */
    @Override
    public int random() {
        shibie = 2;
        //apdu指令
        byte byscmd[] = {0x00, (byte) 0x84, 0x00, 0x00, 0x08};
        byte[] a = execdabaoCmd(byscmd);
        execCompleteCmd(a);
        return 0;
    }

    @Override
    public String getResult() {
        return str;
    }

    @Override
    public String getSafeResult() {
        return str2;
    }

    //调用的工具类、方法

    /**
     * (输入输出)标准指令打包成华旭二代证模块接收的指令格式
     */
    private byte[] execdabaoCmd(byte[] byscmd) {
        return DataConversionUtils.execCmd(byscmd);
    }


    /**
     * 转换string为需要的标准命令
     */
    private byte[] geshizhuanhuan(String strResp) {

        return DataConversionUtils.hexStringToByteArray(strResp);
    }


    /**
     * 将一个4byte的数组转换成32位的int
     *
     * @param buf bytes buffer
     * @return convert result
     */
    private long unsigned4BytesToInt(byte[] buf) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = 0;
        firstByte = (0x000000FF & ((int) buf[index]));
        secondByte = (0x000000FF & ((int) buf[index + 1]));
        thirdByte = (0x000000FF & ((int) buf[index + 2]));
        fourthByte = (0x000000FF & ((int) buf[index + 3]));
        index = index + 4;
        return ((long) (firstByte << 24 | secondByte << 16 | thirdByte << 8 | fourthByte)) & 0xFFFFFFFFL;
    }


    /**
     * 将数据转换为16进制形式字符串
     */
    private String bytes2Hexstring(final byte[] buffer, final int size) {
        String dataString = "";
        String tempString = "";

        int i = 0;

        for (i = 0; i < size; i++) {
            tempString = Integer.toHexString(buffer[i] & 0xFF);
            if (1 == tempString.length()) {
                dataString += "0";
            }
            dataString += Integer.toHexString(buffer[i] & 0xFF);

        }

        return dataString;
    }


    /**
     * 让while循环线程休眠的时间
     */
    private void mysleep() {
        try {
            Thread.sleep((long) 100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 读取安全模块号
     */
    private void onReadSamID(final byte[] bysReadResp) {
        int i;
        int j;
        long lTemp;

        byte SW1 = bysReadResp[7];
        byte SW2 = bysReadResp[8];
        byte SW3 = bysReadResp[9];

        String strCode = "";
        String strTemp;

        if ((0x0 != SW1) || (0x0 != SW2) || (((byte) 0x90) != SW3)) {

            return;
        }
        // 05.01
        byte bysCode01A[] = new byte[4];
        bysCode01A[0] = 0;
        bysCode01A[1] = 0;
        bysCode01A[2] = bysReadResp[11];
        bysCode01A[3] = bysReadResp[10];
        lTemp = unsigned4BytesToInt(bysCode01A);
        strTemp = Long.toString(lTemp);
        j = 2 - strTemp.length();
        for (i = 0; i < j; i++) {
            strCode += "0";
        }
        strCode += Long.toString(lTemp);

        strCode += ".";

        byte bysCode01B[] = new byte[4];
        bysCode01B[0] = 0;
        bysCode01B[1] = 0;
        bysCode01B[2] = bysReadResp[13];
        bysCode01B[3] = bysReadResp[12];
        lTemp = unsigned4BytesToInt(bysCode01B);
        strTemp = Long.toString(lTemp);
        j = 2 - strTemp.length();
        for (i = 0; i < j; i++) {
            strCode += "0";
        }
        strCode += Long.toString(lTemp);

        // 分隔符'-'
        strCode += "-";

        // 20101129
        byte bysCode02[] = new byte[4];
        bysCode02[0] = bysReadResp[17];
        bysCode02[1] = bysReadResp[16];
        bysCode02[2] = bysReadResp[15];
        bysCode02[3] = bysReadResp[14];
        lTemp = unsigned4BytesToInt(bysCode02);
        strTemp = Long.toString(lTemp);
        strCode += Long.toString(lTemp);

        //  分隔符'-'
        strCode += "-";

        // 1228293
        byte bysCode03[] = new byte[4];
        bysCode03[0] = bysReadResp[21];
        bysCode03[1] = bysReadResp[20];
        bysCode03[2] = bysReadResp[19];
        bysCode03[3] = bysReadResp[18];
        lTemp = unsigned4BytesToInt(bysCode03);
        strTemp = Long.toString(lTemp);
        j = 10 - strTemp.length();
        for (i = 0; i < j; i++) {
            strCode += "0";
        }
        strCode += Long.toString(lTemp);

        // 分隔符'-'
        strCode += "-";

        // 296863149
        byte bysCode04[] = new byte[4];
        bysCode04[0] = bysReadResp[25];
        bysCode04[1] = bysReadResp[24];
        bysCode04[2] = bysReadResp[23];
        bysCode04[3] = bysReadResp[22];
        lTemp = unsigned4BytesToInt(bysCode04);
        strTemp = Long.toString(lTemp);
        j = 10 - strTemp.length();
        for (i = 0; i < j; i++) {
            strCode += "0";
        }
        strCode += Long.toString(lTemp);

        // 显示安全模块号
        str2 = strCode + "\n";

    }


    /**
     * 指令应答处理,得到显示的数据给str、str2 以及 处理反馈信息格式
     */
    private void onCmdResp(final byte[] bysReadResp, final int iSize) {
        if (iSize <= 0) {
            return;
        }
        Log.i(TAG, "bysReadResp = " + DataConversionUtils.byteArrayToStringLog(bysReadResp, bysReadResp.length));
        //把byte[]变成String字符串
        String strResp = bytes2Hexstring(bysReadResp, iSize);
        Log.i(TAG, "strResp = " + strResp);
        //如果是输入指令,处理显示数据
        if (shibie == 2) {

            strResp = strToShort(strResp);
            //如果是生成所需的格式,则需进行格式转换
            if (geshi == 3) {
                execmingling = geshizhuanhuan(strResp);

                geshi = 0;

            }
            //如果是寻卡
        } else if (shibie == 1) {
            strResp = strToShort2(strResp);
        }

        str = strResp;

        Log.i(TAG, "str = " + str);

    }


    /**
     * 把得到的String型的完整指令进行分析,如果是错误信息则进行分析
     */
    private String strToShort(String strResp) {
        //得到命令内容
        //长度短的反馈是错误信息
        if (strResp.length() == 24) {
            //反馈错误
            return wrongBack(strResp);
        } else {//得到结果
            return quchu3(strResp);
        }
    }

    /**
     * 反馈的错误信息分析
     */
    private String wrongBack(String strResp) {
        String a1 = strResp.substring(10, 18);
        String a2 = "";
        switch (a1) {
            case "33303036":
                a2 = "提示:3006,操作非接触式用户卡数据无回应";
                break;
            case "31303031":
                a2 = "提示:1001,不支持接触式用户卡";
                break;
            case "31303032":
                a2 = "提示:1002,接触式用户卡未插到位";
                break;
            case "31303034":
                a2 = "提示:1004,接触式用户卡未上电";
                break;
            case "31303036":
                a2 = "提示:1006,操作接触式用户卡数据无回应";
                break;
            case "31303037":
                a2 = "提示:1007,操作接触式用户卡数据出现错误";
                break;
            case "32303031":
                a2 = "提示:2001,不支持PSAM卡";
                break;
            case "32303034":
                a2 = "提示:2004,PSAM卡未上电";
                break;
            case "32303036":
                a2 = "提示:2006,操作PSAM卡数据无回应";
                break;
            case "32303037":
                a2 = "提示:2007,操作PSAM卡数据出现错误";
                break;
            case "33303031":
                a2 = "提示:3001,不支持非接触式用户卡";
                break;
            case "33303034":
                a2 = "提示:3004,非接触式用户卡未激活";
                break;
            case "33303037":
                a2 = "提示:3007,操作非接触式用户卡数据出现错误";
                break;
            default:
                //如果不是上述信息,则反馈内容
                String str2 = "";
                for (int x = 1; x < a1.length(); x += 2) {
                    str2 = str2 + a1.substring(x, x + 1);
                }
                a2 = "提示:" + str2;
                break;
        }
        return a2;
    }


    /**
     * 把 寻卡 得到的String型的信息进行分析
     */
    private String strToShort2(String strResp) {
        //长度短的反馈是需要解析的
        if (strResp.length() == 24) {
            return wrongBack2(strResp);
        } else {//得到结果
            return quchu3(strResp);
        }

    }

    /**
     * 解析寻卡反馈的信息
     */
    private String wrongBack2(String strResp) {
        String a1 = strResp.substring(10, 18);
        String a2 = "";
        switch (a1) {
            case "33303031":
                a2 = "提示:3001,不支持非接触用户卡";
                break;
            case "33303035":
                a2 = "提示:3005,非接触用户卡激活失败";
                break;
            case "33303036":
                a2 = "提示:3006,等待卡进入感应区超时";
                break;
            case "33303039":
                a2 = "提示:3009,有多张卡在感应区";
                break;
            //如果不是上述信息,则反馈内容
            default:
                String str2 = "";
                for (int x = 1; x < a1.length(); x += 2) {
                    str2 = str2 + a1.substring(x, x + 1);
                }
                a2 = "提示:" + str2;
                break;
        }
        return a2;
    }


    /**
     * 把完整的二代证指令去除帧头帧尾效验码和"3",得到数据
     */
    private String quchu3(String strResp) {
        String str1 = strResp.substring(18, strResp.length() - 6);

        //把内容去除"3"
        String str2 = "";
        for (int x = 1; x < str1.length(); x += 2) {
            str2 = str2 + str1.substring(x, x + 1);
        }
        strResp = str2;
        return strResp;
    }


    //指令执行部分


    /**
     * 执行指令,输入的指令为二代证模块识别的完整指令
     */
    private void execCompleteCmd(byte[] byscmd) {
        int iResult = 0;
        iResult = inputCmd(byscmd);
        if (0 != iResult) {
            if (1 == iResult) {
                str = "提示:上一条指令未执行完";
                return;
            } else if (2 == iResult) {
                str = "提示:输入的指令为空";
                return;
            }

            return;
        }
        Log.i(TAG, "iResult = " + iResult);

        executeCmd();//处理指令

    }


    /**
     * 分析指令是否为空以及是否有未执行完的指令
     */
    private int inputCmd(final byte[] byscmd) {
        if (0 != m_iCmdSize) {
            //上一条指令未执行完
            return 1;
        }

        if (0 == byscmd.length) {
            //输入了空指令
            return 2;
        }

        Log.i(TAG, "_bysCmd = " + DataConversionUtils.byteArrayToStringLog(byscmd, byscmd.length));

        m_iCmdSize = byscmd.length;
        m_bysCmd = byscmd.clone();

        Log.i(TAG, "_bysCmd = " + DataConversionUtils.byteArrayToStringLog(byscmd, byscmd.length));
        Log.i(TAG, "m_bysCmd = " + DataConversionUtils.byteArrayToStringLog(m_bysCmd, m_bysCmd.length));
        return 0;
    }


    private void executeCmd() {

        if (0 == m_iCmdSize) {
            str = "提示:指令长度为0";
            return;
        }

        if (null == m_bysCmd) {
            str = "提示:指令为空";
            return;
        }
        Log.i(TAG, "m_bysCmd = " + DataConversionUtils.byteArrayToStringLog(m_bysCmd, m_bysCmd.length));
        try {//发送指令
            mOutputstream.write(m_bysCmd);
            mOutputstream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.i(TAG, "m_iCmdSize = " + m_iCmdSize + "m_bysCmd = " + DataConversionUtils.byteArrayToStringLog(m_bysCmd, m_bysCmd.length));
        //准备接受指令
        getCmdWholeResp(1000);


        m_iCmdSize = 0;

    }


    /**
     * 问题出现于此方法,getCmdWholeResp得到的buffer值的帧头00是不对的,应为02
     */
    private void getCmdWholeResp(final long lWaitTotal) {
        int i;
        byte[] buffer = new byte[1024];
        int size;
        long lWait = 0;
        int iCanReadSize;
        boolean bIsDataExits = true;

        try {
            m_iRecvSize = 0;
            mIrecvoffset = 0;

            //1
            while (true) {
                //2,读取反馈信息
                while (true) {
                    iCanReadSize = mInputstream.available();
                    if (iCanReadSize > 0) {
                        break;
                    }

                    //读数据超时, 跳出.
                    if (lWait > lWaitTotal) {
                        //m_iCmdSize = 0;
                        bIsDataExits = false;
                        break;
                    }
                    //延时读取信息
                    mysleep();

                    lWait += (long) 100;
                } //while(true) //2

                if (!bIsDataExits) {
                    break;
                }

                size = mInputstream.read(buffer);

                //buffer为空时,继续while
                if (size <= 0) {
                    continue;
                }

                Log.i(TAG, "buffer = " + DataConversionUtils.byteArrayToStringLog(m_bysRecvBuffer, m_bysRecvBuffer.length));
                //把buffer取出来放到m_bysRecvBuffer
                for (i = 1; i <= size; i++) {
                    m_bysRecvBuffer[mIrecvoffset + i] = buffer[i];
                }
                mIrecvoffset += size;
                m_iRecvSize = mIrecvoffset;

            } //while(true) //1

        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "m_bysRecvBuffer = " + DataConversionUtils.byteArrayToStringLog(m_bysRecvBuffer, m_bysRecvBuffer.length) + "      m_iRecvSize =" + m_iRecvSize);
        //手动处理帧头,把错误的0x00改为0x02
        //完整的反馈信息
        m_bysRecvBuffer[0] = 0x02;
        //处理反馈信息,生成显示的字符串
        onCmdResp(m_bysRecvBuffer, m_iRecvSize);
    }


    /**
     *    打开串口
      */
    private int openSerialPort(String path, int baudrate) {
        if (mSerialport == null) {

            try {
                mSerialport = new SerialPort(new File(path), baudrate, 0);
                mInputstream = mSerialport.getInputStream();
                mOutputstream = mSerialport.getOutputStream();
            }  catch (Exception e) {
                e.printStackTrace();
                return -1;
            }
        }

        return 0;
    }

    /**
     * 关闭串口
      */
    private void closeSerialPort() {
        if (mSerialport != null) {
            mSerialport.close();
            mSerialport = null;
        }
    }

    /**
     * GPIO上电
     */
    private int gpioPower() {

        if (mBispoweron) {
            return 0;
        }
        int iResult = -1;
        String[] cmdx = new String[]{"/system/bin/sh", "-c", "echo 1 > sys/HxReaderID_apk/hxreaderid"};
        try {
            iResult = ShellExe.execCommand(cmdx);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (0 == iResult) {
            mBispoweron = true;
        }
        return iResult;
    }


    /**
     * GPIO下电
     */
    private void gpioPowerOff() {

        if (!mBispoweron) {
            return;
        }

        String[] cmdx = new String[]{"/system/bin/sh", "-c", "echo 0 > sys/HxReaderID_apk/hxreaderid"};
        try {
            ShellExe.execCommand(cmdx);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBispoweron = false;
    }

}
