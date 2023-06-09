package com.dds.gles.demo3.render;

import android.opengl.EGL14;
import android.opengl.GLException;
import android.opengl.GLUtils;

public class EglError extends RuntimeException {

    public EglError(String error) {
        super(error + GLUtils.getEGLErrorString(EGL14.eglGetError()));
    }

}
