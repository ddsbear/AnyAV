package com.dds.gles.demo3.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BaseTextureRender {
    protected static final int FLOAT_SIZE_BYTES = 4;
    protected final FloatBuffer mRegularTriangleVertices;
    // Sampling is 1:1 for a straight copy for the back camera
    protected static final float[] sRegularTriangleVertices = {
            // X,   Y,     Z, U,   V
            -1.0f, -1.0f,  0, 0.f, 0.f,   // left  bottom
            1.0f,  -1.0f,  0, 1.f, 0.f,    // right bottom
            -1.0f, 1.0f,   0, 0.f, 1.f,    // left top
            1.0f,  1.0f,   0, 1.f, 1.f,     // right top
    };

    protected final FloatBuffer mHorizontalFlipTriangleVertices;
    protected static final float[] sHorizontalFlipTriangleVertices = {
            // X,   Y,      Z, U,   V
            -1.0f,  -1.0f,  0, 1.f, 0.f,
            1.0f,   -1.0f,  0, 0.f, 0.f,
            -1.0f,  1.0f,   0, 1.f, 1.f,
            1.0f,   1.0f,   0, 0.f, 1.f,
    };

    protected final FloatBuffer mVerticalFlipTriangleVertices;
    // Sampling is mirrored across the vertical axis
    protected static final float[] sVerticalFlipTriangleVertices = {
            // X,  Y,     Z, U,   V
            -1.0f, -1.0f, 0, 0.f, 1.f,
            1.0f,  -1.0f, 0, 1.f, 1.f,
            -1.0f, 1.0f,  0, 0.f, 0.f,
            1.0f,  1.0f,  0, 1.f, 0.f,
    };
    protected final FloatBuffer mBothFlipTriangleVertices;
    // Sampling is mirrored across the both axes
    protected static final float[] sBothFlipTriangleVertices = {
            // X, Y,      Z, U, V
            -1.0f, -1.0f, 0, 1.f, 1.f,
            1.0f,  -1.0f, 0, 0.f, 1.f,
            -1.0f, 1.0f,  0, 1.f, 0.f,
            1.0f,  1.0f,  0, 0.f, 0.f,
    };


    protected static final String VERTEX_SHADER_OES =
                    "#version 300 es\n" +
                    "uniform mat4 uMVPMatrix;\n" +
                    "uniform mat4 uSTMatrix;\n" +
                    "in vec4 aPosition;\n" +
                    "in vec4 aTextureCoord;\n" +
                    "out vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = (uSTMatrix * aTextureCoord).xy;\n" +
                    "}\n";

    protected static final String FRAGMENT_SHADER_OES =
                    "#version 300 es\n" +
                    "#extension GL_OES_EGL_image_external_essl3 : require\n" +
                    "precision mediump float;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "in vec2 vTextureCoord;\n" +
                    "out vec4 gl_FragColor;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture(sTexture, vTextureCoord);\n" +
                    "}\n";


    public BaseTextureRender() {
        mRegularTriangleVertices = ByteBuffer.allocateDirect(sRegularTriangleVertices.length *
                FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        mRegularTriangleVertices.put(sRegularTriangleVertices).position(0);

        mHorizontalFlipTriangleVertices = ByteBuffer.allocateDirect(
                        sHorizontalFlipTriangleVertices.length * FLOAT_SIZE_BYTES).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mHorizontalFlipTriangleVertices.put(sHorizontalFlipTriangleVertices).position(0);

        mVerticalFlipTriangleVertices = ByteBuffer.allocateDirect(
                        sVerticalFlipTriangleVertices.length * FLOAT_SIZE_BYTES).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVerticalFlipTriangleVertices.put(sVerticalFlipTriangleVertices).position(0);

        mBothFlipTriangleVertices = ByteBuffer.allocateDirect(
                        sBothFlipTriangleVertices.length * FLOAT_SIZE_BYTES).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mBothFlipTriangleVertices.put(sBothFlipTriangleVertices).position(0);
    }
}
