package com.speedata.identity_as;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.speedata.identity_as.utils.ToastUtils;
import com.speedata.libid2.HFManager;
import com.speedata.libid2.IHFService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author xuyan  华旭的二代证读非接卡部分
 */
public class HfActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnBack, btnSure, btnClearWindow, btnSafecode, btnNonOn, btnNonRandom;
    private EditText m_RespView;
    private EditText m_CmdEdit;
    //输入法管理器
    protected InputMethodManager m_imm = null;
    //API等
    private IHFService ihfService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hf);
        creatrFile();
        PlaySoundUtils.initSoundPool(this);
        initView();
        initDev();
    }

    private void creatrFile() {
        copyfile("/sdcard/wltlib", "base.dat", R.raw.base);
        copyfile("/sdcard/wltlib", "license.lic", R.raw.license);
    }

    private void copyfile(String fileDirPath, String fileName, int id) {
        // 文件路径
        String filePath = fileDirPath + "/" + fileName;
        try {
            File files = new File("/sdcard/wltlib");
            if (!files.exists()) {
                files.mkdirs();
            }
            // 文件夹存在，则将apk中raw文件夹中的须要的文档拷贝到该文件夹下
            File file = new File(filePath);
            // 文件不存在
            if (!file.exists()) {
                // 通过raw得到数据资源
                InputStream is = getResources().openRawResource(id);
                FileOutputStream fs = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                // 循环写出
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fs.write(buffer, 0, count);
                }
                // 关闭流
                fs.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        //输入法管理
        m_imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        btnBack = findViewById(R.id.back_btn);
        btnSure = findViewById(R.id.sure_btn);
        btnClearWindow = findViewById(R.id.clearwindow_btn);

        btnSafecode = findViewById(R.id.safecode_btn);
        btnNonOn = findViewById(R.id.noncard_onoroff_btn);
        btnNonRandom = findViewById(R.id.noncard_random_btn);

        m_RespView = findViewById(R.id.displaywindow_et);
        m_CmdEdit = findViewById(R.id.input_et);

        btnBack.setOnClickListener(this);
        btnSure.setOnClickListener(this);

        btnClearWindow.setOnClickListener(this);

        btnSafecode.setOnClickListener(this);
        btnNonOn.setOnClickListener(this);
        btnNonRandom.setOnClickListener(this);

        //手动输入的指令,以取非接卡随机数为例
        m_CmdEdit.setText("0084000008");

        Editable ea = m_CmdEdit.getText();

        m_CmdEdit.setSelection(ea.length());

        //隐藏软键盘
        hideSoftInpu();

        //让软键盘不挡住EIDT. 编辑时，将会把把EDIT以上部分整体上移,  而不是压缩紧靠EDIT上方紧邻的那个控件.
        //若是压缩上方控件，则可能上方控件高度不足，无足够压缩空间导致EDIT被挡住.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    private void initDev() {
        ihfService = HFManager.getInstance();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在初始化");
        progressDialog.show();
        new Thread(() -> {
            try {
//
                final boolean result = ihfService.initDev(HfActivity.this, infor -> {
                    Message message = new Message();
                    message.obj = infor;
                    handler.sendMessage(message);
                });

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (!result) {
                        new AlertDialog.Builder(HfActivity.this).setCancelable(false)
                                .setMessage("二代证模块初始化失败")
                                .setPositiveButton("确定", (dialogInterface, i) -> finish()).show();
                    } else {
                        ToastUtils.showShortToastSafe("初始化成功");
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }


    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    };

    /**
     * 隐藏软键盘
     */
    protected void hideSoftInpu() {
        if (null != m_imm) {
            boolean isOpen = m_imm.isActive();
            if (isOpen) {
                m_imm.hideSoftInputFromWindow(m_CmdEdit.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onDestroy() {
        try {
            ihfService.releaseDev();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        // 返回
        if (v == btnBack) {
            // 退出读卡线程
            try {
                ihfService.releaseDev();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finish();
            // 发送指令
        } else if (v == btnSure) {
            String str = m_CmdEdit.getText().toString();
            int q = ihfService.execfasongCmd(str);
            if (q == -1) {
                m_RespView.append("输入有问题\n");
            } else if (q == 0) {
                m_RespView.append(ihfService.getResult());
            }
            // 清空显示窗口
        } else if (v == btnClearWindow) {
            m_RespView.setText(null);
            // 安全模块号
        } else if (v == btnSafecode) {
            ihfService.readSamID();
            m_RespView.append(ihfService.getSafeResult());
            // 非接卡寻卡
        } else if (v == btnNonOn) {
            //非接卡寻卡
            ihfService.nCSearch();
            m_RespView.append(ihfService.getResult());
            // 非接卡随机数
        } else if (v == btnNonRandom) {
            ihfService.random();
            m_RespView.append(ihfService.getResult());
        }
    }
}
