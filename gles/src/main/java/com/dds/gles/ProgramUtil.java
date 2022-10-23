package com.dds.gles;

public class ProgramUtil {

    public static String SHADER_VERTEX =
            "attribute vec4 vPosition;\n" +
                    "void main{\n" +
                    "   gl_Position = vPosition;\n" +
                    "}";
    public static String SHADER_FRAGMENT =
            "precision mediump float;\n" +
                    "uniform vec4 vColor;" +
                    "void main{" +
                    "   gl_FragColor = vColor"+
                    "}";
}
