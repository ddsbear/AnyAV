#version 100

attribute vec4 vPosition;
attribute vec2 vCoordinate;
uniform mat4 vMatrix;
varying vec2 textureCoordinate;
void main() {
    gl_Position = vPosition;
//    textureCoordinate = vCoordinate;
    textureCoordinate = (vMatrix * vec4(vCoordinate, 1.0, 1.0)).xy;
}