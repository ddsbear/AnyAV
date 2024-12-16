package com.dds.gles.demo2;

import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dds.base.camera.CameraUtils;
import com.dds.base.utils.StatueBarUtils;
import com.dds.gles.R;
import com.dds.gles.camera.Camera2Manager;
import com.dds.gles.demo2.render.RenderManager;
import com.dds.gles.demo2.view.AutoFitSurfaceView;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class GLCamera2SurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "dds_RenderActivity";
    private AutoFitSurfaceView mSurfaceView;
    private Surface mPreviewSurface;

    private final Size mDesiredPreviewSize = new Size(1280, 720);

    private RenderManager mRenderManager;

    private boolean isFilterEnable;
    private boolean isBeautyEnable;

    // camera
    Camera2Manager mCamera2Manager;
    DeviceOrientationLiveData deviceOrientationLiveData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
        setContentView(R.layout.activity_gl_camera2_surface_view);
        Log.d(TAG, "onCreate: ");
        initView();

        mRenderManager = new RenderManager();
        mCamera2Manager = new Camera2Manager(this);
        deviceOrientationLiveData = new DeviceOrientationLiveData(this);
        deviceOrientationLiveData.observe(this, deviceOrientation -> {
            Log.d(TAG, "deviceOrientationLiveData : deviceOrientation = " + deviceOrientation);
        });

        int displayRotation = CameraUtils.getDisplayRotation(this);
        Log.d(TAG, "onCreate : displayRotation = " + displayRotation);
        mRenderManager.setDeviceRotation(displayRotation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        Camera2Manager.doOrPost(() -> {
            String defaultCameraId = mCamera2Manager.getDefaultCameraId();
            mCamera2Manager.initCamera(defaultCameraId, mDesiredPreviewSize);
            configOutPutSurface();
            mCamera2Manager.openCamera();
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        mCamera2Manager.closeCamera();
        mRenderManager.destroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int displayRotation = CameraUtils.getDisplayRotation(this);
        Log.d(TAG, "onConfigurationChanged : displayRotation = " + displayRotation);
        mRenderManager.setDeviceRotation(displayRotation);
        mSurfaceView.post(() -> {
            resizeSurfaceView(mSurfaceView);
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_filter) {
            handleFilter();
        } else if (id == R.id.btn_beauty) {
            handleBeauty();
        } else if (id == R.id.btn_take_switch) {
            handSwitch();
        }
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surface_container);
        mSurfaceView.getHolder().addCallback(this);
        resizeSurfaceView(mSurfaceView);
    }

    private void handSwitch() {
        String nextCameraId = mCamera2Manager.getNextCameraId();
        if (nextCameraId != null) {
            mCamera2Manager.initCamera(nextCameraId, mDesiredPreviewSize);
            configOutPutSurface();
            mCamera2Manager.switchCamera();
        }
    }

    private void handleFilter() {
        mRenderManager.enableFilter(isFilterEnable = !isFilterEnable);
    }

    private void handleBeauty() {
        mRenderManager.enableBeauty(isBeautyEnable = !isBeautyEnable);
    }

    private void setUpOutputSurfaces(int width, int height) {
        Log.d(TAG, "setUpOutputSurfaces: preview width = " + width + ",height = " + height);
        mRenderManager.setup(width, height);
        mRenderManager.startPreview(mPreviewSurface);
    }

    private void configOutPutSurface() {
        CompletableFuture<SurfaceTexture> completableFuture = mRenderManager.getSurfaceTexture();
        SurfaceTexture surfaceTexture = null;
        try {
            surfaceTexture = completableFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.d(TAG, "future.get: " + e);
        }
        if (surfaceTexture == null) {
            Log.d(TAG, "surfaceTexture == null");
            return;
        }
        Size previewSize = mCamera2Manager.getPreviewSize();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
        surfaceTexture.setOnFrameAvailableListener(this);
        Surface surface = new Surface(surfaceTexture);
        mCamera2Manager.setSurfaceFuture(surface);

    }

    private void resizeSurfaceView(View surfaceView) {
        Size layoutSize = CameraUtils.findBestLayoutSize(this, mDesiredPreviewSize);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) surfaceView.getLayoutParams();
        params.width = layoutSize.getWidth();
        params.height = layoutSize.getHeight();
        params.gravity = Gravity.CENTER;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mRenderManager.drawFrame();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: size = " + width + "x" + height + ", fmt = " + format);
        if (mPreviewSurface != holder.getSurface()) {
            mPreviewSurface = holder.getSurface();
            setUpOutputSurfaces(width, height);
        } else {
            mRenderManager.setSurfaceSize(width, height);
        }

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mPreviewSurface = null;
    }


}