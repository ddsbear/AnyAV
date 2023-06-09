package com.dds.gles.demo1.render.image;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.dds.gles.demo1.render.ProgramUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * draw a image
 */
public class ImageRenderer implements GLSurfaceView.Renderer {


    static final String SHADER_VERTEX = "" +
            "attribute vec4 vPosition;\n" +
            "attribute vec2 vCoordinate;\n" +
            "uniform mat4 vMatrix;\n" +
            "varying vec2 aCoordinate;\n" +
            "void main(){\n" +
            "    gl_Position = vPosition * vMatrix;\n" +
            "    aCoordinate = vCoordinate;\n" +
            "}";
    static final String SHADER_FRAGMENT = "" +
            "precision mediump float;\n" +
            "uniform sampler2D vTexture;\n" +
            "varying vec2 aCoordinate;\n" +
            "void main(){\n" +
            "    gl_FragColor = texture2D(vTexture,aCoordinate);\n" +
            "}";


    // 顶点坐标
    private static final float[] sPosition = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    // 纹理坐标
    private static final float[] sCoordinate = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };


    private Bitmap bitmap;
    int program;

    int vPosition;
    int vMatrix;
    int vCoordinate;
    int vTexture;

    FloatBuffer bPosition;
    FloatBuffer bCoordinate;

    int texture1;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    public ImageRenderer(Bitmap bitmap) {
        this.bitmap = bitmap;

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);

        program = ProgramUtil.createOpenGLProgram(SHADER_VERTEX, SHADER_FRAGMENT);
        if (program != 0) {
            vPosition = GLES20.glGetAttribLocation(program, "vPosition");
            vMatrix = GLES20.glGetUniformLocation(program, "vMatrix");
            vCoordinate = GLES20.glGetAttribLocation(program, "vCoordinate");
            vTexture = GLES20.glGetUniformLocation(program, "vTexture");
        }

        bPosition = ProgramUtil.createFloatBuffer(sPosition);
        bCoordinate = ProgramUtil.createFloatBuffer(sCoordinate);

        texture1 = ProgramUtil.createImage2DTexture(bitmap);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        // 图片比例
        float sWH = w / (float) h;
        // 屏幕比例
        float sWidthHeight = width / (float) height;

        if (width > height) {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight * sWH, sWidthHeight * sWH, -1, 1, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -sWidthHeight / sWH, sWidthHeight / sWH, -1, 1, 3, 7);
            }
        } else {
            if (sWH > sWidthHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1 / sWidthHeight * sWH, 1 / sWidthHeight * sWH, 3, 7);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1, 1, -sWH / sWidthHeight, sWH / sWidthHeight, 3, 7);
            }
        }

//        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f);
//        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        GLES20.glUseProgram(program);

        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mMVPMatrix, 0);


        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1);
        GLES20.glUniform1i(vTexture, 0);


        // 传入顶点坐标
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, true, 0, bPosition);
        // 传入纹理坐标
        GLES20.glEnableVertexAttribArray(vCoordinate);
        GLES20.glVertexAttribPointer(vCoordinate, 2, GLES20.GL_FLOAT, true, 0, bCoordinate);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sCoordinate.length / 2);


        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(vCoordinate);
    }


}
