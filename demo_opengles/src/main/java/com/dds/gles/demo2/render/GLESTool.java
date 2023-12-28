package com.dds.gles.demo2.render;

import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLException;
import android.opengl.GLUtils;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Locale;

public class GLESTool {

    public static final boolean GL_ASSERTIONS_ENABLED = true;

    public static class EglError extends RuntimeException {

        public EglError(String error) {
            super(error + GLUtils.getEGLErrorString(EGL14.eglGetError()));
        }
    }

    public static class GlOutOfMemoryException extends GLException {
        public GlOutOfMemoryException(int error, String msg) {
            super(error, msg);
        }
    }

    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw error == GLES20.GL_OUT_OF_MEMORY
                    ? new GlOutOfMemoryException(error, op)
                    : new GLException(error, op + " glError 0x" + Integer.toHexString(error));
        }
    }

    public static void checkGlErrors(String tag, String op) {
        int lastError = GLES20.GL_NO_ERROR;
        do {
            // there could be more than one error flag
            int error = GLES20.glGetError();
            if (error == GLES20.GL_NO_ERROR) break;
            lastError = error;
            if (op == null || op.length() == 0) {
                Log.e(tag, String.format(Locale.US, "GL error 0x%04x", error));
            } else {
                Log.e(tag, String.format(Locale.US, "GL error: %s -> 0x%04x", op, error));
            }
        } while (true);
        if (GL_ASSERTIONS_ENABLED && lastError != GLES20.GL_NO_ERROR) {
            throw new IllegalStateException(String.format("glError: 0x%04x", lastError));
        }
    }

    /**
     * Create an OES texture for the camera preview
     *
     * @return int texture ID
     */
    public static int createOESTexture() {
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        checkGlError("glBindTexture");
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        return textures[0];
    }

    /**
     * Generate texture with standard parameters.
     */
    public static int generateTexture(int target) {
        final int[] textureArray = new int[1];
        GLES20.glGenTextures(1, textureArray, 0);
        final int textureId = textureArray[0];
        GLES20.glBindTexture(target, textureId);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(target, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("generateTexture" + target);
        return textureId;
    }

    public static FloatBuffer createFloatBuffer(float[] s) {
        ByteBuffer b = ByteBuffer.allocateDirect(s.length * 4);
        b.order(ByteOrder.nativeOrder());
        FloatBuffer bPosition = b.asFloatBuffer();
        bPosition.put(s);
        bPosition.position(0);
        return bPosition;
    }
}
