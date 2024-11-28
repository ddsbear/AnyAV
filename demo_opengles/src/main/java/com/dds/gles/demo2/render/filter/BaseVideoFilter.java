package com.dds.gles.demo2.render.filter;

import com.dds.gles.render.GlFrameBuffer;

public class BaseVideoFilter {
    int mWidth;
    int mHeight;

    public void onInit(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public void onDraw(final int cameraTexture, GlFrameBuffer mFrameBuffer) {

    }

    public void onDestroy() {
    }

    protected void onPreDraw() {
        // ignore
    }

    protected void onAfterDraw() {
        // ignore
    }


}
