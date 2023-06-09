#version 100 es
#extension GL_OES_EGL_image_external_essl3 : require
precision mediump float;
uniform samplerExternalOES sTexture;
in vec2 vTextureCoord;
out vec4 gl_FragColor;

void main() {
    gl_FragColor = texture(sTexture, vTextureCoord);
}