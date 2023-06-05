package com.dds.gles.render.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.dds.gles.R;
import com.dds.gles.camera.MyManager;
import com.dds.gles.utils.OpenGLUtils;
import com.dds.gles.render.ProgramUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraPreViewRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "CameraPreViewRenderer";
    private MyManager manager;
    private SurfaceTexture surfaceTexture;
    private Context mContext;

    // 顶点坐标
    private static final float[] sPosition = {
            -1.0f, 1.0f,    //左上角
            -1.0f, -1.0f,   //左下角
            1.0f, 1.0f,     //右上角
            1.0f, -1.0f     //右下角
    };

    // 纹理坐标
    private static final float[] sCoordinate = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,


    };

    private float[] mViewMatrix = new float[16];
    private float[] mProjectMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];

    int program;
    int vPosition;
    int vMatrix;
    int vCoordinate;
    int textureCoordinate;

    FloatBuffer bPosition;
    FloatBuffer bCoordinate;

    private int textureId;


    public CameraPreViewRenderer(Context context) {
        mContext = context;
        manager = new MyManager(context);
        manager.openCamera("0");
        surfaceTexture = manager.getSurfaceTexture();

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        program = ProgramUtil.createOpenGLProgram(
                OpenGLUtils.readRawTextFile(mContext, R.raw.vertex),
                OpenGLUtils.readRawTextFile(mContext, R.raw.frgament));

        if (program != 0) {
            vPosition = GLES20.glGetAttribLocation(program, "vPosition");
            vMatrix = GLES20.glGetUniformLocation(program, "vMatrix");
            vCoordinate = GLES20.glGetAttribLocation(program, "vCoordinate");
            textureCoordinate = GLES20.glGetUniformLocation(program, "textureCoordinate");

            bPosition = ProgramUtil.createFloatBuffer(sPosition);
            bCoordinate = ProgramUtil.createFloatBuffer(sCoordinate);
            textureId = ProgramUtil.createOESTexture();

        }


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // 正交投影
        Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1, 1, 1, 3);

        // 透视投影
//        Matrix.perspectiveM(mProjectMatrix,0,0,0,1,3);


        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);


        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0);


        // flip
//        Matrix.scaleM(mMVPMatrix, 0, -1, 1, 1);
        // rotate
//        Matrix.rotateM(mMVPMatrix, 0, 90, 0, 0, 1);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 清除画布
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // use program
        GLES20.glUseProgram(program);

        // 设置矩阵
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mMVPMatrix, 0);
        // bindTexture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glUniform1i(textureCoordinate, 0);

        surfaceTexture.updateTexImage();
        surfaceTexture.getTransformMatrix(mMVPMatrix);

        // draw vertex
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, true, 0, bPosition);
        // draw fragment
        GLES20.glEnableVertexAttribArray(vCoordinate);
        GLES20.glVertexAttribPointer(vCoordinate, 2, GLES20.GL_FLOAT, true, 0, bCoordinate);
        // 通知开始画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, sCoordinate.length / 2);

        // 关闭
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(vCoordinate);

    }

}
