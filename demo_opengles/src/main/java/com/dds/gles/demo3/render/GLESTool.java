package com.dds.gles.demo3.render;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLException;
import android.opengl.GLUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLESTool {

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

    public static String readRawTextFile(Context context, int rawId) {
        InputStream is = context.getResources().openRawResource(rawId);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line;
        StringBuilder sb = new StringBuilder();
        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static String readAssetTextFile(Context context, String file) {
        try {
            AssetManager assetManager = context.getAssets();
            InputStream ims = assetManager.open(file);
            String re = convertStreamToString(ims);
            ims.close();
            return re;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String convertStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    /**
     * 判断是否为平板
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
