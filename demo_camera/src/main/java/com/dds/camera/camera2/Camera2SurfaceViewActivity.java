package com.dds.camera.camera2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dds.base.camera.CameraUtils;
import com.dds.base.camera.ImageSaver;
import com.dds.base.utils.StatueBarUtils;
import com.dds.base.utils.Toasts;
import com.dds.fbo.R;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * SurfaceView + Camera2
 */
public class Camera2SurfaceViewActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    private static final String TAG = "SurfaceViewCamera2Activity";
    private AutoFitSurfaceView mSurfaceView;
    private Button btnSwitch;
    private Button btnPicture;

    private Surface mPreviewSurface;
    private final Size mDesiredPreviewSize = new Size(1920, 1080);
    private ImageReader mImageReader;
    private final Object mCameraLock = new Object();

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CameraCaptureSession mCaptureSession;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private HandlerThread imageReaderThread;
    private Handler imageReaderHandler;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final MediaActionSound mSound = new MediaActionSound();

    // camera
    private String mCameraId;
    private CameraManager manager;
    private CameraCharacteristics characteristics;

    private OrientationLiveData orientationLiveData;

    private final BlockingQueue<CaptureResult> captureResults = new LinkedBlockingDeque<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatueBarUtils.setStatusBarOrScreenStatus(this);
        setContentView(R.layout.activity_surface_view_camera2);
        initView();
        initListener();
        initCameraManager();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        stopBackgroundThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.preview_surface);
        btnSwitch = findViewById(R.id.btn_switch);
        btnPicture = findViewById(R.id.btn_take_picture);
         mSurfaceView.getHolder().addCallback(this);
    }

    private void initListener() {
        btnSwitch.setOnClickListener(v -> switchCamera());
        btnPicture.setOnClickListener(v -> takePicture());
     }

    private void initCameraManager() {
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = manager.getCameraIdList();
            if (cameraIdList.length > 0) {
                initCameraConfig(cameraIdList[0]);
            } else {
                throw new CameraAccessException(CameraAccessException.CAMERA_ERROR, "No camera available");
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "initCameraManager: " + e);
            this.finish();
        }
    }

    private void takePicture() {
        try {
            CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());

            // AF
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            // AE
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // rotation
            Integer rotation = orientationLiveData.getValue();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);

            mCaptureSession.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                    Log.d(TAG, "onCaptureStarted: " + Thread.currentThread().getName());
                    mSound.play(MediaActionSound.SHUTTER_CLICK);
                }

                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    Log.d(TAG, "onCaptureCompleted: " + Thread.currentThread().getName());
                    try {
                        captureResults.put(result);
                    } catch (InterruptedException e) {
                        Log.d(TAG, "onCaptureCompleted: " + e);
                    }
                }
            }, mBackgroundHandler);


        } catch (CameraAccessException e) {
            Log.e(TAG, "takePicture: " + e);
        }


    }

    private void switchCamera() {
        try {
            closeCamera();
            String[] cameraIdList = manager.getCameraIdList();
            List<String> deviceNames = Arrays.asList(cameraIdList);
            int cameraIdIndex = deviceNames.indexOf(mCameraId);
            String cameraId = deviceNames.get((cameraIdIndex + 1) % deviceNames.size());
            initCameraConfig(cameraId);
            mHandler.post(this::openCamera);
        } catch (CameraAccessException e) {
            Log.e(TAG, "switchCamera: " + e);
        }
    }

    private void initCameraConfig(String cameraId) throws CameraAccessException {
        characteristics = manager.getCameraCharacteristics(cameraId);
        orientationLiveData = new OrientationLiveData(this, characteristics);
        orientationLiveData.observe(this, integer -> {
            Log.d(TAG, "orientationLiveData orientation = " + integer);
        });
        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map != null) {
            Size mPreviewSize = CameraUtils.chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), mDesiredPreviewSize);
            mCameraId = cameraId;
            // set layoutSize
            Size bestLayoutSize = CameraUtils.findBestLayoutSize(this, mDesiredPreviewSize);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(bestLayoutSize.getWidth(), bestLayoutSize.getHeight());
            layoutParams.gravity = Gravity.CENTER;
            mSurfaceView.setLayoutParams(layoutParams);
            mSurfaceView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        }

    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

        imageReaderThread = new HandlerThread("imageReaderThread");
        imageReaderThread.start();
        imageReaderHandler = new Handler(imageReaderThread.getLooper());

    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        imageReaderThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            imageReaderThread.join();
            imageReaderThread = null;
            imageReaderHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(Camera2SurfaceViewActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "openCamera: permission denied");
                return;
            }
            synchronized (mCameraLock) {
                Log.d(TAG, "openCamera: start");
                manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            Log.d(TAG, "openCamera: " + e);
        }


    }

    private void closeCamera() {
        try {
            Log.d(TAG, "closeCamera: E");
            synchronized (mCameraLock) {
                if (null != mImageReader) {
                    mImageReader.close();
                    mImageReader = null;
                }
                if (null != mCaptureSession) {
                    mCaptureSession.stopRepeating();
                    mCaptureSession.close();
                    mCaptureSession = null;
                }
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    mCameraDevice = null;
                }
            }
            Log.d(TAG, "closeCamera: X");
        } catch (Exception e) {
            Log.d(TAG, "closeCamera: " + e);

        }

    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            synchronized (mCameraLock) {
                Log.d(TAG, "onOpened: " + cameraDevice.getId());
                // This method is called when the camera is opened.  We start camera preview here.
                mCameraDevice = cameraDevice;
                createCameraPreviewSession();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            synchronized (mCameraLock) {
                Log.d(TAG, "onDisconnected: ");
                cameraDevice.close();
                mCameraDevice = null;
            }


        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            synchronized (mCameraLock) {
                Log.d(TAG, "onError: ");
                cameraDevice.close();
                mCameraDevice = null;
            }


        }
    };

    private void createCameraPreviewSession() {
        try {
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CameraUtils.CompareSizesByArea());
            mImageReader = ImageReader.newInstance(largest.getWidth(), largest.getHeight(),
                    ImageFormat.JPEG, /*maxImages*/2);
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(mPreviewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    synchronized (mCameraLock) {
                        Log.d(TAG, "onConfigured: ");
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
                        } catch (Exception e) {
                            Log.d(TAG, "onConfigured: " + e);
                        }
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, "onConfigureFailed: " + mCameraId);
                    mCaptureSession = null;
                }
            }, null);

            mImageReader.setOnImageAvailableListener(reader -> {
                Log.d(TAG, "onImageAvailable: " + Thread.currentThread().getName());
                Image image = reader.acquireNextImage();
                try {
                    CaptureResult captureResult = captureResults.take();
                    if (image != null && captureResult != null) {
                        String path = CameraUtils.genDCIMCameraPath();
                        File file = new File(path);
                        Log.d(TAG, "takePicture: " + path);
                        imageReaderHandler.post(new ImageSaver(getApplicationContext(), image, file));
                        mHandler.post(() -> Toasts.show(Camera2SurfaceViewActivity.this, "save success: " + path, Toast.LENGTH_SHORT));
                    }
                } catch (Exception e) {
                    Log.d(TAG, "takePicture: " + e);
                }

            }, imageReaderHandler);

        } catch (Exception e) {
            Log.d(TAG, "createCameraPreviewSession: " + e);
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
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mPreviewSurface = holder.getSurface();
        mHandler.post(this::openCamera);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged: size = " + width + "x" + height + ", fmt = " + format);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mPreviewSurface = null;
    }

}