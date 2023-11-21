package com.dds.camera.camera1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dds.base.camera.CameraUtils;
import com.dds.base.utils.ThreadPoolUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraPresenter implements SurfaceHolder.Callback, Camera.PreviewCallback, TextureView.SurfaceTextureListener {

    private static final String TAG = "CameraPresenter";
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private TextureView mTextureView;

    private SurfaceTexture mSurfaceTexture;

    private Activity mContext;

    private final int screenWidth;
    private final int screenHeight;

    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private Camera.Parameters mParameters;
    private CameraCallBack mCameraCallBack;

    private int orientation;

    public CameraPresenter(Activity context, SurfaceView surfaceView, Size mDesiredPreviewSize) {
        mContext = context;
        this.mSurfaceView = surfaceView;
        mSurfaceHolder = mSurfaceView.getHolder();
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        //获取宽高像素
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        mSurfaceHolder.addCallback(this);

        Size bestLayoutSize = CameraUtils.findBestLayoutSize(context, mDesiredPreviewSize);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(bestLayoutSize.getWidth(), bestLayoutSize.getHeight());
        layoutParams.gravity = Gravity.CENTER;
        mSurfaceView.setLayoutParams(layoutParams);
    }

    public CameraPresenter(Activity context, TextureView textureView, Size mDesiredPreviewSize) {
        mContext = context;
        this.mTextureView = textureView;
        mTextureView.setSurfaceTextureListener(this);

        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        //获取宽高像素
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;


        Size bestLayoutSize = CameraUtils.findBestLayoutSize(context, mDesiredPreviewSize);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(bestLayoutSize.getWidth(), bestLayoutSize.getHeight());
        layoutParams.gravity = Gravity.CENTER;
        mTextureView.setLayoutParams(layoutParams);
    }

    public void setCameraCallBack(CameraCallBack mCameraCallBack) {
        this.mCameraCallBack = mCameraCallBack;

    }

    public void takePicture(final int takePhotoOrientation) {
        if (mCamera != null) {
            mCamera.takePicture(null, null, (data, camera) -> {
                // reset preview
                mCamera.startPreview();
                // callback data
                if (mCameraCallBack != null) {
                    mCameraCallBack.onTakePicture(data, camera);
                }
                // save jpg
                ThreadPoolUtil.executeByCached(new ThreadPoolUtil.SimpleTask<String>() {
                    @Override
                    public String doInBackground() {
                        String path = CameraUtils.genDCIMCameraPath();
                        // save image
                        CameraUtils.saveImage(data, path);
                        // rotate image
                        CameraUtils.rotateImageView(mCameraId, takePhotoOrientation, path);
                        return path;
                    }

                    @Override
                    public void onSuccess(String result) {
                        Toast.makeText(mContext, "save success:" + result, Toast.LENGTH_SHORT).show();
                    }
                });


            });
        }
    }

    public void switchCamera() {
        releaseCamera();
        //在Android P之前 Android设备仍然最多只有前后两个摄像头，在Android p后支持多个摄像头 用户想打开哪个就打开哪个
        mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras();
        openCamera(mCameraId);
        startPreview();
    }

    public void releaseCamera() {
        if (mCamera != null) {
            //停止预览
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    // region -------------------------SurfaceView callback-----------------------------------
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (mCamera == null) {
            openCamera(mCameraId);
        }
        startPreview();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        releaseCamera();
    }

    //endregion


    // region -------------------------TextureView callback-----------------------------------

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
        if (mCamera == null) {
            openCamera(mCameraId);
        }
        mSurfaceTexture = surface;
        startPreview();

    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        mSurfaceTexture = null;
        releaseCamera();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }

    //endregion

    private void openCamera(int mCameraId) {
        boolean isSupportCamera = isSupport(mCameraId);
        if (isSupportCamera) {
            mCamera = Camera.open(mCameraId);
            initParameters(mCamera);
            if (mCamera != null) {
                mCamera.setPreviewCallback(this);
            }
        }

    }

    public void startPreview() {
        try {
            //根据所传入的SurfaceHolder对象来设置实时预览
            if (mSurfaceHolder != null) {
                mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                mCamera.setPreviewDisplay(mSurfaceHolder);
            }
            if (mSurfaceTexture != null) {
                mCamera.setPreviewTexture(mSurfaceTexture);
            }

            //调整预览角度
            setCameraDisplayOrientation(mContext, mCameraId, mCamera);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCameraDisplayOrientation(Activity appCompatActivity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        //rotation是预览Window的旋转方向，对于手机而言，当在清单文件设置Activity的screenOrientation="portait"时，
        //rotation=0，这时候没有旋转，当screenOrientation="landScape"时，rotation=1。
        int rotation = appCompatActivity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
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
        //计算图像所要旋转的角度
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        orientation = result;
        //调整预览图像旋转角度
        camera.setDisplayOrientation(orientation);

    }

    private void initParameters(Camera mCamera) {
        //获取Parameters对象
        mParameters = mCamera.getParameters();
        //设置预览格式
        mParameters.setPreviewFormat(ImageFormat.NV21);
        if (mSurfaceView != null) {
            setPreviewSize(mSurfaceView.getMeasuredWidth(), mSurfaceView.getMeasuredHeight());
        }

        if (mTextureView != null) {
            setPreviewSize(mTextureView.getMeasuredWidth(), mTextureView.getMeasuredHeight());
        }

        setPictureSize();
        //连续自动对焦图像
        if (isSupportFocus(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (isSupportFocus(Camera.Parameters.FOCUS_MODE_AUTO)) {
            //自动对焦(单次)
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(mParameters);
    }

    private boolean isSupport(int faceOrBack) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            //返回相机信息
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == faceOrBack) {
                return true;
            }
        }
        return false;
    }

    public void setPreviewSize(int width, int height) {
        //获取系统支持预览大小
        List<Camera.Size> localSizes = mParameters.getSupportedPreviewSizes();
        Camera.Size biggestSize = null;//最大分辨率
        Camera.Size fitSize = null;// 优先选屏幕分辨率
        Camera.Size targetSize = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(大)的size
        Camera.Size targetSiz2 = null;// 没有屏幕分辨率就取跟屏幕分辨率相近(小)的size
        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            // 如果是 预览窗口宽：高 ==  3：4的话
            if ((float) width / height == 3.0f / 4) {
                for (int n = 0; n < cameraSizeLength; n++) {
                    Camera.Size size = localSizes.get(n);
                    if ((float) size.width / size.height == 4.0f / 3) {
                        mParameters.setPreviewSize(size.width, size.height);
                        break;
                    }
                }
            } else if ((float) width / height == 9.0f / 16) {
                for (int n = 0; n < cameraSizeLength; n++) {
                    Camera.Size size = localSizes.get(n);
                    if ((float) size.width / size.height == 16.0f / 9) {
                        mParameters.setPreviewSize(size.width, size.height);
                        break;
                    }
                }
            } else {
                //全屏幕预览
                for (int n = 0; n < cameraSizeLength; n++) {
                    Camera.Size size = localSizes.get(n);
                    if (biggestSize == null ||
                            (size.width >= biggestSize.width && size.height >= biggestSize.height)) {
                        biggestSize = size;
                    }

                    //如果支持的比例都等于所获取到的宽高
                    if (size.width == height
                            && size.height == width) {
                        fitSize = size;
                        //如果任一宽或者高等于所支持的尺寸
                    } else if (size.width == height
                            || size.height == width) {
                        if (targetSize == null) {
                            targetSize = size;
                            //如果上面条件都不成立 如果任一宽高小于所支持的尺寸
                        } else if (size.width < height
                                || size.height < width) {
                            targetSiz2 = size;
                        }
                    }
                }

                if (fitSize == null) {
                    fitSize = targetSize;
                }

                if (fitSize == null) {
                    fitSize = targetSiz2;
                }

                if (fitSize == null) {
                    fitSize = biggestSize;
                }
                mParameters.setPreviewSize(fitSize.width, fitSize.height);
                fixScreenSize(fitSize.height, fitSize.width);
            }

        }


    }

    private void setPictureSize() {
        List<Camera.Size> localSizes = mParameters.getSupportedPictureSizes();
        Camera.Size biggestSize = null;
        Camera.Size fitSize = null;// 优先选预览界面的尺寸
        Camera.Size previewSize = mParameters.getPreviewSize();//获取预览界面尺寸
        float previewSizeScale = 0;
        if (previewSize != null) {
            previewSizeScale = previewSize.width / (float) previewSize.height;
        }

        if (localSizes != null) {
            int cameraSizeLength = localSizes.size();
            for (int n = 0; n < cameraSizeLength; n++) {
                Camera.Size size = localSizes.get(n);
                if (biggestSize == null) {
                    biggestSize = size;
                } else if (size.width >= biggestSize.width && size.height >= biggestSize.height) {
                    biggestSize = size;
                }

                // 选出与预览界面等比的最高分辨率
                if (previewSizeScale > 0
                        && size.width >= previewSize.width && size.height >= previewSize.height) {
                    float sizeScale = size.width / (float) size.height;
                    if (sizeScale == previewSizeScale) {
                        if (fitSize == null) {
                            fitSize = size;
                        } else if (size.width >= fitSize.width && size.height >= fitSize.height) {
                            fitSize = size;
                        }
                    }
                }
            }

            // 如果没有选出fitSize, 那么最大的Size就是FitSize
            if (fitSize == null) {
                fitSize = biggestSize;
            }
            mParameters.setPictureSize(fitSize.width, fitSize.height);
        }

    }

    private boolean isSupportFocus(String focusMode) {
        boolean isSupport = false;
        //获取所支持对焦模式
        List<String> listFocus = mParameters.getSupportedFocusModes();
        for (String s : listFocus) {
            //如果存在 返回true
            if (s.equals(focusMode)) {
                isSupport = true;
            }

        }
        return isSupport;
    }


    private void fixScreenSize(int fitSizeHeight, int fitSizeWidth) {
        // 预览 View 的大小，比如 SurfaceView
        int viewHeight = screenHeight;
        int viewWidth = screenWidth;
        // 相机选择的预览尺寸
        int cameraHeight = fitSizeWidth;
        int cameraWidth = fitSizeHeight;
        // 计算出将相机的尺寸 => View 的尺寸需要的缩放倍数
        float ratioPreview = (float) cameraWidth / cameraHeight;
        float ratioView = (float) viewWidth / viewHeight;
        float scaleX, scaleY;
        if (ratioView < ratioPreview) {
            scaleX = ratioPreview / ratioView;
            scaleY = 1;
        } else {
            scaleX = 1;
            scaleY = ratioView / ratioPreview;
        }
        // 计算出 View 的偏移量
        float scaledWidth = viewWidth * scaleX;
        float scaledHeight = viewHeight * scaleY;
        float dx = (viewWidth - scaledWidth) / 2;
        float dy = (viewHeight - scaledHeight) / 2;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleX, scaleY);
        matrix.postTranslate(dx, dy);

        float[] values = new float[9];
        matrix.getValues(values);

        if (mSurfaceView != null) {
            mSurfaceView.setTranslationX(values[Matrix.MTRANS_X]);
            mSurfaceView.setTranslationY(values[Matrix.MTRANS_Y]);
            mSurfaceView.setScaleX(values[Matrix.MSCALE_X]);
            mSurfaceView.setScaleY(values[Matrix.MSCALE_Y]);
            mSurfaceView.invalidate();
        }
        if (mTextureView != null) {
            mTextureView.setTranslationX(values[Matrix.MTRANS_X]);
            mTextureView.setTranslationY(values[Matrix.MTRANS_Y]);
            mTextureView.setScaleX(values[Matrix.MSCALE_X]);
            mTextureView.setScaleY(values[Matrix.MSCALE_Y]);
            mTextureView.invalidate();
        }

    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mCameraCallBack != null) {
            mCameraCallBack.onPreviewFrame(data, camera);
        }
    }


    public interface CameraCallBack {

        void onPreviewFrame(byte[] data, Camera camera);

        void onTakePicture(byte[] data, Camera Camera);

    }
}
