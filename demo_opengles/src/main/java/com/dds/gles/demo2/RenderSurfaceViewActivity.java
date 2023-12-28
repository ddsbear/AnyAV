package com.dds.gles.demo2;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dds.base.camera.CameraUtils;
import com.dds.base.utils.StatueBarUtils;
import com.dds.gles.R;
import com.dds.gles.demo2.render.GLESTool;
import com.dds.gles.demo2.render.RenderManager;
import com.dds.gles.demo2.view.AutoFitSurfaceView;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class RenderSurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "RenderActivity";
    private AutoFitSurfaceView mSurfaceView;
    private Surface mPreviewSurface;

    private final Size mDesiredPreviewSize = new Size(1280, 720);
    private Size mPreviewSize;

    private int mPreviewSurfaceWidth;
    private int mPreviewSurfaceHeight;

    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    // camera
    private String mCameraId;
    private CameraManager manager;

    private OrientationLiveData orientationLiveData;

    private RenderManager mRenderManager;

    private boolean isConfigOrientated;

    private boolean isFilterEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
        if (!Utils.isTablet(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        setContentView(R.layout.activity_render);
        Log.d(TAG, "onCreate: ");
        initView();
        initListener();

        mRenderManager = new RenderManager();
        initCameraManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
        closeCamera();
        stopBackgroundThread();

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
        Log.d(TAG, "onConfigurationChanged: ");

        Size layoutSize = CameraUtils.findBestLayoutSize(this, mPreviewSize);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        params.width = (int) layoutSize.getWidth();
        params.height = (int) layoutSize.getHeight();
        params.gravity = Gravity.CENTER;


        if (Utils.isTablet(this)) {
            Integer dataValue = orientationLiveData.getValue();
            if (dataValue != null) {
                mRenderManager.setRotation(dataValue);
            }
        }

    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_filter) {
            handleFilter();
        }
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surface_container);
        mSurfaceView.getHolder().addCallback(this);
    }

    private void initListener() {

    }

    private void initCameraManager() {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = manager.getCameraIdList();
            if (cameraIdList.length > 0) {
                initCameraConfig(cameraIdList[1]);
            } else {
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "No camera available");
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "initCameraManager: " + e);
            this.finish();
        }
    }

    private void initCameraConfig(String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            mPreviewSize = CameraUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), mDesiredPreviewSize);
            Log.d(TAG, "initCameraConfig chooseOptimalSize: width = " + mPreviewSize.getWidth() + ",height = " + mPreviewSize.getHeight());
            mCameraId = cameraId;
            mSurfaceView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Size layoutSize = CameraUtils.findBestLayoutSize(this, mPreviewSize);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
            params.width = (int) layoutSize.getWidth();
            params.height = (int) layoutSize.getHeight();
            params.gravity = Gravity.CENTER;
            orientationLiveData = new OrientationLiveData(this, characteristics);
            orientationLiveData.observe(this, integer -> {
                Log.d(TAG, "orientationLiveData orientation = " + integer);
                if (!isConfigOrientated) {
                    mRenderManager.setRotation(integer);
                    isConfigOrientated = true;
                }
            });
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());


    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setUpOutputSurfaces() {
        Log.d(TAG, "setUpOutputSurfaces: preview width = " + mPreviewSize.getWidth() + ",height = " + mPreviewSize.getHeight());
        mRenderManager.setup(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mRenderManager.startPreview(mPreviewSurface);
    }

    private void openCamera() {
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(RenderSurfaceViewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }

    }

    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.stopRepeating();
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "closeCamera: " + e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void handleFilter() {
        if (!isFilterEnable) {
            isFilterEnable = true;
            mRenderManager.enableFilter(true);
        } else {
            isFilterEnable = false;
            mRenderManager.enableFilter(false);
        }

    }


    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.d(TAG, "Camera onOpened: " + cameraDevice.getId());
            // This method is called when the camera is opened.  We start camera preview here.
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.d(TAG, "Camera onDisconnected: " + cameraDevice.getId());
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            Log.d(TAG, "Camera onError: " + cameraDevice.getId() + ",error = " + error);
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();
        }
    };

    private void createCameraPreviewSession() {
        try {
            Log.d(TAG, "createCameraPreviewSession: ");
            CompletableFuture<SurfaceTexture> future = mRenderManager.getSurfaceTexture();
            SurfaceTexture texture = future.get();
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            texture.setOnFrameAvailableListener(this);
            Surface surface = new Surface(texture);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, "Camera onConfigured: ");
                    if (null == mCameraDevice) {
                        return;
                    }
                    mCaptureSession = session;
                    // Auto focus
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    // Auto Flash
                    mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                    CaptureRequest captureRequest = mPreviewRequestBuilder.build();
                    try {
                        mCaptureSession.setRepeatingRequest(captureRequest, mCaptureCallback, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }
                    if (Utils.isTablet(RenderSurfaceViewActivity.this) && orientationLiveData.getValue() != null) {
                        mRenderManager.setRotation(orientationLiveData.getValue());
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, "Camera onConfigureFailed: " + mCameraId);

                }
            }, null);

        } catch (CameraAccessException | ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {

        private void process(CaptureResult result) {

        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request,
                                        CaptureResult partialResult) {
            process(partialResult);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request,
                                       TotalCaptureResult result) {
            process(result);
        }
    };

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mRenderManager.drawFrame();
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
        mPreviewSurface = holder.getSurface();
        setUpOutputSurfaces();
        openCamera();

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: size = " + width + "x" + height + ", fmt = " + format);
        mPreviewSurfaceWidth = width;
        mPreviewSurfaceHeight = height;
        mRenderManager.setResolution(mPreviewSurfaceWidth, mPreviewSurfaceHeight);


    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mPreviewSurface = null;
    }


}