package com.dds.gles.demo3.render;

import android.opengl.GLES20;
import android.util.Log;

public class GlShader {
    private static final String TAG = "GlShader";

    private int mProgramHandle;

    public GlShader(String vertexSource, String fragmentSource) {
        final int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        final int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (vertexShader == 0 || fragmentShader == 0) {
            Log.e(TAG, "Aborting program creation.");
            return;
        }
        mProgramHandle = attachAndLink(vertexShader, fragmentShader);

        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        GLESTool.checkGlError("createAndLink GlShader");
    }

    private int compileShader(int shaderType, String source) {
        final int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) {
            Log.e(TAG, "compileShader() failed. GLES20 error: " + GLES20.glGetError());
            return 0;
        }
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compileStatus = new int[]{GLES20.GL_FALSE};
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Compile error " + GLES20.glGetShaderInfoLog(shader) + " in shader:\n" + source);
            return 0;
        }
        GLESTool.checkGlError("compileShader");
        return shader;
    }

    private int attachAndLink(int vertexShader, int fragmentShader) {
        int shader = GLES20.glCreateProgram();
        if (shader == 0) {
            Log.e(TAG, "glCreateProgram() failed. GLES20 error: " + GLES20.glGetError());
            return 0;
        }
        GLES20.glAttachShader(shader, vertexShader);
        GLESTool.checkGlError("Attach vertex shader");

        GLES20.glAttachShader(shader, fragmentShader);
        GLESTool.checkGlError("Attach fragment shader");

        GLES20.glLinkProgram(shader);
        int[] linkStatus = new int[]{GLES20.GL_FALSE};
        GLES20.glGetProgramiv(shader, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: " + GLES20.glGetProgramInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }

    public int getAttribLocation(String label) {
        if (mProgramHandle == -1) {
            throw new RuntimeException(String.format("Can't get location of %s on an invalid program.", label));
        }
        int location = GLES20.glGetAttribLocation(mProgramHandle, label);
        if (location < 0) {
            throw new RuntimeException("Could not locate '" + label + "' in program");
        }
        return location;
    }

    public int getUniformLocation(String label) {
        if (mProgramHandle == -1) {
            throw new RuntimeException(String.format("Can't get location of %s on an invalid program.", label));
        }
        int location = GLES20.glGetUniformLocation(mProgramHandle, label);
        if (location < 0) {
            throw new RuntimeException("Could not locate uniform '" + label + "' in program");
        }
        return location;
    }

    public void useProgram() {
        if (mProgramHandle == -1) {
            throw new RuntimeException("The program has been released");
        }
        GLES20.glUseProgram(mProgramHandle);
        GLESTool.checkGlError("glUseProgram");
    }

    public void release() {
        Log.d(TAG, "Deleting shader.");
        // Delete program, automatically detaching any shaders from it.
        if (mProgramHandle != -1) {
            GLES20.glDeleteProgram(mProgramHandle);
            mProgramHandle = -1;
        }
    }

}
