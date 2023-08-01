package com.dds.gles.demo3.render;

import android.opengl.GLES20;
import android.util.Log;

public class GlShader {
    private static final String TAG = "GlShader";

    private static int compileShader(int shaderType, String source) {
        final int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) {
            throw new RuntimeException("glCreateShader() failed. GLES20 error: " + GLES20.glGetError());
        }
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compileStatus = new int[]{GLES20.GL_FALSE};
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Compile error " + GLES20.glGetShaderInfoLog(shader) + " in shader:\n" + source);
            throw new RuntimeException(GLES20.glGetShaderInfoLog(shader));
        }
        GLESTool.checkGlError("compileShader");
        return shader;
    }

    private int program;

    public GlShader(String vertexSource, String fragmentSource) {
        final int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        final int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        program = GLES20.glCreateProgram();
        if (program == 0) {
            throw new RuntimeException("glCreateProgram() failed. GLES20 error: " + GLES20.glGetError());
        }
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[]{GLES20.GL_FALSE};
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            Log.e(TAG, "Could not link program: " + GLES20.glGetProgramInfoLog(program));
            throw new RuntimeException(GLES20.glGetProgramInfoLog(program));
        }
        // According to the documentation of glLinkProgram():
        // "After the link operation, applications are free to modify attached shader objects, compile
        // attached shader objects, detach shader objects, delete shader objects, and attach additional
        // shader objects. None of these operations affects the information log or the program that is
        // part of the program object."
        // But in practice, detaching shaders from the program seems to break some devices. Deleting the
        // shaders are fine however - it will delete them when they are no longer attached to a program.
        GLES20.glDeleteShader(vertexShader);
        GLES20.glDeleteShader(fragmentShader);
        GLESTool.checkGlError("Creating GlShader");
    }

    public int getAttribLocation(String label) {
        if (program == -1) {
            throw new RuntimeException("The program has been released");
        }
        int location = GLES20.glGetAttribLocation(program, label);
        if (location < 0) {
            throw new RuntimeException("Could not locate '" + label + "' in program");
        }
        return location;
    }

    public int getUniformLocation(String label) {
        if (program == -1) {
            throw new RuntimeException("The program has been released");
        }
        int location = GLES20.glGetUniformLocation(program, label);
        if (location < 0) {
            throw new RuntimeException("Could not locate uniform '" + label + "' in program");
        }
        return location;
    }

    public void useProgram() {
        if (program == -1) {
            throw new RuntimeException("The program has been released");
        }
        GLES20.glUseProgram(program);
        GLESTool.checkGlError("glUseProgram");
    }

    public void release() {
        Log.d(TAG, "Deleting shader.");
        // Delete program, automatically detaching any shaders from it.
        if (program != -1) {
            GLES20.glDeleteProgram(program);
            program = -1;
        }
    }

}
