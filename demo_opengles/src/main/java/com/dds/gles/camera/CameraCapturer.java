package com.dds.gles.camera;


import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

public abstract class CameraCapturer implements CameraVideoCapturer {

    private final Object stateLock = new Object();
    private boolean sessionOpening;

    private int mWidth;
    private int mHeight;
    private int mFrameRate;


    private final CameraEnumerator mCameraEnumerator;

    private String mCameraName;

    public CameraCapturer(String cameraName, CameraEnumerator enumerator) {
        mCameraEnumerator = enumerator;
        mCameraName = cameraName;
    }

    @Override
    public void switchCamera(CameraSwitchHandler switchHandler) {
        List<String> deviceNames = Arrays.asList(mCameraEnumerator.getDeviceNames());
        if (deviceNames.size() < 2) {
            reportCameraSwitchError("No camera to switch to.", switchHandler);
            return;
        }
        int cameraNameIndex = deviceNames.indexOf(mCameraName);
        String cameraName = deviceNames.get((cameraNameIndex + 1) % deviceNames.size());
        switchCameraInternal(switchHandler, cameraName);
    }

    @Override
    public void switchCamera(String cameraName, CameraSwitchHandler switchHandler) {

        switchCameraInternal(switchHandler, cameraName);
    }

    private void switchCameraInternal(CameraSwitchHandler switchHandler, String cameraName) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public void startCapture(int width, int height, int frameRate) {
        this.mWidth = width;
        this.mHeight = height;
        this.mFrameRate = frameRate;
        createCameraSession();

    }

    @Override
    public void stopCapture() throws InterruptedException {

    }

    @Override
    public void dispose() {

    }


    private void reportCameraSwitchError(String error, @Nullable CameraSwitchHandler switchEventsHandler) {
        if (switchEventsHandler != null) {
            switchEventsHandler.onCameraSwitchError(error);
        }
    }

    abstract protected void createCameraSession();

}
