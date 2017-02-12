package com.speedata.face;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * Created by brxu on 2017/1/6.
 */

public class MyCamera implements SurfaceHolder.Callback {
    private Context mContext;

    private boolean isFront = false;
    private SurfaceHolder holder;//surfaceHolder声明
    private Activity activity;

    public MyCamera(Activity activity, Context mContext, SurfaceView surfaceView, boolean isFront) {
        this.mContext = mContext;
        this.isFront = isFront;
        this.activity = activity;
        //获得句柄
        holder = surfaceView.getHolder();
        //添加回调
        holder.addCallback(this);
        //设置类型
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void preview(){
//        initCamera();
        myCamera.startPreview();
    }
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        initCamera();
    }
    public void releaseCamera() {
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
    }

    private void initCamera() {
        if (myCamera != null) {
            myCamera.stopPreview();
            myCamera.release();
            myCamera = null;
        }
//        } else {
        cammeraIndex = FindFrontCamera();
        if (cammeraIndex == -1) {
            showToast("无前置摄像头");
            return;
        } else {
            try {
                myCamera = Camera.open(cammeraIndex);
                myCamera.stopPreview();
                setCameraDisplayOrientation(1, myCamera);//前置设置预览方向,
                //设置显示
                myCamera.setPreviewDisplay(holder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        }
    }

    private int cammeraIndex;
    private Camera myCamera = null;//相机声明

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (cammeraIndex == -1) {
            showToast("无前置摄像头");
            return;
        }
        //设置参数并开始预览
        Camera.Parameters params = myCamera.getParameters();
        params.setPictureFormat(PixelFormat.JPEG);
        params.setPreviewSize(640, 480);
        // 设置预览照片时每秒显示多少帧的最小值和最大值
        params.setPreviewFpsRange(4, 10);
        // 设置图片格式
        params.setPictureFormat(ImageFormat.JPEG);
        // 设置JPG照片的质量
        params.set("jpeg-quality", 85);
        myCamera.setParameters(params);
        myCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
        }
    };

    public void takePic(Camera.PictureCallback jpeg) {
        myCamera.startPreview();
        myCamera.takePicture(shutterCallback, null, jpeg);
    }
    public void takePreview(int cammeraIndex) {
        try {
            myCamera = Camera.open(cammeraIndex);
            myCamera.stopPreview();
//            setCameraDisplayOrientation(1, myCamera);//设置预览方向,
            setCameraDisplayOrientation(0, myCamera);//后置设置预览方向,
            //设置显示
            myCamera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int FindFrontCamera() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
            int cameraFacingFront;
//            cameraFacingFront = Camera.CameraInfo.CAMERA_FACING_FRONT;
            cameraFacingFront = Camera.CameraInfo.CAMERA_FACING_BACK;
            if (cameraInfo.facing == cameraFacingFront) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;//无前置摄像头
    }

    /*
    设置方向
     */
    public void setCameraDisplayOrientation(int cameraId, android
            .hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 180; //后置为180  前置为0
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;   // compensate the mirror
        } else {
            // back-facing
            result = (info.orientation - degrees + 180) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
    }

    public void setState(boolean isFront) {
        this.isFront = isFront;
        initCamera();
    }

}
