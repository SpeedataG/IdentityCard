package com.speedata.libid2;

import android.serialport.DeviceControl;

/**
 * ----------Dragon be here!----------/
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛
 * 　　　　┃　　　┃神兽保佑
 * 　　　　┃　　　┃代码无BUG！
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━神兽出没━━━━━━
 *
 * @author :Reginer in  2017/2/23 16:52.
 *         联系方式:QQ:282921012
 *         功能描述:获取参数
 */
class DeviceType {
    /**
     * 获取串口.
     *
     * @return SerialPort
     */
    static String getSerialPort() {
        switch (android.os.Build.MODEL) {
            case "kt45":
                return "/dev/ttyMT1";
            case "kt45q":
                return "/dev/ttyMT1";
            case "kt50":
                return "/dev/ttyMT1";
            case "KT55":
                return "/dev/ttyMT2";
            default:
                break;

        }

        return "/dev/ttyMT1";

    }

    /**
     * getPowerType.
     *
     * @return PowerType
     */
    static DeviceControl.PowerType getPowerType() {
        switch (android.os.Build.MODEL) {
            case "kt45":
                return DeviceControl.PowerType.MAIN;
            case "kt45q":
                return DeviceControl.PowerType.MAIN;
            case "kt50":
                return DeviceControl.PowerType.MAIN;
            case "KT55":
                return DeviceControl.PowerType.MAIN_AND_EXPAND;

            default:
                break;
        }
        return DeviceControl.PowerType.MAIN;
    }

    /**
     * getGpio .
     *
     * @return Gpio
     */
    static int[] getGpio() {
        switch (android.os.Build.MODEL) {
            case "kt45":
                return new int[]{106};
            case "kt45q":
                return new int[]{94};
            case "kt50":
                return new int[]{64};
            case "KT55":
                return new int[]{88, 6};

            default:
                break;
        }
        return new int[]{106};
    }
}
