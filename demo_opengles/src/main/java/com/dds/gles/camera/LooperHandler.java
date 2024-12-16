package com.dds.gles.camera;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

public class LooperHandler extends Handler {
    public LooperHandler(String name) {
        this(name, Process.THREAD_PRIORITY_BACKGROUND);
    }

    public LooperHandler(String name, int priority) {
        super(generateLooper(name, priority));
    }

    private static Looper generateLooper(String name, int priority) {
        HandlerThread thread = new HandlerThread(name, priority);
        thread.start();
        return thread.getLooper();
    }

    public void quit() {
        getLooper().quit();
    }
}
