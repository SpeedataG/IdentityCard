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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.speedata.identity_as.utils.ProgressUtils;
import com.speedata.identity_as.utils.ToastUtils;
import com.speedata.libid2.HFManager;
import com.speedata.libid2.IHFService;

import java.io.IOException;

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
    private static String TAG = "function_DEV";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hf);
        initView();

        PlaySoundUtils.initSoundPool(this);
        initDev();
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
        ProgressUtils.dismissProgressDialog();
        ihfService.releaseDev();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        // 返回
        if (v == btnBack) {
            // 退出读卡线程
            ihfService.releaseDev();
            finish();
        } else if (v == btnSure) {
            String str = m_CmdEdit.getText().toString();
            ProgressUtils.showProgressDialog(HfActivity.this, "请稍候...");
            int q = ihfService.execfasongCmd(str);
            ProgressUtils.dismissProgressDialog();
            if (q == -1) {
                m_RespView.append("输入有问题\n");
            } else if (q == 0) {
                m_RespView.append(ihfService.getResult() + "\n");
            }
            // 清空显示窗口
        } else if (v == btnClearWindow) {
            m_RespView.setText(null);
            // 安全模块号
        } else if (v == btnSafecode) {
            ProgressUtils.showProgressDialog(HfActivity.this, "请稍候...");
            ihfService.readSamID();
            ProgressUtils.dismissProgressDialog();
            m_RespView.append(ihfService.getSafeResult());
            // 非接卡寻卡
        } else if (v == btnNonOn) {
            //非接卡寻卡
            ProgressUtils.showProgressDialog(HfActivity.this, "请稍候...");
            ihfService.nCSearch();
            ProgressUtils.dismissProgressDialog();
            String all = ihfService.getResult();
            Log.d(TAG, "all:" + all);
            m_RespView.append( "寻卡:\n");
            if (all.startsWith("提示")) {
                m_RespView.append(all + "\n");
            } else {
                String card = all.substring(2, 4);
                Log.d(TAG, "card:" + card);
                int icard = Integer.parseInt(card, 16);
                Log.d(TAG, "icard:" + icard);
                int cardEnd = 4 + icard * 2;
                String cardId = all.substring(4, cardEnd);
                Log.d(TAG, "cardId:" + cardId);
                m_RespView.append("卡号:" + cardId + "\n");
                String read = all.substring(cardEnd, cardEnd + 2);
                Log.d(TAG, "read:" + read);
                int iread = Integer.parseInt(read, 16);
                Log.d(TAG, "iread:" + iread);
                if (iread != 0) {
                    String mRead = all.substring(cardEnd + 2, cardEnd + 2 + iread * 2);
                    Log.d(TAG, "mRead:" + mRead);
                    m_RespView.append("读卡:" + mRead + "\n");
                }

            }
            // 非接卡随机数
        } else if (v == btnNonRandom) {
            ProgressUtils.showProgressDialog(HfActivity.this, "请稍候...");
            ihfService.random();
            ProgressUtils.dismissProgressDialog();
            m_RespView.append(ihfService.getResult() + "\n");
        }
    }
}
