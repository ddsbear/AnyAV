package com.dds.gles.camera;


public interface CameraEnumerator {

    public String[] getDeviceNames();

    public boolean isFrontFacing(String deviceName);


}
