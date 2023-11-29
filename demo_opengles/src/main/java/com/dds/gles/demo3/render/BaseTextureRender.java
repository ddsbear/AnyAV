package com.dds.gles.demo3.render;

import java.nio.FloatBuffer;

public class BaseTextureRender {
    protected static final int FLOAT_SIZE_BYTES = 4;
    protected final FloatBuffer mRegularTriangleVertices;

    // Sampling is 1:1 for a straight copy for the back camera
    protected static final float[] sRegularTriangleVertices = {
            // X,   Y,     Z, U,   V
            -1.0f, -1.0f, 0, 0.f, 0.f,    // left  bottom
            1.0f, -1.0f, 0, 1.f, 0.f,    // right bottom
            -1.0f, 1.0f, 0, 0.f, 1.f,    // left top
            1.0f, 1.0f, 0, 1.f, 1.f,    // right top
    };

    protected final FloatBuffer mHorizontalFlipTriangleVertices;
    // Sampling is mirrored across the horizontal axis
    protected static final float[] sHorizontalFlipTriangleVertices = {
            // X,   Y,      Z, U,   V
            -1.0f, -1.0f, 0, 1.f, 0.f,
            1.0f, -1.0f, 0, 0.f, 0.f,
            -1.0f, 1.0f, 0, 1.f, 1.f,
            1.0f, 1.0f, 0, 0.f, 1.f,
    };

    protected final FloatBuffer mVerticalFlipTriangleVertices;

    // Sampling is mirrored across the vertical axis
    protected static final float[] sVerticalFlipTriangleVertices = {
            // X,  Y,     Z, U,   V
            -1.0f, -1.0f, 0, 0.f, 1.f,
            1.0f, -1.0f, 0, 1.f, 1.f,
            -1.0f, 1.0f, 0, 0.f, 0.f,
            1.0f, 1.0f, 0, 1.f, 0.f,
    };
    protected final FloatBuffer mBothFlipTriangleVertices;

    // Sampling is mirrored across the both axes
    protected static final float[] sBothFlipTriangleVertices = {
            // X, Y,      Z, U, V
            -1.0f, -1.0f, 0, 1.f, 1.f,
            1.0f, -1.0f, 0, 0.f, 1.f,
            -1.0f, 1.0f, 0, 1.f, 0.f,
            1.0f, 1.0f, 0, 0.f, 0.f,
    };

    protected static final FloatBuffer FULL_RECTANGLE_TEXTURE_BUFFER =
            GLESTool.createFloatBuffer(new float[]{
                    0.0f, 0.0f, // Bottom left.
                    1.0f, 0.0f, // Bottom right.
                    0.0f, 1.0f, // Top left.
                    1.0f, 1.0f, // Top right.
            });


    protected static final String VERTEX_SHADER_OES =
            "attribute vec4 aPosition;\n" +
                    "attribute vec4 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    protected static final String FRAGMENT_SHADER_OES =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";


    public BaseTextureRender() {
        mRegularTriangleVertices = GLESTool.createFloatBuffer(sRegularTriangleVertices);
        mHorizontalFlipTriangleVertices = GLESTool.createFloatBuffer(sHorizontalFlipTriangleVertices);
        mVerticalFlipTriangleVertices = GLESTool.createFloatBuffer(sVerticalFlipTriangleVertices);
        mBothFlipTriangleVertices = GLESTool.createFloatBuffer(sBothFlipTriangleVertices);
    }
}
