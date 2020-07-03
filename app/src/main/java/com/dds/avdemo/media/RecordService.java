package com.dds.avdemo.media;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;


/**
 * 录屏功能
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class RecordService extends Service {
    private static final String TAG = "RecordService";


    private MediaProjectionManager mMPM;
    private MediaProjection mMpj;
    private VirtualDisplay mVirtualDisplay;


    // 录屏类
    private MediaRecorder mediaRecorder;


    private WindowManager wm;
    private int windowWidth;
    private int windowHeight;
    private int screenDensity;


    // 保存
    private String mDstPath;
    private static final int FRAME_RATE = 60; // 60 fps
    private static final int mBitRate = 6000000; // 6000 kbps


    // 开启服务
    public static void startService(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, RecordService.class);
        context.stopService(intent);
    }

    public RecordService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化MediaProjectionManager
        mMPM = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        // 初始化悬浮窗相关
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        screenDensity = displayMetrics.densityDpi;
        windowWidth = displayMetrics.widthPixels;
        windowHeight = displayMetrics.heightPixels;

        try {
            initRecorder();
            startVirtualDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // 初始化录制类
    private void initRecorder() {
        mDstPath = getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/record";
        File dir = new File(mDstPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mDstPath = mDstPath + "/test.mp4";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(mDstPath);

        mediaRecorder.setVideoSize(windowWidth, windowHeight);
        mediaRecorder.setVideoFrameRate(FRAME_RATE);
        mediaRecorder.setVideoEncodingBitRate(mBitRate);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaRecorder.start();
    }

    public static int mResultCode;
    public static Intent mResultIntent;

    // 创建截屏对象
    private void startVirtualDisplay() {
        if (mMpj == null) {
            mMpj = mMPM.getMediaProjection(mResultCode, mResultIntent);
            mResultCode = 0;
            mResultIntent = null;
        }
        if (mMpj == null) {
            return;
        }
        mVirtualDisplay = mMpj.createVirtualDisplay(
                "record_screen",
                windowWidth,
                windowHeight,
                screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC | DisplayManager.VIRTUAL_DISPLAY_FLAG_PRESENTATION,
                mediaRecorder.getSurface(), null, null
        );

    }


    @Override
    public void onDestroy() {

        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        if (mediaRecorder != null) {
            try {
                mediaRecorder.setOnErrorListener(null);
                mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

        }
        if (mMpj != null) {
            mMpj.stop();
            mMpj = null;
        }

        Log.d(TAG, "onDestroy release");

        super.onDestroy();
    }
}
