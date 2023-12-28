package com.dds.gles.camera;

public interface CameraVideoCapturer extends VideoCapturer {

    public interface CameraEventsHandler {
        // Camera error handler - invoked when camera can not be opened
        // or any camera exception happens on camera thread.
        void onCameraError(String errorDescription);

        // Called when camera is disconnected.
        void onCameraDisconnected();

        // Invoked when camera stops receiving frames.
        void onCameraFreezed(String errorDescription);

        // Callback invoked when camera is opening.
        void onCameraOpening(String cameraName);

        // Callback invoked when first camera frame is available after camera is started.
        void onFirstFrameAvailable();

        // Callback invoked when camera is closed.
        void onCameraClosed();
    }

    public interface CameraSwitchHandler {
        // Invoked on success. `isFrontCamera` is true if the new camera is front facing.
        void onCameraSwitchDone(boolean isFrontCamera);

        // Invoked on failure, e.g. camera is stopped or only one camera available.
        void onCameraSwitchError(String errorDescription);
    }


    public void switchCamera(String cameraName, CameraSwitchHandler switchHandler);

    public void switchCamera(CameraSwitchHandler switchHandler);


}
