package com.dds.gles.demo2.render;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.dds.gles.demo2.render.filter.GreyFilter;

import java.nio.FloatBuffer;

public class GlTextureRenderer extends BaseTextureRender {
    private static final String TAG = "TextureRenderer";

    public enum ShaderType {OES, RGB, YUV}

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int VERTEX_POS_SIZE = 3;
    private static final int VERTEX_UV_SIZE = 2;

    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;


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

        }
    }

    public void drawOesTexture(int textureId, float[] mTexMatrix, int viewportX, int viewportY,
                               int viewportWidth, int viewportHeight) {
        if (mGLShader == null) {
            Log.w(TAG, "drawTexture: not fire prepareShader");
            return;
        }

        GLES20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        mGLShader.useProgram();

        // bindTexture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        FloatBuffer triangleVertices = mRegularTriangleVertices;

        // draw vertex
        triangleVertices.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        int inPos = mGLShader.getAttribLocation("in_pos");
        GLES20.glEnableVertexAttribArray(inPos);
        GLES20.glVertexAttribPointer(inPos, VERTEX_POS_SIZE, GLES20.GL_FLOAT,
                false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        GLESTool.checkGlError("glDrawArrays");

        // draw fragment
        triangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        int inTc = mGLShader.getAttribLocation("in_tc");
        GLES20.glEnableVertexAttribArray(inTc);
        GLES20.glVertexAttribPointer(inTc, VERTEX_UV_SIZE, GLES20.GL_FLOAT, false, TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices);
        GLESTool.checkGlError("glDrawArrays");

        // Upload the texture transformation matrix.
        GLES20.glUniformMatrix4fv(mGLShader.getUniformLocation("tex_mat"), 1, false, mTexMatrix, 0);

        GLES20.glUniform1i(mGLShader.getUniformLocation("sTexture"), 0);
        // Draw the textures.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLESTool.checkGlError("glDrawArrays");
    }

    public void drawRgbTexture(int textureId, float[] mTexMatrix, int viewportX, int viewportY,
                               int viewportWidth, int viewportHeight) {
        if (mGLShader == null) {
            Log.w(TAG, "drawTexture: not fire prepareShader");
            return;
        }

        GLES20.glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        mGLShader.useProgram();

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        Matrix.translateM(mTexMatrix, 0, 0f, 1f, 0);
        Matrix.rotateM(mTexMatrix, 0, 90, 0, 0, 1);

        // bindTexture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // Upload the texture transformation matrix.
        GLES20.glUniformMatrix4fv(mGLShader.getUniformLocation("tex_mat"), 1, false, mTexMatrix, 0);

        GLES20.glUniform1i(mGLShader.getUniformLocation("sTexture"), 0);
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
