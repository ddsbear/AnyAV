package com.dds.gles.demo2.render.filter;

import android.opengl.GLES20;

import com.dds.gles.demo2.render.BaseTextureRender;
import com.dds.gles.render.GlShader;

public class SkinSmoothFilter {
    private GlShader shader;
    private float stepScale = 3;
    private int xStepLoc;
    private int yStepLoc;

    public void prepare() {
        if (shader == null) {
            shader = new GlShader(getVertexShader(), getGreyFragmentShader());
            yStepLoc = shader.getUniformLocation("yStep");
            xStepLoc = shader.getUniformLocation("xStep");
        }

    }

    public void setStepScale(float stepScale) {
        this.stepScale = stepScale;
    }

    public void draw(float[] mSTMatrix, int mWidth, int mHeight) {

        shader.useProgram();


        GLES20.glUniformMatrix4fv(shader.getUniformLocation("tex_mat"), 1, false, mSTMatrix, 0);

        GLES20.glUniform1i(shader.getUniformLocation("sTexture"), 0);

        GLES20.glUniform1f(xStepLoc, stepScale / mWidth);
        GLES20.glUniform1f(yStepLoc, stepScale / mHeight);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

    }

    public void release() {
        if (shader != null) {
            shader.release();
        }

    }

    private String getVertexShader() {
        return BaseTextureRender.VERTEX_SHADER_DEFAULT;
    }

    private String getGreyFragmentShader() {
        return "precision mediump float;\n" +
                "uniform sampler2D sTexture;\n" +
                "varying mediump vec2 tc;\n" +
                "const float maxdelta = 0.08;\n" +
                "uniform mediump float xStep;\n" +
                "uniform mediump float yStep;\n" +
                "const mediump mat3 rgb2yuv = mat3(0.299,-0.147,0.615,0.587,-0.289,-0.515,0.114,0.436,-0.1);\n" +
                "const mediump mat3 gaussianMap = mat3(0.142,0.131,0.104,0.131,0.122,0.096,0.104,0.096,0.075);\n" +
                "void main(){\n" +
                "    vec4 color = texture2D(sTexture,tc);\n" +
                "    vec3 yuv = rgb2yuv*color.rgb;\n" +
                "    if(yuv.g<-0.225 || yuv.g>0.0 || yuv.b<0.022 || yuv.b>0.206){\n" +
                "        gl_FragColor = color;\n" +
                "        return;\n" +
                "    }\n" +
                "    float xfS = tc.x - xStep*2.0;\n" +
                "    float yf = tc.y - yStep*2.0;\n" +
                "    int x,y;\n" +
                "    float xf=xfS;\n" +
                "    vec4 sum=vec4(0.0,0.0,0.0,0.0);\n" +
                "    vec4 fact=vec4(0.0,0.0,0.0,0.0);\n" +
                "    vec4 tmp;\n" +
                "    vec4 color2;\n" +
                "    float gauss;\n" +
                "    for(y=-2;y<3;y+=1){\n" +
                "        if (yf < 0.0 || yf > 1.0){\n" +
                "            yf+=yStep;\n" +
                "            continue;\n" +
                "        }\n" +
                "        for(x=-2;x<3;x+=1){\n" +
                "            if (xf < 0.0 || xf > 1.0){\n" +
                "                xf+=xStep;\n" +
                "                continue;\n" +
                "            }\n" +
                "            color2 = texture2D(sTexture,vec2(xf,yf));\n" +
                "            tmp = color - color2;\n" +
                "            gauss = gaussianMap[x<0?-x:x][y<0?-y:y];\n" +
                "            if (abs(tmp.r) < maxdelta){\n" +
                "                sum.r += (color2.r*gauss);\n" +
                "                fact.r +=gauss;\n" +
                "            }\n" +
                "            if (abs(tmp.g) < maxdelta){\n" +
                "                sum.g += color2.g*gauss;\n" +
                "                fact.g +=gauss;\n" +
                "            }\n" +
                "            if (abs(tmp.b) < maxdelta){\n" +
                "                sum.b += color2.b*gauss;\n" +
                "                fact.b +=gauss;\n" +
                "            }\n" +
                "            xf+=xStep;\n" +
                "        }\n" +
                "        yf+=yStep;\n" +
                "        xf=xfS;\n" +
                "    }\n" +
                "    vec4 res = sum/fact;\n" +
                "    if(fact.r<1.0){\n" +
                "        tmp.r = color.r;\n" +
                "    }else{\n" +
                "        tmp.r = res.r;\n" +
                "    }\n" +
                "    if(fact.g<1.0){\n" +
                "        tmp.g = color.g;\n" +
                "    }else{\n" +
                "        tmp.g = res.g;\n" +
                "    }\n" +
                "    if(fact.b<1.0){\n" +
                "        tmp.b = color.b;\n" +
                "    }else{\n" +
                "        tmp.b = res.b;\n" +
                "    }\n" +
                "    gl_FragColor = vec4(tmp.rgb,1.0);\n" +
                "}\n";
    }
}
