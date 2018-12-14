package com.speedata.libid2;

import android.content.Context;
import android.serialport.DeviceControlSpd;
import android.serialport.SerialPortSpd;
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
 * @author xuyan
 */
public class CpuCardApi implements IHFService {
    // 初始化
    private static final String TAG = "function_DEV";

    protected int m_iRecvBufSize = 1024 * 3;
    protected SerialPort m_SerialPort = null;
    protected InputStream m_InputStream = null;
    protected OutputStream m_OutputStream = null;
    protected boolean m_bIsPowerOn = false;
    protected int m_iRecvSize = 0;
    protected int m_iRecvOffset = 0;
    protected byte m_bysRecvBuffer[] = new byte[m_iRecvBufSize];
    protected int m_iCmdSize = 0;
    protected byte m_bysCmd[] = null;

    //识别是寻卡还是输入的指令
    protected int shibie = 0;
    protected int geshi = 0;
    //存放命令
    protected byte[] execmingling = null;


    //activity显示用的str,str2
    public String str = "";
    public String str2 = "";

    public CpuCardApi() {
        super();
    }

    /**
     * 2.读取SAMID安全模块号
     */
    @Override
    public byte[] readSamID() {
        byte byscmd[] = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA, (byte) 0x96, 0x69, 0x00, 0x03, 0x12, (byte) 0xFF, (byte) 0xEE};
        try {
            m_OutputStream.write(byscmd);
            m_OutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        getCmdWholeResp(300, 100);
        onReadSamID(m_bysRecvBuffer, m_iRecvSize);
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
     *  4.发送标准指令 内部实现自动打包成华旭二代证模块接收的指令格式
     */
    public byte[] execCmd(byte[] _bysCmd) {
        shibie = 2;
        geshi = 3;
        _bysCmd = execdabaoCmd(_bysCmd);
        execCompleteCmd(_bysCmd);
        return execmingling;
    }


    //按钮功能实现
    //发送指令按钮
    @Override
    public int execfasongCmd(String str) {
        shibie = 2;
        int iStrLen = 0;
        String strCmd01 = str;
        iStrLen = strCmd01.length();
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
        String strCmd02 = strCmd01.replace(" ", "");

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
    public byte[] execdabaoCmd(byte[] _bysCmd) {
        byte[] a = DataConversionUtils.execCmd(_bysCmd);//
        return a;
    }


    /**
     *  转换string为需要的标准命令
     */
    private byte[] geshizhuanhuan(String strResp) {

        byte[] s = DataConversionUtils.hexStringToByteArray(strResp);

        return s;
    }


    /**
     * 将一个4byte的数组转换成32位的int
     *
     * @param buf bytes buffer
     * @param pos byte[]中开始转换的位置
     * @return convert result
     */
    private long unsigned4BytesToInt(byte[] buf, int pos) {
        int firstByte = 0;
        int secondByte = 0;
        int thirdByte = 0;
        int fourthByte = 0;
        int index = pos;
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
        String dataString = new String();
        String tempString = new String();

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
    private void MySleep(long lWaitStep) {

        try {
            Thread.sleep(lWaitStep);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }

    }


    /**
     * 读取安全模块号
     */
    private void onReadSamID(final byte[] bysReadResp, final int iSize) {
        int i = 0;
        int j = 0;
        long lTemp = 0;

        byte SW1 = bysReadResp[7];
        byte SW2 = bysReadResp[8];
        byte SW3 = bysReadResp[9];

        String strCode = "";
        String strTemp = null;

        if ((0x0 != SW1) || (0x0 != SW2) || (((byte) 0x90) != SW3)) {

            return;
        }
        // 05.01
        byte bysCode01A[] = new byte[4];
        bysCode01A[0] = 0;
        bysCode01A[1] = 0;
        bysCode01A[2] = bysReadResp[11];
        bysCode01A[3] = bysReadResp[10];
        lTemp = unsigned4BytesToInt(bysCode01A, 0);
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
        lTemp = unsigned4BytesToInt(bysCode01B, 0);
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
        lTemp = unsigned4BytesToInt(bysCode02, 0);
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
        lTemp = unsigned4BytesToInt(bysCode03, 0);
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
        lTemp = unsigned4BytesToInt(bysCode04, 0);
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
            strResp = "寻卡:\n" + strToShort2(strResp);
        }

        str = strResp + "\n";

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
            String daan = wrongBack(strResp);
            return daan;
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
                a2 = "3006,操作非接触式用户卡数据无回应";
                break;
            case "31303031":
                a2 = "1001,不支持接触式用户卡";
                break;
            case "31303032":
                a2 = "1002,接触式用户卡未插到位";
                break;
            case "31303034":
                a2 = "1004,接触式用户卡未上电";
                break;
            case "31303036":
                a2 = "1006,操作接触式用户卡数据无回应";
                break;
            case "31303037":
                a2 = "1007,操作接触式用户卡数据出现错误";
                break;
            case "32303031":
                a2 = "2001,不支持PSAM卡";
                break;
            case "32303034":
                a2 = "2004,PSAM卡未上电";
                break;
            case "32303036":
                a2 = "2006,操作PSAM卡数据无回应";
                break;
            case "32303037":
                a2 = "2007,操作PSAM卡数据出现错误";
                break;
            case "33303031":
                a2 = "3001,不支持非接触式用户卡";
                break;
            case "33303034":
                a2 = "3004,非接触式用户卡未激活";
                break;
            case "33303037":
                a2 = "3007,操作非接触式用户卡数据出现错误";
                break;
            default:
                //如果不是上述信息,则反馈内容
                String str2 = "";
                for (int x = 1; x < a1.length(); x += 2) {
                    str2 = str2 + a1.substring(x, x + 1);
                }
                a2 = str2;
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
            String daan = wrongBack2(strResp);
            return daan;
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
                a2 = "3001,不支持非接触用户卡";
                break;
            case "33303035":
                a2 = "3005,非接触用户卡激活失败";
                break;
            case "33303036":
                a2 = "3006,等待卡进入感应区超时";
                break;
            case "33303039":
                a2 = "3009,有多张卡在感应区";
                break;
            //如果不是上述信息,则反馈内容
            default:
                String str2 = "";
                for (int x = 1; x < a1.length(); x += 2) {
                    str2 = str2 + a1.substring(x, x + 1);
                }
                a2 = str2;
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
    private void execCompleteCmd(byte[] _bysCmd) {
        int iResult = 0;
        iResult = inputCmd(_bysCmd);
        if (0 != iResult) {
            if (1 == iResult) {
                str = "上一条指令未执行完/n";
                return;
            } else if (2 == iResult) {
                str = "输入的指令为空/n";
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
    private int inputCmd(final byte[] _bysCmd) {
        if (0 != m_iCmdSize) {
            //上一条指令未执行完
            return 1;
        }

        if (0 == _bysCmd.length) {
            //输入了空指令
            return 2;
        }

        Log.i(TAG, "_bysCmd = " + DataConversionUtils.byteArrayToStringLog(_bysCmd, _bysCmd.length));

        m_iCmdSize = _bysCmd.length;
        m_bysCmd = _bysCmd.clone();

        Log.i(TAG, "_bysCmd = " + DataConversionUtils.byteArrayToStringLog(_bysCmd, _bysCmd.length));
        Log.i(TAG, "m_bysCmd = " + DataConversionUtils.byteArrayToStringLog(m_bysCmd, m_bysCmd.length));
        return 0;
    }


    private void executeCmd() {

        if (0 == m_iCmdSize) {
            str = "指令长度为0/n";
            return;
        }

        if (null == m_bysCmd) {
            str = "指令为空/n";
            return;
        }
        Log.i(TAG, "m_bysCmd = " + DataConversionUtils.byteArrayToStringLog(m_bysCmd, m_bysCmd.length));
        try {//发送指令
            m_OutputStream.write(m_bysCmd);
            m_OutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Log.i(TAG, "m_iCmdSize = " + m_iCmdSize + "m_bysCmd = " + DataConversionUtils.byteArrayToStringLog(m_bysCmd, m_bysCmd.length));
        //准备接受指令
        getCmdWholeResp(1000, 100);


        m_iCmdSize = 0;

    }


    /**
     * 问题出现于此方法,getCmdWholeResp得到的buffer值的帧头00是不对的,应为02
     */
    private void getCmdWholeResp(final long lWaitTotal, final long lWaitStep) {
        int i = 0;
        byte[] buffer = new byte[1024];
        int size = 0;

        //int iReadTimes = 0;

        long lWait = 0;

        int iCanReadSize = 0;

        boolean bIsDataExits = true;

        try {
            m_iRecvSize = 0;
            m_iRecvOffset = 0;

            //1
            while (true) {
                //2,读取反馈信息
                while (true) {
                    iCanReadSize = m_InputStream.available();
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
                    MySleep(lWaitStep);

                    lWait += lWaitStep;
                } //while(true) //2

                if (!bIsDataExits) {
                    break;
                }

                size = m_InputStream.read(buffer);
                //Log.i(TAG, "size" +  size);
                //buffer为空时,继续while
                if (size <= 0) {
                    continue;
                }

                Log.i(TAG, "buffer = " + DataConversionUtils.byteArrayToStringLog(m_bysRecvBuffer, m_bysRecvBuffer.length));
                //把buffer取出来放到m_bysRecvBuffer
                for (i = 1; i <= size; i++) {
                    m_bysRecvBuffer[m_iRecvOffset + i] = buffer[i];
                }
                m_iRecvOffset += size;
                m_iRecvSize = m_iRecvOffset;

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


    private Context mContext;
    private IDReadCallBack callBack;
    private ParseIDInfor parseIDInfor;
    private DeviceControlSpd deviceControl;
    private boolean isNeedFingerprinter;
    private SerialPortSpd mIDDev;
    private int fd;
    private static final int READ_NORMAL = 1024;
    private static final byte[] CMD_FIND_CARD = {(byte) 0xaa, (byte) 0xaa, (byte) 0xaa, (byte)
            0x96, 0x69, 0x00, 0x03, 0x20, 0x01, 0x22};


    /**
     *  1.初始化模块，包括设备上电，打开串口操作
     */
    @Override
    public boolean initDev(Context context, IDReadCallBack callBack) throws IOException {

        Long start = System
                .currentTimeMillis();
        ReadBean mConfig = ConfigUtils.readConfig(context);
        ReadBean.Id2Bean id2Bean = mConfig.getId2();
        parseIDInfor = new ParseIDInfor(context);
        this.mContext = context;
        this.callBack = callBack;
        List<Integer> gpio1 = id2Bean.getGpio();
        int[] gpio = new int[gpio1.size()];
        for (int i = 0; i < gpio.length; i++) {
            gpio[i] = gpio1.get(i);
        }

        openSerialPort(id2Bean.getSerialPort(), id2Bean.getBraut());

        gpioPower();

        return true;
    }


    /**
     *  5.释放模块，包括关闭串口，设备下电
     */
    @Override
    public void releaseDev() throws IOException {
        closeSerialPort();
        gpioPowerOff();
    }

    // 打开串口
    private int openSerialPort(String path, int baudrate) {
        if (m_SerialPort == null) {

            try {
                m_SerialPort = new SerialPort(new File(path), baudrate, 0);
                m_InputStream = m_SerialPort.getInputStream();
                m_OutputStream = m_SerialPort.getOutputStream();
            } catch (SecurityException e) {
                e.printStackTrace();
                return -1;
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
        }

        return 0;
    }

    // 关闭串口
    private void closeSerialPort() {
        if (m_SerialPort != null) {
            m_SerialPort.close();
            m_SerialPort = null;
        }
    }

    //GPIO上电
    private int gpioPower() {

        if(m_bIsPowerOn)
        {
            return 0;
        }
        int iResult = -1;
        String[] cmdx = new String[]{ "/system/bin/sh", "-c", "echo 1 > sys/HxReaderID_apk/hxreaderid" };
        try
        {
            iResult = ShellExe.execCommand(cmdx);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        if(0 == iResult)
        {
            m_bIsPowerOn = true;
        }
        return iResult;
    }


    //GPIO下电
    private int gpioPowerOff() {

        if(!m_bIsPowerOn)
        {
            return 0;
        }
        int iResult = -1;
        String[] cmdx = new String[]{ "/system/bin/sh", "-c", "echo 0 > sys/HxReaderID_apk/hxreaderid" };
        try
        {
            iResult = ShellExe.execCommand(cmdx);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        m_bIsPowerOn = false;
        return iResult;
    }

}
