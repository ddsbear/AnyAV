package com.dds.base.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraUtils {
    private static final String TAG = "CameraUtils";
    public static final String PRIMARY_STORAGE_PATH = Environment.getExternalStorageDirectory().toString();
    public static final String CAMERA_STORAGE_PATH_SUFFIX = "DCIM/Camera/";
    public static final String JPEG_SUFFIX = ".jpeg";

    public static final String IMG_PREFIX = "IMG_";
    private static int mDumpNum;
    private static String mLastTitle;

    public static String genSaveTitle() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss", Locale.getDefault());
        String titleTrunk = formatter.format(new Date());

        StringBuilder builder = new StringBuilder();
        builder.append(IMG_PREFIX)
                .append(titleTrunk);
        if (titleTrunk.equals(mLastTitle)) {
            mDumpNum++;
            builder.append("_").append(mDumpNum);
        } else {
            mLastTitle = titleTrunk;
            mDumpNum = 0;
        }
        return builder.toString();
    }

    public static String genDCIMCameraPath() {
        return PRIMARY_STORAGE_PATH + File.separator + CAMERA_STORAGE_PATH_SUFFIX + genSaveTitle() + JPEG_SUFFIX;
    }

    public static void saveImage(byte[] data, String savePath) {
        try {
            Log.d(TAG, "save jpeg");
            FileOutputStream fileOut = new FileOutputStream(savePath);
            BufferedOutputStream bufferOut = new BufferedOutputStream(fileOut);
            bufferOut.write(data, 0, data.length);
        } catch (Exception e) {
            Log.e(TAG, "error save " + e.getMessage());
        }
    }

    public static void rotateImageView(int cameraId, int orientation, String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Matrix matrix = new Matrix();
        matrix.postRotate((float) orientation);
        // 创建新的图片
        Bitmap resizedBitmap;
        if (cameraId == 1) {
            if (orientation == 90) {
                matrix.postRotate(180f);
            }
        }
        // 创建新的图片
        resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        //新增 如果是前置 需要镜面翻转处理
        if (cameraId == 1) {
            Matrix matrix1 = new Matrix();
            matrix1.postScale(-1f, 1f);
            resizedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0,
                    resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix1, true);

        }
        File file = new File(path);
        //重新写入文件
        try {
            // 写入文件
            FileOutputStream fos;
            fos = new FileOutputStream(file);
            //默认jpg
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            resizedBitmap.recycle();
        } catch (Exception e) {
            Log.e(TAG, "rotateImageView: " + e);
        }

    }

    public static Size findBestLayoutSize(Context context, Size targetSize) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int displayWidth = metrics.widthPixels;
        int displayHeight = metrics.heightPixels;

        float ratio = (float) targetSize.getWidth() / targetSize.getHeight();
        if (displayHeight > displayWidth) {
            displayHeight = (int) (displayWidth * ratio);
        } else {
            displayWidth = (int) (displayHeight * ratio);
        }
        return new Size(displayWidth, displayHeight);
    }


    /**
     * Choose the most appropriate size
     * larger than or equal to the desired size
     * If you don't find a suitable one, use the first one.
     *
     * @param choices     choices
     * @param desiredSize desiredSize
     * @return OptimalSize
     */
    public static Size chooseOptimalSize(Size[] choices, Size desiredSize) {
        List<Size> bigEnough = new ArrayList<>();
        int w = desiredSize.getWidth();
        int h = desiredSize.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w && option.getWidth() >= w && option.getHeight() >= h) {
                bigEnough.add(option);
            }
        }
        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }


}
