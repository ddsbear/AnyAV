package com.dds.camera.camera1;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dds.camera.camera2.Camera2SurfaceViewActivity;
import com.dds.camera.camera2.view.AutoFitSurfaceView;
import com.dds.camera.camera2.utils.OrientationLiveData;
import com.dds.fbo.R;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Camera1SurfaceViewActivity extends AppCompatActivity {

    private AutoFitSurfaceView mSurfaceView;
    private CameraPresenter cameraPresenter;

    private Button btnSwitch;
    private Button btnPicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusBarOrScreenStatus(this);
        setContentView(R.layout.activity_camera1_surface_view);
        mSurfaceView = findViewById(R.id.preview_surface);
        cameraPresenter = new CameraPresenter(this, mSurfaceView);
        btnSwitch = findViewById(R.id.btn_switch);
        btnPicture = findViewById(R.id.btn_take_picture);
        initListener();
    }

    private void initListener() {
        btnSwitch.setOnClickListener(v -> switchCamera());
        btnPicture.setOnClickListener(v -> takePicture());
    }

    private void takePicture() {
        cameraPresenter.takePicture(90);
    }

    private void switchCamera() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraPresenter != null) {
            cameraPresenter.releaseCamera();
        }
    }

    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        return flags;
    }

    public void setStatusBarOrScreenStatus(Activity activity) {
        Window window = activity.getWindow();
        //全屏+锁屏+常亮显示
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
            layoutParams.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(layoutParams);
        }
        // 5.0以上系统状态栏透明
        //清除透明状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //设置状态栏颜色必须添加
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);//设置透明
    }

}