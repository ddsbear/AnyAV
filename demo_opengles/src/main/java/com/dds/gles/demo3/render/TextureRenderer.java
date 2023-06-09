package com.dds.gles.demo3.render;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

public class TextureRenderer extends BaseTextureRender {
    private static final String TAG = "TextureRenderer";
    private static final int GL_MATRIX_SIZE = 16;
    private final float[] mSTMatrix = new float[GL_MATRIX_SIZE];


    private int mVertexShaderHandle = 0;
    private int mFragmentShaderHandle = 0;
    private int mProgramHandle = 0;


    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    public TextureRenderer() {
        super();
        Matrix.setIdentityM(mSTMatrix, 0);
    }

    public void init() {
        mVertexShaderHandle = compileShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_OES);
        mFragmentShaderHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_OES);
        if (mVertexShaderHandle == 0 || mFragmentShaderHandle == 0) {
            Log.e(TAG, "Aborting program creation.");
            return;
        }
        mProgramHandle = createAndLink(mVertexShaderHandle, mFragmentShaderHandle);
        GLES20.glDeleteShader(mVertexShaderHandle);
        GLES20.glDeleteShader(mFragmentShaderHandle);
        GLESTool.checkGlError("Linking program .");

        maPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        maTextureHandle = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        muSTMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "uSTMatrix");
    }

    private int compileShader(int type, String source) {
        final int shader = GLES20.glCreateShader(type);
        if (shader == 0) {
            GLESTool.checkGlError("glCreateShader failed:");
            return 0;
        }
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int[] compileStatus = {GLES20.GL_FALSE};
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] != GLES20.GL_TRUE) {
            String errorLog = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            Log.e(TAG, "Error compiling shader: " + errorLog);
            return 0;
        }
        return shader;
    }

    private int createAndLink(int vertexShader, int fragmentShader) {
        final int program = GLES20.glCreateProgram();

        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        GLES20.glLinkProgram(program);

        int[] linkStatus = {GLES20.GL_FALSE};
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);

        if (linkStatus[0] != GLES20.GL_TRUE) {
            String errorLog = GLES20.glGetProgramInfoLog(program);
            GLES20.glDeleteProgram(program);
            Log.e(TAG, "Error linking program: " + errorLog);
            return 0;
        }
        return program;
    }


}
