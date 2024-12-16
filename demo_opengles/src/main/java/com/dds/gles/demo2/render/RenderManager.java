package com.dds.gles.demo2.render;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import com.dds.gles.demo2.render.filter.GreyFilter;
import com.dds.gles.demo2.render.filter.SkinSmoothFilter;
import com.dds.gles.render.GLESTool;
import com.dds.gles.render.GlFrameBuffer;

import java.util.concurrent.CompletableFuture;

public class RenderManager {
    private static final String TAG = "dds_RenderManager";
    private final HandlerThread mGLHandlerThread;
    private final GLHandler mHandler;

    private final Object syncOp = new Object();

    private static final boolean sUseFbo = true;

    public RenderManager() {
        mGLHandlerThread = new HandlerThread(TAG);
        mGLHandlerThread.start();
        mHandler = new GLHandler(mGLHandlerThread.getLooper());
    }

    public CompletableFuture<SurfaceTexture> getSurfaceTexture() {
        return mHandler.mSurfaceFuture;
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

    public void setDeviceRotation(int rotation) {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_DEVICE_ROTATION);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_DEVICE_ROTATION, rotation, 0));
        }
    }

    public void setSensorRotation(int rotation) {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_SENSOR_ROTATION);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_SENSOR_ROTATION, rotation, 0));
        }
    }

    public void setSurfaceSize(int width, int height) {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_SURFACE_SIZE);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_SURFACE_SIZE, width, height));
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

    public void enableFilter(boolean enableFilter) {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_FILTER);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_FILTER, enableFilter ? 1 : 0, 0));
        }
    }

    public void enableBeauty(boolean enableBeauty) {
        synchronized (syncOp) {
            mHandler.removeMessages(GLHandler.MSG_BEAUTY);
            mHandler.sendMessage(mHandler.obtainMessage(GLHandler.MSG_BEAUTY, enableBeauty ? 1 : 0, 0));
        }
    }

    private static class GLHandler extends Handler {
        static final int MSG_SETUP = 0x01;
        static final int MSG_DRAW_FRAME = 0x02;
        static final int MSG_PREVIEW = 0x03;
        static final int MSG_DEVICE_ROTATION = 0x04;
        static final int MSG_SURFACE_SIZE = 0x05;
        static final int MSG_RELEASE = 0x06;
        static final int MSG_FILTER = 0x07;
        static final int MSG_BEAUTY = 0x08;
        static final int MSG_SENSOR_ROTATION = 0x09;

        private EGLDisplay mEGLDisplay = EGL14.EGL_NO_DISPLAY;
        private EGLConfig mConfig;
        private EGLContext mEGLContext = EGL14.EGL_NO_CONTEXT;
        private EGLSurface mStubEglSurface;
        private int mTextureID;

        public volatile SurfaceTexture mSurfaceTexture;
        private final CompletableFuture<SurfaceTexture> mSurfaceFuture;
        private GlTextureRenderer mGlTextureRenderer;

        private GlFrameBuffer mFrameBuffer;
        private GlTextureRenderer mRGBTextureRenderer;

        private GlFrameBuffer mFrameBuffer1;
        private GlFrameBuffer mFrameBuffer2;

        private int mBufferWidth;
        private int mBufferHeight;
        private int mDeviceRotation;
        private int mSensorRotation;

        private int mWidth;
        private int mHeight;

        private EGLSurface previewEglSurface;

        private boolean isFilterEnable;
        private GreyFilter filter;

        private boolean isBeautyEnable;
        private SkinSmoothFilter smoothFilter;

        public GLHandler(Looper looper) {
            super(looper);
            mSurfaceFuture = new CompletableFuture<>();
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
                case MSG_DEVICE_ROTATION:
                    handleSetDeviceRotation(msg.arg1);
                    break;
                case MSG_SURFACE_SIZE:
                    handleSetSurfaceSize(msg.arg1, msg.arg2);
                    break;
                case MSG_FILTER:
                    handleEnableFilter(msg.arg1);
                    break;
                case MSG_BEAUTY:
                    handleEnableBeauty(msg.arg1);
                    break;
                case MSG_SENSOR_ROTATION:
                    handleSetSensorRotation(msg.arg1);
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
                throw new GLESTool.EglError("eglGetDisplay failed:");
            }
            Log.d(TAG, "initOffScreenGL: 1. EGL eglGetDisplay success");
            int[] version = new int[2];
            // 2. eglInitialize
            if (!EGL14.eglInitialize(mEGLDisplay, version, /*offset*/ 0, version, /*offset*/ 1)) {
                throw new GLESTool.EglError("eglInitialize failed:");
            }
            Log.d(TAG, "initOffScreenGL: 2. EGL eglInitialize success");
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                    EGLExt.EGL_RECORDABLE_ANDROID, 1,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT | EGL14.EGL_WINDOW_BIT,
                    EGL14.EGL_NONE
            };

            EGLConfig[] configs = new EGLConfig[1];

            int[] numConfigs = new int[1];

            // 3. eglChooseConfig
            EGL14.eglChooseConfig(mEGLDisplay, attribList, 0, configs, 0, configs.length, numConfigs, 0);
            if (numConfigs[0] <= 0) {
                throw new GLESTool.EglError("eglChooseConfig failed:");
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
                throw new GLESTool.EglError("eglCreateContext failed:");
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
                throw new GLESTool.EglError("eglCreatePbufferSurface failed:");
            }
            Log.d(TAG, "initOffScreenGL: 5. EGL eglCreatePbufferSurface success");

            // 6. eglMakeCurrent
            boolean success = EGL14.eglMakeCurrent(mEGLDisplay, mStubEglSurface, mStubEglSurface, mEGLContext);
            if (!success) {
                throw new GLESTool.EglError("eglMakeCurrent failed:");
            }
            Log.d(TAG, "initOffScreenGL: 6. EGL eglMakeCurrent success");

            Log.d(TAG, "------initOffScreenGL end------------");
        }

        private void handleSetup(int width, int height) {
            mBufferWidth = width;
            mBufferHeight = height;
            mWidth = mBufferWidth;
            mHeight = mBufferHeight;
            // createTexture
            mTextureID = GLESTool.createOESTexture();
            // new SurfaceTexture
            mSurfaceTexture = new SurfaceTexture(mTextureID);

            mGlTextureRenderer = new GlTextureRenderer();

            if (sUseFbo) {
                mRGBTextureRenderer = new GlTextureRenderer();
                mRGBTextureRenderer.setDisplayRotation(mDeviceRotation);
                mFrameBuffer = new GlFrameBuffer(GLES20.GL_RGBA);
                mFrameBuffer.allocateBuffers(width, height);

                mFrameBuffer1 = new GlFrameBuffer(GLES20.GL_RGBA);
                mFrameBuffer1.allocateBuffers(width, height);

                mFrameBuffer2 = new GlFrameBuffer(GLES20.GL_RGBA);
                mFrameBuffer2.allocateBuffers(width, height);

                filter = new GreyFilter();
                smoothFilter = new SkinSmoothFilter();
            }
            mSurfaceFuture.complete(mSurfaceTexture);
        }

        private void handleStartPreview(Surface surface) {
            if (previewEglSurface == null) {
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

        }

        private void handleDrawFrame() {
            if (mBufferHeight == 0 || mBufferWidth == 0) {
                return;
            }

            EGL14.eglMakeCurrent(mEGLDisplay, previewEglSurface, previewEglSurface, mEGLContext);

            mSurfaceTexture.updateTexImage();
            // glClear
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

            if (sUseFbo) {
                mFrameBuffer.bind();
            }
            // draw
            float[] mSTMatrix = new float[16];
            mSurfaceTexture.getTransformMatrix(mSTMatrix);

            mGlTextureRenderer.prepareShader(GlTextureRenderer.ShaderType.OES);
            mGlTextureRenderer.drawOesTexture(mTextureID, mSTMatrix, 0, 0, mBufferWidth, mBufferHeight);

            if (sUseFbo) {
                mFrameBuffer.unbind();

                int showTextureId = mFrameBuffer.getTextureId();

                if (isFilterEnable) {
                    mFrameBuffer1.bind();
                    filter.prepare();
                    filter.draw(showTextureId, mSTMatrix);
                    mFrameBuffer1.unbind();
                    showTextureId = mFrameBuffer1.getTextureId();
                }

                if (isBeautyEnable) {
                    mFrameBuffer2.bind();
                    smoothFilter.prepare();
                    smoothFilter.draw(showTextureId, mSTMatrix, mFrameBuffer.getWidth(), mFrameBuffer.getHeight());
                    mFrameBuffer2.unbind();
                    showTextureId = mFrameBuffer2.getTextureId();
                }

                mRGBTextureRenderer.prepareShader(GlTextureRenderer.ShaderType.RGB);
                mRGBTextureRenderer.drawRgbTexture(showTextureId, mSTMatrix, 0, 0, mWidth, mHeight, mSensorRotation);
            }

            // eglSwapBuffers
            EGL14.eglSwapBuffers(mEGLDisplay, previewEglSurface);

        }

        private void handleSetDeviceRotation(int deviceRotation) {
            mDeviceRotation = deviceRotation;
            if (mRGBTextureRenderer != null) {
                mRGBTextureRenderer.setDisplayRotation(mDeviceRotation);
            }
        }

        private void handleSetSurfaceSize(int width, int height) {
            if (width == 0 || height == 0) {
                return;
            }
            this.mWidth = width;
            this.mHeight = height;
        }

        private void handleEnableFilter(int arg1) {
            isFilterEnable = arg1 == 1;
        }

        private void handleEnableBeauty(int arg1) {
            isBeautyEnable = arg1 == 1;
        }

        private void handleSetSensorRotation(int sensorRotation) {
            mSensorRotation = sensorRotation;
        }

        private void releaseEGLContext() {
            if (mEGLDisplay != EGL14.EGL_NO_DISPLAY) {
                if (filter != null) {
                    filter.release();
                }
                if (mFrameBuffer != null) {
                    mFrameBuffer.release();
                }
                if (mGlTextureRenderer != null) {
                    mGlTextureRenderer.release();
                }
                if (mRGBTextureRenderer != null) {
                    mRGBTextureRenderer.release();
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
