package com.dds.gles;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "MyRenderer";
    int mProgram;


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 申请底色空间
        GLES30.glClearColor(0.14f, 0.1f, 0.1f, 1f);

        // 创建顶点着色器
        int v_shader = GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);
        if (v_shader == 0) {
            Log.d(TAG, "onSurfaceCreated: v_shader is 0");
            return;
        }
        // 将着色器的代码放入着色器中
        GLES30.glShaderSource(v_shader, ProgramUtil.SHADER_VERTEX);
        // 编译着色器
        GLES30.glCompileShader(v_shader);

        // 创建片元着色器
        int f_shader = GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);
        if (f_shader == 0) {
            Log.d(TAG, "onSurfaceCreated: f_shader is 0");
            return;
        }
        GLES30.glShaderSource(f_shader, ProgramUtil.SHADER_FRAGMENT);
        GLES30.glCompileShader(f_shader);

        mProgram = GLES30.glCreateProgram();
        if (mProgram != 0) {
            GLES30.glAttachShader(mProgram, v_shader);
            GLES30.glAttachShader(mProgram, f_shader);
            GLES30.glLinkProgram(mProgram);
        }


    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES30.glViewport(0, 0, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {

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
