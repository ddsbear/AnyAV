package com.dds.gles.demo3.render;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import com.dds.gles.demo1.render.camera.CameraPreViewRenderer;

public class RenderManager {
    private static final String TAG = "RenderManager";
    private HandlerThread mGLHandlerThread;
    private GLHandler mHandler;

    private final Object syncOp = new Object();

    public RenderManager() {
        mGLHandlerThread = new HandlerThread(TAG);
    }

    public void prepare() {
        synchronized (syncOp) {
            mGLHandlerThread.start();
            mHandler = new GLHandler(mGLHandlerThread.getLooper());
            mHandler.sendEmptyMessage(GLHandler.MSG_INIT);
        }

    }

    public void start(int width, int height) {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_START);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_START, width, height));
        }

    }

    public void destroy() {
        synchronized (syncOp) {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(GLHandler.MSG_UN_INIT);
            }
            if (mGLHandlerThread != null) {
                mGLHandlerThread.quitSafely();
            }

        }

    }

    private static class GLHandler extends Handler {

        static final int MSG_INIT = 0x001;
        static final int MSG_UN_INIT = 0x002;
        static final int MSG_START = 0x003;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLConfig mConfig;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mStubEglSurface;
        private int mTextureID;

        private volatile SurfaceTexture mSurfaceTexture;

        private GlTextureFrameBuffer mGLFramebuffer;

        private TextureRenderer mOesTextureRenderer;


        public GLHandler(Looper looper) {
            super(looper);
        }

        private void initEGLContext() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                return;
            }
            // 1. eglGetDisplay
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new EglError("eglGetDisplay failed:");
            }
            int[] version = new int[2];
            // 2. eglInitialize
            if (!EGL14.eglInitialize(mEGLDisplay, version, /*offset*/ 0, version, /*offset*/ 1)) {
                throw new EglError("eglInitialize failed:");
            }

            int attribList[] = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGLExt.EGL_OPENGL_ES3_BIT_KHR,
                    EGLExt.EGL_RECORDABLE_ANDROID, 1,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT | EGL14.EGL_WINDOW_BIT,
                    EGL14.EGL_NONE
            };

            EGLConfig[] configs = new EGLConfig[1];

            int[] numConfigs = new int[1];

            // 3. eglChooseConfig
            EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);
            if (numConfigs[0] <= 0) {
                throw new EglError("eglChooseConfig failed:");
            }
            mConfig = configs[0];

            int[] attribList2 = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, 3,
                    EGL14.EGL_NONE
            };
            // 4. eglCreateContext
            mEGLContext = EGL14.eglCreateContext(mEGLDisplay, mConfig, EGL14.EGL_NO_CONTEXT, attribList2, 0);
            if (mEGLContext == EGL14.EGL_NO_CONTEXT) {
                throw new EglError("eglCreateContext failed:");
            }

            int[] attributes = new int[]{
                    EGL14.EGL_WIDTH, 1,
                    EGL14.EGL_HEIGHT, 1,
                    EGL14.EGL_NONE
            };
            // 5. eglCreatePbufferSurface
            mStubEglSurface = EGL14.eglCreatePbufferSurface(mEGLDisplay, mConfig, attributes, 0);
            if (mStubEglSurface == null || mStubEglSurface == EGL14.EGL_NO_SURFACE) {
                throw new EglError("eglCreatePbufferSurface failed:");
            }
            // 6. eglMakeCurrent
            boolean success = EGL14.eglMakeCurrent(mEGLDisplay, mStubEglSurface, mStubEglSurface, mEGLContext);
            if (!success) {
                throw new EglError("eglMakeCurrent failed:");
            }

            // 7.glBindTexture
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            mTextureID = textures[0];
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureID);
            GLESTool.checkGlError("glBindTexture ");
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);


        }

        private void UnInitEGLContext() {

        }

        private void handleStart(Message msg) {
            int width = msg.arg1;
            int height = msg.arg2;

            mSurfaceTexture = new SurfaceTexture(mTextureID);
            mSurfaceTexture.setDefaultBufferSize(width, height);

            mGLFramebuffer = new GlTextureFrameBuffer(GLES20.GL_RGB);
            mGLFramebuffer.allocateBuffers(width, height);

            mOesTextureRenderer = new TextureRenderer();
            mOesTextureRenderer.init();

        }


        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_INIT:
                    initEGLContext();
                    break;
                case MSG_UN_INIT:
                    UnInitEGLContext();
                    break;
                case MSG_START:
                    handleStart(msg);
                    break;
            }
        }

    }

}
