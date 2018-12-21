package com.speedata.identity_as;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.speedata.libid2.IDInfor;
import com.speedata.libid2.IDManager;
import com.speedata.libid2.IID2Service;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private TextView tvIDInfor;
    private ImageView imgPic;

    private Button mHf;
    private ToggleButton btnGet;
    private TextView tvMsg;

    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PlaySoundUtils.initSoundPool(this);
        initUI();
        initID();

    }


    private ImageView imageView;
    private TextView tvTime;

    private void initUI() {
        setContentView(R.layout.activity_main);
        tvTime = findViewById(R.id.tv_time);
        imageView = findViewById(R.id.img_logo);
        tvMsg = findViewById(R.id.tv_msg);
        tvIDInfor = findViewById(R.id.tv_idinfor);
        imgPic = findViewById(R.id.img_pic);
        mHf = findViewById(R.id.btn_hf);
        mHf.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HfActivity.class));
            finish();
        });
        btnGet = findViewById(R.id.btn_get);
        btnGet.setOnCheckedChangeListener((compoundButton, b) -> {
            iid2Service.getIDInfor(false, b);
            if (b) {
                startTime = System.currentTimeMillis();
                MyAnimation.showLogoAnimation(MainActivity.this, imageView);
            } else {
                imageView.clearAnimation();
            }
        });

    }

    private IID2Service iid2Service;


    private void initID() {
        iid2Service = IDManager.getInstance();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在初始化");
        progressDialog.show();
        new Thread(() -> {
            try {
                final boolean result = iid2Service.initDev(MainActivity.this, infor -> {
                    Message message = new Message();
                    message.obj = infor;
                    handler.sendMessage(message);
                });

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    if (!result) {
                        new AlertDialog.Builder(MainActivity.this).setCancelable(false)
                                .setMessage("二代证模块初始化失败")
                                .setPositiveButton("确定", (dialogInterface, i) -> {
                                    btnGet.setEnabled(false);
                                    mHf.setVisibility(View.INVISIBLE);
                                    finish();
                                }).show();
                    } else {
                        mHf.setVisibility(View.VISIBLE);
                        showToast();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    /**
     *  成功读取过后   不显示循环读取返回错误信息
     */
    private boolean isShow = false;
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            long leftTime = System.currentTimeMillis() - startTime;
            Log.d("Reginer", "time is: " + leftTime);
            startTime = System.currentTimeMillis();
            iid2Service.getIDInfor(false, btnGet.isChecked());

            IDInfor idInfor1 = (IDInfor) msg.obj;

            if (idInfor1.isSuccess()) {
                isShow = true;
                PlaySoundUtils.play(1, 1);
                tvTime.setText("耗时：" + leftTime + "ms");
                tvIDInfor.setText("姓名:" + idInfor1.getName() + "\n身份证号：" + idInfor1.getNum()
                        + "\n性别：" + idInfor1.getSex()
                        + "\n民族：" + idInfor1.getNation() + "\n住址:"
                        + idInfor1.getAddress() + "\n出生：" + idInfor1.getYear() + "年" + idInfor1
                        .getMonth() + "月" + idInfor1.getDay() + "日" + "\n有效期限：" + idInfor1
                        .getDeadLine());
                System.out.println("id:" + idInfor1.toString());
                Bitmap bmps = idInfor1.getBmps();
                imgPic.setImageBitmap(bmps);
                tvMsg.setText("");
            } else {
                if (!isShow) {
                    tvMsg.setText(String.format("ERROR:%s", idInfor1.getErrorMsg()));
                }
            }
        }
    };

    @SuppressWarnings("unused")
    private Bitmap showfingerbitmap(byte[] image, int width, int height) {
        if (width == 0) { return null; }
        if (height == 0) { return null; }

        int[] rgbbits = new int[width * height];

        for (int i = 0; i < width * height; i++) {
            int v;
            if (image != null) { v = image[i] & 0xff; }
            else { v = 0; }
            rgbbits[i] = Color.rgb(v, v, v);
        }
        return Bitmap.createBitmap(rgbbits, width, height, Bitmap.Config.RGB_565);
    }

    private void showToast() {
        Toast.makeText(this, "初始化成功", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        try {
            iid2Service.releaseDev();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
