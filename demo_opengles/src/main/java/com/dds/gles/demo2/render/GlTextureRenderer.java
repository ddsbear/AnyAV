package com.dds.gles.demo2.render;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

public class GlTextureRenderer extends BaseTextureRender {
    private static final String TAG = "TextureRenderer";

    public enum ShaderType {OES, RGB, YUV}

    private static final int GL_MATRIX_SIZE = 16;

    private final float[] mMVPMatrix = new float[GL_MATRIX_SIZE];
    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int VERTEX_POS_SIZE = 3;
    private static final int VERTEX_UV_SIZE = 2;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;

    private static final String INPUT_VERTEX_COORDINATE_NAME = "in_pos";
    private static final String INPUT_TEXTURE_COORDINATE_NAME = "in_tc";
    private static final String TEXTURE_MATRIX_NAME = "tex_mat";

    private int mInputPosLocation;
    private int mInputTcLocation;

    private int muMVPMatrixHandle;
    private int mTexMatrixLocation;

    private GlShader mGLShader;
    private ShaderType mCurrentShaderType;
    private GlShader mCurrentShader;

    public GlTextureRenderer() {
        super();
    }

    public void prepareShader(ShaderType shaderType) {
        if (shaderType == mCurrentShaderType) {
            mGLShader = mCurrentShader;
        } else {
            mCurrentShaderType = null;
            if (mCurrentShader != null) {
                mCurrentShader.release();
                mCurrentShader = null;
            }
            mGLShader = createShader(shaderType);
            mCurrentShaderType = shaderType;
            mCurrentShader = mGLShader;
            mInputPosLocation = mGLShader.getAttribLocation(INPUT_VERTEX_COORDINATE_NAME);
            mInputTcLocation = mGLShader.getAttribLocation(INPUT_TEXTURE_COORDINATE_NAME);
            muMVPMatrixHandle = mGLShader.getUniformLocation("uMVPMatrix");
            mTexMatrixLocation = mGLShader.getUniformLocation(TEXTURE_MATRIX_NAME);
        }
    }

    public void drawTexture(int textureId, float[] mTexMatrix, int viewportX, int viewportY,
                            int viewportWidth, int viewportHeight) {
        if (mGLShader == null) {
            Log.w(TAG, "drawTexture: not fire prepareShader");
            return;
        }

        Matrix.setIdentityM(mMVPMatrix, 0);

        GLES20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        mGLShader.useProgram();

        // bindTexture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        if (mCurrentShaderType == ShaderType.YUV || mCurrentShaderType == ShaderType.OES) {
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        } else {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        }
        FloatBuffer triangleVertices = mRegularTriangleVertices;
        triangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);

        // draw vertex
        GLES20.glEnableVertexAttribArray(mInputPosLocation);
        GLES20.glVertexAttribPointer(mInputPosLocation, VERTEX_POS_SIZE, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        GLESTool.checkGlError("glDrawArrays");

        // draw fragment
        triangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glEnableVertexAttribArray(mInputTcLocation);
        GLES20.glVertexAttribPointer(mInputTcLocation, VERTEX_UV_SIZE, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        GLESTool.checkGlError("glDrawArrays");

        // Upload the texture transformation matrix.
        GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mTexMatrixLocation, 1, false, mTexMatrix, 0);
        // Draw the textures.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLESTool.checkGlError("glDrawArrays");
    }

    public void release() {
        if (mGLShader != null) {
            mGLShader.release();
        }

    }

    private GlShader createShader(ShaderType shaderType) {
        if (shaderType == ShaderType.OES) {
            return new GlShader(VERTEX_SHADER_DEFAULT, FRAGMENT_SHADER_OES);
        } else if (shaderType == ShaderType.RGB) {
            return new GlShader(VERTEX_SHADER_DEFAULT, FRAGMENT_SHADER_RGB);
        } else {
            return new GlShader(VERTEX_SHADER_DEFAULT, FRAGMENT_SHADER_RGB);
        }

    }

}
