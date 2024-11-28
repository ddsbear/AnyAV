package com.dds.gles.demo1.render;

import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.util.Size;


import com.dds.gles.render.GlFrameBuffer;
import com.dds.gles.render.GlShader;

import java.nio.FloatBuffer;
import java.util.concurrent.CompletableFuture;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraPreViewRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "dds_CameraPreViewRenderer";
    private SurfaceTexture surfaceTexture;
    private final CompletableFuture<SurfaceTexture> completableFuture;


    private static final boolean sUseFbo = true;
    private static final boolean sUseFilter = true;

    // 顶点坐标
    private static final float[] sPosition = {
            -1.0f, 1.0f,    //left-up        1-------3
            -1.0f, -1.0f,   //left-bottom    |    /  |
            1.0f, 1.0f,     //right-up       |  /    |
            1.0f, -1.0f     //right-bottom   2-------4
    };

    // 纹理坐标
    private static final float[] sCoordinate = {
            0.0f, 1.0f,       // left-up
            0.0f, 0.0f,       // left-bottom
            1.0f, 1.0f,       // right-up
            1.0f, 0.0f,       // right-bottom
    };

    private static final String VERTEX_SHADER_CAMERA = "attribute vec4 vPosition;\n" +
            "attribute vec4 vCoordinate;\n" +
            "uniform mat4 vMatrix;\n" +
            "varying vec2 tc;\n" +
            "void main() {\n" +
            "    gl_Position = vPosition;\n" +
            "    tc = (vMatrix * vCoordinate).xy;\n" +
            "}";

    private static final String FRAGMENT_SHADER_CAMERA = "#extension GL_OES_EGL_image_external: require\n" +
            "precision mediump float;\n" +
            "varying vec2 tc;\n" +
            "uniform samplerExternalOES vTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(vTexture,tc);\n" +
            "}";

    private static final String FRAGMENT_SHADER_FBO = "#extension GL_OES_EGL_image_external: require\n" +
            "precision mediump float;\n" +
            "varying vec2 tc;\n" +
            "uniform sampler2D vTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(vTexture,tc);\n" +
            "}";

    private static final String FRAGMENT_SHADER_FILTER = "precision mediump float;\n"
            + "varying vec2 tc;\n"
            + "uniform sampler2D vTexture;\n"
            + "void main(){\n"
            + "  vec4 mask = texture2D(vTexture, tc);\n"
            + "  gl_FragColor = vec4(mask.r,mask.g,mask.g,1.0);\n"
            + "}";


    private final float[] mMVPMatrix = new float[16];


    FloatBuffer bPosition;
    FloatBuffer bCoordinate;

    // oes
    int oesTextureId;

    // shader
    GlShader shader;
    // framebuffer
    GlFrameBuffer frameBuffer;
    // fbo
    GlShader shaderFbo;
    // filter
    GlShader filter;

    Size mBuferSize;
    int mWidth;
    int mHeight;

    public CompletableFuture<SurfaceTexture> getCompletableFuture() {
        return completableFuture;
    }

    public CameraPreViewRenderer(Size mPreviewSize) {
        bPosition = ProgramUtil.createFloatBuffer(sPosition);
        bCoordinate = ProgramUtil.createFloatBuffer(sCoordinate);
        completableFuture = new CompletableFuture<>();
        mBuferSize = mPreviewSize;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated: ");
        // create texture
        int[] textures = new int[1];
        ProgramUtil.createOESTexture(textures);
        oesTextureId = textures[0];
        // bind SurfaceTexture
        surfaceTexture = new SurfaceTexture(oesTextureId);
        // loadRenderShaders
        shader = new GlShader(VERTEX_SHADER_CAMERA, FRAGMENT_SHADER_CAMERA);
        if (sUseFbo) {
            shaderFbo = new GlShader(VERTEX_SHADER_CAMERA, FRAGMENT_SHADER_FBO);
            if (sUseFilter) {
                filter = new GlShader(VERTEX_SHADER_CAMERA, FRAGMENT_SHADER_FILTER);
            }
        }
        frameBuffer = new GlFrameBuffer(GLES20.GL_RGBA);
        frameBuffer.allocateBuffers(mBuferSize.getWidth(), mBuferSize.getHeight());


        completableFuture.complete(surfaceTexture);


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: width = " + width + ",height = " + height);
        GLES20.glViewport(0, 0, width, height);
        mWidth = width;
        mHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // update frame
        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mMVPMatrix);

        // clear
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

        // use program
        shader.useProgram();

        if (sUseFbo) {
            GLES20.glViewport(0, 0, mBuferSize.getWidth(), mBuferSize.getHeight());
            frameBuffer.bind();
        }

        // set bPosition value
        int vPosition = shader.getAttribLocation("vPosition");
        int vCoordinate = shader.getAttribLocation("vCoordinate");

        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, bPosition);
        GLES20.glEnableVertexAttribArray(vPosition);

        // set bCoordinate value
        GLES20.glVertexAttribPointer(vCoordinate, 2, GLES20.GL_FLOAT, false, 0, bCoordinate);
        GLES20.glEnableVertexAttribArray(vCoordinate);

        // bindTexture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId);

        // set matrix value
        GLES20.glUniformMatrix4fv(shader.getUniformLocation("vMatrix"), 1, false, mMVPMatrix, 0);

        // 赋值tc
        GLES20.glUniform1i(shader.getUniformLocation("vTexture"), 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sCoordinate.length / 2);


        if (sUseFbo) {
            frameBuffer.unbind();

            GLES20.glViewport(0, 0, mWidth, mHeight);

            Matrix.translateM(mMVPMatrix, 0, 0f, 1f, 0);
            Matrix.rotateM(mMVPMatrix, 0, 90, 0, 0, 1);

            // use program
            shaderFbo.useProgram();

            // bindTexture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, frameBuffer.getTextureId());

            // set matrix value
            GLES20.glUniformMatrix4fv(shaderFbo.getUniformLocation("vMatrix"), 1, false, mMVPMatrix, 0);

            // 赋值tc
            GLES20.glUniform1i(shaderFbo.getUniformLocation("vTexture"), 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sCoordinate.length / 2);

            if (sUseFilter) {
                // use program
                filter.useProgram();

                // set matrix value
                GLES20.glUniformMatrix4fv(filter.getUniformLocation("vMatrix"), 1, false, mMVPMatrix, 0);

                // 赋值tc
                GLES20.glUniform1i(filter.getUniformLocation("vTexture"), 0);
                GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sCoordinate.length / 2);
            }

        }
    }

    public void release() {
        if (shader != null) {
            shader.release();
        }
        if (shaderFbo != null) {
            shaderFbo.release();
        }
        if (filter != null) {
            filter.release();
        }
        if (frameBuffer != null) {
            frameBuffer.release();
        }

    }
}
