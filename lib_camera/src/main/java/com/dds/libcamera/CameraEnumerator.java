package com.dds.libcamera;


public interface CameraEnumerator {

    public String[] getDeviceNames();

    public boolean isFrontFacing(String deviceName);


}
