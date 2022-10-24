package com.dds.gles;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "MyRenderer";
    int mProgram;
    FloatBuffer vertexBuffer;

    public static float triangleCoordinates[] = {
            0.5f, 0.5f, 0f,
            -0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f
    };
    public static final float vertexColor[] = {1.0f, 1.0f, 1.0f, 1.0f};

    public static final int CORDS_PER_VERTEX = 3;                  // 每个顶点的坐标数
    public static final int VERTEX_STRIDE = CORDS_PER_VERTEX * 4;  // 每个坐标是4个字节

    public static final int VERTEX_COUNT = triangleCoordinates.length / CORDS_PER_VERTEX;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 申请底色空间
        GLES20.glClearColor(0.14f, 0.1f, 0.1f, 1f);

        // 创建顶点着色器-----------------------------------------------
        int v_shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        if (v_shader == 0) {
            Log.d(TAG, "onSurfaceCreated: v_shader is 0");
            return;
        }
        // 将着色器的代码放入着色器中
        GLES20.glShaderSource(v_shader, ProgramUtil.SHADER_VERTEX);
        // 编译着色器
        GLES20.glCompileShader(v_shader);

        // 创建片元着色器-----------------------------------------------
        int f_shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        if (f_shader == 0) {
            Log.d(TAG, "onSurfaceCreated: f_shader is 0");
            return;
        }
        GLES20.glShaderSource(f_shader, ProgramUtil.SHADER_FRAGMENT);
        GLES20.glCompileShader(f_shader);

        // 创建program,并链接着色器
        mProgram = GLES20.glCreateProgram();
        if (mProgram != 0) {
            GLES20.glAttachShader(mProgram, v_shader);
            GLES20.glAttachShader(mProgram, f_shader);
            GLES20.glLinkProgram(mProgram);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(TAG, "onSurfaceCreated: Could not link Program:" + GLES20.glGetProgramInfoLog(mProgram));
                GLES20.glDeleteProgram(mProgram);
                mProgram = 0;
            }
        }

        // 将坐标数据转换为FloatBuffer,用于传入opengl
        ByteBuffer bb = ByteBuffer.allocateDirect(triangleCoordinates.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(triangleCoordinates);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 整个buffer的清理
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 将program加载到opengl的环境里面
        GLES20.glUseProgram(mProgram);
        // 获取顶点着色器的成员句柄
        int vPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // 启用句柄
        GLES20.glEnableVertexAttribArray(vPositionHandle);
        // 准备图形的坐标信息
        GLES20.glVertexAttribPointer(vPositionHandle, CORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer);

        // 获取片元着色器的颜色句柄
        int vColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // 设置图形的颜色
        GLES20.glUniform4fv(vColorHandle, 1, vertexColor, 0);


        // 绘制图形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, VERTEX_COUNT);

        // 关闭顶点句柄
        GLES20.glDisableVertexAttribArray(vPositionHandle);

    }


}
