precision mediump float;
varying vec2 tc;
uniform sampler2D tex;
void main() {
    vec4 tmpColor = texture(tex, tc);
    float weightMean = tmpColor.r * 0.3 + tmpColor.g * 0.59 + tmpColor.b * 0.11;
    tmpColor.r = tmpColor.g = tmpColor.b = weightMean;
    gl_FragColor = tmpColor;
}

