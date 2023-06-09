package com.dds.gles.demo3.render;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GLESTool {

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

}
