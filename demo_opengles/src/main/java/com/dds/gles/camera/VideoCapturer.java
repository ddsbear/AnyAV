package com.dds.gles.camera;


public interface VideoCapturer {
    void initialize();

    void startCapture(int width, int height, int frameRate);

    void stopCapture() throws InterruptedException;

    void dispose();

}
