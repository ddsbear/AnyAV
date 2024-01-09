package com.dds.gles.demo2.render.filter;

import android.opengl.GLES20;

import com.dds.gles.demo2.render.BaseTextureRender;
import com.dds.gles.demo2.render.GlShader;


public class GreyFilter {
    private GlShader shader;

    public void prepare() {
        if (shader == null) {
            shader = new GlShader(getVertexShader(), getGreyFragmentShader());
        }

    }

    public void draw(float[] mSTMatrix) {

        shader.useProgram();

        GLES20.glUniformMatrix4fv(shader.getUniformLocation("tex_mat"), 1, false, mSTMatrix, 0);

        GLES20.glUniform1i(shader.getUniformLocation("sTexture"), 0);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }


    public void destroy() {
        if (shader != null) {
            shader.release();
        }

    }

    private String getVertexShader() {
        return BaseTextureRender.VERTEX_SHADER_DEFAULT;
    }

    // 灰度
    private String getGreyFragmentShader() {
        return "precision mediump float;\n" +
                "varying vec2 tc;\n" +
                "uniform sampler2D sTexture;\n" +
                "void main() {\n" +
                "    vec4 mask = texture2D(sTexture, tc);\n" +
                "    gl_FragColor = vec4(mask.g,mask.g,mask.g,1.0);\n" +
                "}";
    }

}
