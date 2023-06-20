package com.dds.gles.demo3.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

public class TextureRenderer extends BaseTextureRender {
    private static final String TAG = "TextureRenderer";
    private static final int GL_MATRIX_SIZE = 16;
    private final float[] mSTMatrix = new float[GL_MATRIX_SIZE];
    private final float[] mMVPMatrix = new float[GL_MATRIX_SIZE];
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int VERTEX_POS_SIZE = 3;
    private static final int VERTEX_UV_SIZE = 2;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;


    private int muMVPMatrixHandle;
    private int muSTMatrixHandle;
    private int maPositionHandle;
    private int maTextureHandle;

    private GlShader mGLShader;

    private int mRotation = 0;

    public TextureRenderer() {
        super();
    }

    public void init() {
        Matrix.setIdentityM(mSTMatrix, 0);
        mGLShader = new GlShader(VERTEX_SHADER_OES, FRAGMENT_SHADER_OES);
        maPositionHandle = mGLShader.getAttribLocation("aPosition");
        maTextureHandle = mGLShader.getAttribLocation("aTextureCoord");
        muMVPMatrixHandle = mGLShader.getUniformLocation("uMVPMatrix");
        muSTMatrixHandle = mGLShader.getUniformLocation("uSTMatrix");
    }

    public void draw(SurfaceTexture mSurfaceTexture, int textureId, int width, int height) {

        mSurfaceTexture.getTransformMatrix(mSTMatrix);

        Log.d(TAG, "draw: rotation = " + (mRotation - 90) + ",width = " + width + ",height = " + height);
        // rotation
        Matrix.translateM(mSTMatrix, 0, 0.5f, 0.5f, 0);
        Matrix.rotateM(mSTMatrix, 0, mRotation - 90, 0.0f, 0.0f, 1.0f);
        Matrix.translateM(mSTMatrix, 0, -0.5f, -0.5f, 0);

        Matrix.setIdentityM(mMVPMatrix, 0);

        GLES20.glViewport(0, 0, width, height);


        mGLShader.useProgram();

        // bindTexture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        FloatBuffer triangleVertices = mRegularTriangleVertices;
        triangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);

        // draw vertex
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        GLES20.glVertexAttribPointer(maPositionHandle, VERTEX_POS_SIZE, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);

        // draw fragment
        triangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        GLES20.glVertexAttribPointer(maTextureHandle, VERTEX_UV_SIZE, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);


        // Upload the texture transformation matrix.
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glUniformMatrix4fv(muSTMatrixHandle, 1, false, mSTMatrix, 0);
        // Draw the textures.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLESTool.checkGlError("glDrawArrays");
//        Log.d(TAG, "draw: " + mSurfaceTexture);
    }

    public void setRotation(int rotation) {
        this.mRotation = rotation;
    }

    public void release() {
        if (mGLShader != null) {
            mGLShader.release();
        }

    }


}
