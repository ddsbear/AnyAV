package com.dds.camera.camera1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadPoolUtil {
    private static ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

}
