package com.dds.gles.demo2.render;

import java.nio.FloatBuffer;

public class BaseTextureRender {
    protected static final int FLOAT_SIZE_BYTES = 4;
    protected final FloatBuffer mRegularTriangleVertices;
    protected final FloatBuffer mHorizontalFlipTriangleVertices;
    protected final FloatBuffer mVerticalFlipTriangleVertices;
    protected final FloatBuffer mBothFlipTriangleVertices;

    // Sampling is 1:1 for a straight copy for the back camera
    protected static final float[] sRegularTriangleVertices = {
            // X,   Y,     Z, U,   V
            -1.0f, -1.0f, 0, 0.f, 0.f,    // left  bottom
            1.0f, -1.0f, 0, 1.f, 0.f,    // right bottom
            -1.0f, 1.0f, 0, 0.f, 1.f,    // left top
            1.0f, 1.0f, 0, 1.f, 1.f,    // right top
    };
    // Sampling is mirrored across the horizontal axis
    protected static final float[] sHorizontalFlipTriangleVertices = {
            // X,   Y,      Z, U,   V
            -1.0f, -1.0f, 0, 1.f, 0.f,
            1.0f, -1.0f, 0, 0.f, 0.f,
            -1.0f, 1.0f, 0, 1.f, 1.f,
            1.0f, 1.0f, 0, 0.f, 1.f,
    };
    // Sampling is mirrored across the vertical axis
    protected static final float[] sVerticalFlipTriangleVertices = {
            // X,  Y,     Z, U,   V
            -1.0f, -1.0f, 0, 0.f, 1.f,
            1.0f, -1.0f, 0, 1.f, 1.f,
            -1.0f, 1.0f, 0, 0.f, 0.f,
            1.0f, 1.0f, 0, 1.f, 0.f,
    };
    // Sampling is mirrored across the both axes
    protected static final float[] sBothFlipTriangleVertices = {
            // X, Y,      Z, U, V
            -1.0f, -1.0f, 0, 1.f, 1.f,
            1.0f, -1.0f, 0, 0.f, 1.f,
            -1.0f, 1.0f, 0, 1.f, 0.f,
            1.0f, 1.0f, 0, 0.f, 0.f,
    };


    public static final String VERTEX_SHADER_DEFAULT =
            "attribute vec4 in_pos;\n" +
                    "attribute vec4 in_tc;\n" +
                    "varying vec2 tc;\n" +
                    "uniform mat4 tex_mat;\n" +
                    "void main() {\n" +
                    "  gl_Position = in_pos;\n" +
                    "  tc = (tex_mat * in_tc).xy;\n" +
                    "}\n";

    protected static final String FRAGMENT_SHADER_OES =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "varying vec2 tc;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, tc);\n" +
                    "}\n";

    protected static final String FRAGMENT_SHADER_RGB =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform sampler2D sTexture;\n" +
                    "varying vec2 tc;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, tc);\n" +
                    "}\n";


    public BaseTextureRender() {
        mRegularTriangleVertices = GLESTool.createFloatBuffer(sRegularTriangleVertices);
        mHorizontalFlipTriangleVertices = GLESTool.createFloatBuffer(sHorizontalFlipTriangleVertices);
        mVerticalFlipTriangleVertices = GLESTool.createFloatBuffer(sVerticalFlipTriangleVertices);
        mBothFlipTriangleVertices = GLESTool.createFloatBuffer(sBothFlipTriangleVertices);


    }
}
