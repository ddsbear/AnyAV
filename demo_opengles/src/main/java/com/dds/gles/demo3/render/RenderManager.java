package com.dds.gles.demo3.render;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

public class RenderManager {
    private static final String TAG = "RenderManager";
    private HandlerThread mGLHandlerThread;
    private final GLHandler mHandler;

    private final Object syncOp = new Object();

    public RenderManager() {
        mGLHandlerThread = new HandlerThread(TAG);
        mGLHandlerThread.start();
        mHandler = new GLHandler(mGLHandlerThread.getLooper());
    }

    public SurfaceTexture getSurfaceTexture() {
        return mHandler.mSurfaceTexture;
    }

    public void setup(int width, int height) {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_SETUP);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_SETUP, width, height));
        }

    }

    public void startPreview(Surface surface) {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_PREVIEW);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_PREVIEW, surface));
        }

    }

    public void drawFrame() {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_DRAW_FRAME);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_DRAW_FRAME));
        }
    }

    public void destroy() {
        synchronized (syncOp) {
            if (mHandler != null) {
                mHandler.sendEmptyMessage(GLHandler.MSG_RELEASE);
            }
            if (mGLHandlerThread != null) {
                mGLHandlerThread.quitSafely();
                try {
                    mGLHandlerThread.join();
                } catch (InterruptedException e) {
                    Log.e(TAG, "destroy: " + e);
                }
            }


        }

    }

    private static class GLHandler extends Handler {

        static final int MSG_SETUP = 0x003;
        static final int MSG_RELEASE = 0x002;
        static final int MSG_DRAW_FRAME = 0x004;
        static final int MSG_PREVIEW = 0x005;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLConfig mConfig;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mStubEglSurface;
        private int mTextureID;

        public volatile SurfaceTexture mSurfaceTexture;

        private TextureRenderer mOesTextureRenderer;

        private int mWidth;
        private int mHeight;

        private EGLSurface previewEglSurface;

        public GLHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MSG_SETUP:
                    int width = msg.arg1;
                    int height = msg.arg2;
                    initOffScreenGL();
                    handleSetup(width, height);
                    break;
                case MSG_RELEASE:
                    releaseEGLContext();
                    break;
                case MSG_DRAW_FRAME:
                    handleDrawFrame();
                    break;
                case MSG_PREVIEW:
                    Surface surface = (Surface) msg.obj;
                    handleStartPreview(surface);
                    break;
                default:
                    break;
            }
        }

        private void initOffScreenGL() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                return;
            }
            Log.d(TAG, "------initOffScreenGL start----------");
            // 1. eglGetDisplay
            mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            if (mEGLDisplay == EGL14.EGL_NO_DISPLAY) {
                throw new EglError("eglGetDisplay failed:");
            }
            Log.d(TAG, "initOffScreenGL: 1. EGL eglGetDisplay success");
            int[] version = new int[2];
            // 2. eglInitialize
            if (!EGL14.eglInitialize(mEGLDisplay, version, /*offset*/ 0, version, /*offset*/ 1)) {
                throw new EglError("eglInitialize failed:");
            }
            Log.d(TAG, "initOffScreenGL: 2. EGL eglInitialize success");
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
            Log.d(TAG, "initOffScreenGL: 3. EGL eglChooseConfig success");

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
            Log.d(TAG, "initOffScreenGL: 4. EGL eglCreateContext success");
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
            Log.d(TAG, "initOffScreenGL: 5. EGL eglCreatePbufferSurface success");

            // 6. eglMakeCurrent
            boolean success = EGL14.eglMakeCurrent(mEGLDisplay, mStubEglSurface, mStubEglSurface, mEGLContext);
            if (!success) {
                throw new EglError("eglMakeCurrent failed:");
            }
            Log.d(TAG, "initOffScreenGL: 6. EGL eglMakeCurrent success");

            Log.d(TAG, "------initOffScreenGL end------------");
        }

        private void handleSetup(int width, int height) {
            mWidth = width;
            mHeight = height;
            // bindTexture
            mTextureID = GLESTool.createOESTexture();
            Log.d(TAG, "handleSetup: createOESTexture width = " + width + ",height = " + height);
            mSurfaceTexture = new SurfaceTexture(mTextureID);
            Log.d(TAG, "handleSetup: mSurfaceTexture = " + mSurfaceTexture);
            mOesTextureRenderer = new TextureRenderer();
            mOesTextureRenderer.init();
        }

        private void handleStartPreview(Surface surface) {
            int[] attribList = {
                    EGL14.EGL_NONE
            };
            //  eglCreateWindowSurface
            previewEglSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, mConfig,
                    surface, attribList, /*offset*/ 0);
            GLESTool.checkGlError("handleStartPreview 1. eglCreateWindowSurface");
            // eglMakeCurrent
            EGL14.eglMakeCurrent(mEGLDisplay, previewEglSurface, previewEglSurface, mEGLContext);
            GLESTool.checkGlError("handleStartPreview 2. eglMakeCurrent");
        }

        private void handleDrawFrame() {
            mSurfaceTexture.updateTexImage();
            // eglMakeCurrent
            EGL14.eglMakeCurrent(mEGLDisplay, previewEglSurface, previewEglSurface, mEGLContext);
            // draw
            mOesTextureRenderer.draw(mSurfaceTexture, mTextureID, mWidth, mHeight);
            // eglSwapBuffers
            EGL14.eglSwapBuffers(mEGLDisplay, previewEglSurface);

        }

        private void releaseEGLContext() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                if (mOesTextureRenderer != null) {
                    mOesTextureRenderer.release();
                }
                EGL14.eglMakeCurrent(mEGLDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                        EGL14.EGL_NO_CONTEXT);
                if (previewEglSurface != null) {
                    EGL14.eglDestroySurface(mEGLDisplay, previewEglSurface);
                }

                if (mStubEglSurface != null) {
                    EGL14.eglDestroySurface(mEGLDisplay, mStubEglSurface);
                }
                EGL14.eglDestroyContext(mEGLDisplay, mEGLContext);
                EGL14.eglReleaseThread();
                EGL14.eglTerminate(mEGLDisplay);

                mConfig = null;
                mEGLDisplay = EGL14.EGL_NO_DISPLAY;
                mEGLContext = EGL14.EGL_NO_CONTEXT;
                if (mSurfaceTexture != null) {
                    mSurfaceTexture.release();
                }
            }
        }


    }

}
