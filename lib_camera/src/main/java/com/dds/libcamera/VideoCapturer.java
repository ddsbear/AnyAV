package com.dds.libcamera;


public interface VideoCapturer {

    void initialize();

    void startCapture(int width, int height, int frameRate);

    void stopCapture() throws InterruptedException;

    void dispose();

}
