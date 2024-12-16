package com.dds.gles.demo1.render;

public class TextureBaseInfo {
    // 顶点坐标
    public static final float[] sPosition = {
            -1.0f, 1.0f,    //left-up        1-------3
            -1.0f, -1.0f,   //left-bottom    |    /  |
            1.0f, 1.0f,     //right-up       |  /    |
            1.0f, -1.0f     //right-bottom   2-------4
    };

    // 纹理坐标
    public static final float[] sCoordinate = {
            0.0f, 1.0f,       // left-up
            0.0f, 0.0f,       // left-bottom
            1.0f, 1.0f,       // right-up
            1.0f, 0.0f,       // right-bottom
    };

    public static final String VERTEX_SHADER_CAMERA = "attribute vec4 vPosition;\n" +
            "attribute vec4 vCoordinate;\n" +
            "uniform mat4 vMatrix;\n" +
            "varying vec2 tc;\n" +
            "void main() {\n" +
            "    gl_Position = vPosition;\n" +
            "    tc = (vMatrix * vCoordinate).xy;\n" +
            "}";

    public static final String FRAGMENT_SHADER_CAMERA = "#extension GL_OES_EGL_image_external: require\n" +
            "precision mediump float;\n" +
            "varying vec2 tc;\n" +
            "uniform samplerExternalOES vTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(vTexture,tc);\n" +
            "}";

    public static final String FRAGMENT_SHADER_FBO = "#extension GL_OES_EGL_image_external: require\n" +
            "precision mediump float;\n" +
            "varying vec2 tc;\n" +
            "uniform sampler2D vTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D(vTexture,tc);\n" +
            "}";

    public static final String FRAGMENT_SHADER_FILTER = "precision mediump float;\n"
            + "varying vec2 tc;\n"
            + "uniform sampler2D vTexture;\n"
            + "void main(){\n"
            + "  vec4 mask = texture2D(vTexture, tc);\n"
            + "  gl_FragColor = vec4(mask.r,mask.g,mask.g,1.0);\n"
            + "}";

}
