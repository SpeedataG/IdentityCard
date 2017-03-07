package com.speedata.identitycard;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcB;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.speedata.face.FaceUtil;
import com.speedata.face.MyCamera;
import com.speedata.identitycard.utils.ProgressDialogUtils;
import com.speedata.identitysoftware.IdentitySoftware;
import com.speedata.libid2.IDInfor;
import com.speedata.libid2.IDManager;
import com.speedata.libid2.IDReadCallBack;
import com.speedata.libid2.IID2Service;
import com.speedata.libid2.ParseIDInfor;
import com.zqd.idcard.CardManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.speedata.identitycard.R.id.camera;
import static com.speedata.identitysoftware.IdentitySoftware.COM_SPEEDATA_SOFTWARE_ID;

public class IDTestActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int WIDTH = 30;
    public static final int HEIGHT = 34;
    private TextView tvIDInfor;
    private ImageView imgPic;
    private Button btnRead, btnSelect, btnSearch;
    private ToggleButton btnGet;
    private Button btnTakePhoto, btnCompare;
    private ImageView imgFinger;
    private CheckBox checkBoxFinger;
    private TextView tvCompareResult;
    private TextView tvInfor;
    private Button btnPreView;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            clearUI();
            IDInfor idInfor1 = (IDInfor) msg.obj;
            if (idInfor1.isSuccess()) {
                tvIDInfor.setText("姓名:" + idInfor1.getName() + "\n身份证号：" + idInfor1.getNum() +
                        "\n性别：" + idInfor1.getSex() +
                        "\n民族：" + idInfor1.getNation() + "\n住址:" +
                        idInfor1.getAddress() + "\n出生：" + idInfor1.getYear() + "年" + idInfor1
                        .getMonth() + "月" + idInfor1.getDay() + "日" + "\n有效期限：" + idInfor1
                        .getDeadLine());
                Bitmap bmps = idInfor1.getBmps();
                imgPic.setImageBitmap(bmps);
                if (bmps != null) {
                    file1 = new File(file1Path);
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream
                                (file1));
                        bmps.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
                        bos.flush();//输出
                        bos.close();//关闭
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else {
                tvIDInfor.setText("ERROR:" + idInfor1.getErrorMsg());
                imgPic.setImageBitmap(null);
            }
            if (idInfor1.isWithFinger()) {

                Bitmap bitmap = ShowFingerBitmap(idInfor1.getFingerprStringer(), WIDTH, HEIGHT);
                imgFinger.setImageBitmap(bitmap);
            }
        }
    };

    private CheckBox checkBoxCamera;
    private Timer timer = new Timer();

    class readIDTask extends TimerTask {
        @Override
        public void run() {

            iid2Service.getIDInfor(checkBoxFinger.isChecked());
        }
    }

    private IdentitySoftware software;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if (software.bitmap != null)
//                imgPic.setImageBitmap(software.bitmap);
            byte[] data = intent.getByteArrayExtra("byte_data");
            if (data != null) {
                showToast("nfc read id success");
                ParseIDInfor parseIDInfor = new ParseIDInfor(IDTestActivity.this);
                IDInfor idInfor = parseIDInfor.parseIDInfor(data, false);
                if (idInfor != null) {
                    if (idInfor.isSuccess()) {
                        tvIDInfor.setText("姓名:" + idInfor.getName() + "\n身份证号：" + idInfor.getNum() + "\n性别：" + idInfor.getSex() +
                                "\n民族：" + idInfor.getNation() + "\n住址:" +
                                idInfor.getAddress() + "\n出生：" + idInfor.getYear() + "年" + idInfor
                                .getMonth() + "月" + idInfor.getDay() + "日" + "\n有效期限：" + idInfor
                                .getDeadLine());
                        Bitmap bmps = idInfor.getBmps();
                        imgPic.setImageBitmap(bmps);
                        if (bmps != null) {
                            file1 = new File(file1Path);
                            try {
                                BufferedOutputStream bos = new BufferedOutputStream(new
                                        FileOutputStream

                                        (file1));
                                bmps.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
                                bos.flush();//输出
                                bos.close();//关闭
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                    } else {
                        tvIDInfor.setText("ERROR:" + idInfor.getErrorMsg());
                        imgPic.setImageBitmap(null);
                    }
                }
            }
            String msg = intent.getStringExtra("id_data");
            if (msg != null)
                tvIDInfor.setText(msg);

        }
    };

    private void regist() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(COM_SPEEDATA_SOFTWARE_ID);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setForground();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idtest);
//        IUHFService iuhfService = UHFManager.getUHFService(this);
//        iuhfService.OpenDev();
//        iuhfService.inventory_start(new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//            }
//        });
        software = IdentitySoftware.getIntance(this);
        regist();

        mySurfaceView = (SurfaceView) findViewById(camera);
        myCamera = new MyCamera(this, this, mySurfaceView, true);
        tvIDInfor = (TextView) findViewById(R.id.tv_idinfor);
        imgPic = (ImageView) findViewById(R.id.img_pic);
        btnRead = (Button) findViewById(R.id.btn_read);
        btnRead.setOnClickListener(this);
        btnSearch = (Button) findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(this);
        btnSelect = (Button) findViewById(R.id.btn_select);
        btnSelect.setOnClickListener(this);
        btnGet = (ToggleButton) findViewById(R.id.btn_get);
        btnGet.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (timer == null) {
                    timer = new Timer();
                }
                if (b) {
                    timer.schedule(new readIDTask(), 20, 3000);
                } else {
                    timer.cancel();
                    timer = null;
                }
            }
        });

        tvInfor = (TextView) findViewById(R.id.tv_msg);

        checkBoxFinger = (CheckBox) findViewById(R.id.checkbox_wit_finger);
        checkBoxCamera = (CheckBox) findViewById(R.id.check_camer);

        checkBoxCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                myCamera.setState(b);
                if (b) {
                    myCamera.takePreview(0);
                } else {
                    myCamera.takePreview(1);
                }
            }
        });
        imgFinger = (ImageView) findViewById(R.id.img_finger);

        btnTakePhoto = (Button) findViewById(R.id.btn_take_photo);
        btnTakePhoto.setOnClickListener(this);

        btnCompare = (Button) findViewById(R.id.btn_compare);
        btnCompare.setOnClickListener(this);
        tvCompareResult = (TextView) findViewById(R.id.tv_compare_result);
        btnPreView = (Button) findViewById(R.id.btn_preview);
        btnPreView.setOnClickListener(this);
        //获得控件
        initID();

    }

    private MyCamera myCamera;

    private IID2Service iid2Service;

    private void testID() {
        iid2Service.getIDInfor(checkBoxFinger.isChecked());
    }

    private void initID() {
        iid2Service = IDManager.getInstance();
        try {
            boolean result = iid2Service.initDev(this, new IDReadCallBack() {

                @Override
                public void callBack(IDInfor infor) {
                    Message message = new Message();
                    message.obj = infor;
                    handler.sendMessage(message);
                }
            });
            tvInfor.setText("s:MT1 b:115200 p:106");
            if (!result) {
                new AlertDialog.Builder(this).setCancelable(false).setMessage("二代证模块初始化失败")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {


                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                btnGet.setEnabled(false);
                            }
                        }).show();
            } else {
                showToast("初始化成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    FaceCompare compare = new FaceCompare(this) {
        @Override
        public void FaceCompare(boolean result) {
            showToast("成功");
            ProgressDialogUtils.dismissProgressDialog();
        }

        @Override
        public void showMsg(String msg) {
//            tvCompareResult.setText(msg);
            new AlertDialog.Builder(IDTestActivity.this).setCancelable(false).setPositiveButton
                    ("确定", null).setTitle("对比结果").setMessage(msg).show();
        }
    };

    @Override
    public void onClick(View view) {
        if (view == btnRead) {
            IDInfor infor = iid2Service.readCard(checkBoxFinger.isChecked());
            Message message = new Message();
            message.obj = infor;
            handler.sendMessage(message);
        } else if (view == btnGet) {
            tvIDInfor.setText("");
            imgPic.setImageDrawable(null);
            testID();
        } else if (view == btnSearch) {
            int result = iid2Service.searchCard();
            tvIDInfor.setText(iid2Service.parseReturnState(result));
        } else if (view == btnSelect) {
            int result = iid2Service.selectCard();
            tvIDInfor.setText(iid2Service.parseReturnState(result));
        } else if (view == btnTakePhoto) {
            try {
                myCamera.takePic(jpeg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (view == btnCompare) {
            if (file != null && file1 != null) {
                compare.compare(file, file1);
                ProgressDialogUtils.showProgressDialog(this, "正在对比请稍后");
            }
        } else if (view == btnPreView) {
            myCamera.preview();
        }
    }

    private String filePath = "/sdcard/qianzhi.jpg";//照片保存路径
    private String file1Path = "/sdcard/shenfenzheng.jpg";//照片保存路径
    File file = new File(filePath);
    File file1 = new File(file1Path);
    //创建jpeg图片回调数据对象
    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {// 获得图片
                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                bm = FaceUtil.rotateToDegrees(bm, 90);
                file = new File(filePath);
                BufferedOutputStream bos =
                        new BufferedOutputStream(new FileOutputStream(file));
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
                bos.flush();//输出
                bos.close();//关闭
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            iid2Service.releaseDev();
        } catch (IOException e) {
            e.printStackTrace();
        }
        unregisterReceiver(receiver);
        myCamera.releaseCamera();
    }


    private void clearUI() {
        tvIDInfor.setText("");
        imgPic.setImageBitmap(null);
    }

    private SurfaceView mySurfaceView;//surfaceView声明


//    private void takePhotoByPriView() {
//        //打开补光灯
//        Camera.Parameters parameters = myCamera.getParameters();
//        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//        myCamera.setParameters(parameters);
//    }

    //------------------------------------------------------------------------------------------
    private Bitmap ShowFingerBitmap(byte[] image, int width, int height) {
        if (width == 0) return null;
        if (height == 0) return null;

        int[] RGBbits = new int[width * height];
//        viewFinger.invalidate();
        for (int i = 0; i < width * height; i++) {
            int v;
            if (image != null) v = image[i] & 0xff;
            else v = 0;
            RGBbits[i] = Color.rgb(v, v, v);
        }
        Bitmap bmp = Bitmap.createBitmap(RGBbits, width, height, Bitmap.Config.RGB_565);
        return bmp;
    }


    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }


    // 关于NFC
    public NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private boolean isFirsRun = true;
    private boolean isProcessIntent;
    public static NfcB isoDep;
    private int sysVersion;

    /**
     * 设定前台系统可用
     *
     * @param mPendingIntent
     */
    private void enableNdefExchangeMode(PendingIntent mPendingIntent) {
        nfcAdapter.enableForegroundDispatch(this, mPendingIntent,
                CardManager.intentFiltersArray, CardManager.mTechLists);
    }

    /**
     * 设定前台系统
     */
    private void setForground() {
        sysVersion = Integer.parseInt(Build.VERSION.SDK);
        // 设置优先级-前台发布系统，
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            return;
        }
        // 获取默认的NFC控制器
        if (isFirsRun) {
            mPendingIntent = PendingIntent.getActivity(this, 0, getIntent(), 0);
            enableNdefExchangeMode(mPendingIntent);
            isFirsRun = false;
            enableReaderMode();
        }
    }

    @TargetApi(19)
    private void enableReaderMode() {
        if (sysVersion < 19)
            return;
        Bundle options = new Bundle();
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 1000);
        int READER_FLAGS = NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B |
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
        if (nfcAdapter != null) {
            nfcAdapter.enableReaderMode(this, new MyReaderCallback(), READER_FLAGS, options);
        }
    }

    private Tag tag;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processExtraData(intent);
    }

    private void processExtraData(Intent intent) {
        isProcessIntent = true;
        tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        tvInfor.setText("tag:" + tag.toString());
//        if(face!=null)imageView10066.setImageBitmap(face);
        //processIntentB();
        software.zqdreadId.NFCWithIntent(intent);
//        ReadIDStard = System.currentTimeMillis();
        software.zqdreadId.ReadBCard();
    }


    public class MyReaderCallback implements NfcAdapter.ReaderCallback {

        @Override
        public void onTagDiscovered(final Tag arg0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressDialogUtils.showProgressDialog(IDTestActivity.this, "正在解析");
                    tvInfor.setText(arg0.toString());
                }
            });
            System.out.println("arg0");
            software.zqdreadId.NFCWithTag(arg0);
            software.zqdreadId.ReadBCard();
        }
    }
}
