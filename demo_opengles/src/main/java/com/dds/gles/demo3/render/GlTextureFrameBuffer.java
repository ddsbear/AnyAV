package com.dds.gles.demo3.render;

import android.opengl.GLES20;

import java.nio.ByteBuffer;

public class GlTextureFrameBuffer {
    private static final String TAG = "GLFramebuffer";
    private int mTextureId;
    private int mFramebufferId;
    private int mBufferWidth = 0;
    private int mBufferHeight = 0;

    private final int pixelFormat;

    public GlTextureFrameBuffer(int pixelFormat) {
        switch (pixelFormat) {
            case GLES20.GL_LUMINANCE:
            case GLES20.GL_RGB:
            case GLES20.GL_RGBA:
                this.pixelFormat = pixelFormat;
                break;
            default:
                throw new IllegalArgumentException("Invalid pixel format: " + pixelFormat);
        }
        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        mTextureId = textureIds[0];

        int[] frameBufferIds = new int[1];
        GLES20.glGenFramebuffers(1, frameBufferIds, 0);
        mFramebufferId = frameBufferIds[0];
    }

    public void allocateBuffers(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid size: " + width + "x" + height);
        }
        if (width == mBufferWidth && height == mBufferHeight) {
            return;
        }
        mBufferWidth = width;
        mBufferHeight = height;
        // Allocate texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, pixelFormat, width, height, 0, pixelFormat, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_MIRRORED_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);
        GLESTool.checkGlError("allocateBuffers");
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        // Attach the texture to the framebuffer as color attachment.
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFramebufferId);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, mTextureId, 0);
        // Check that the framebuffer is in a good state.
        final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new IllegalStateException("Framebuffer not complete, status: " + status);
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public void release() {
        GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        mTextureId = 0;
        GLES20.glDeleteFramebuffers(1, new int[]{mFramebufferId}, 0);
        mFramebufferId = 0;
        mBufferWidth = 0;
        mBufferHeight = 0;
    }
}
