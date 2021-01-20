precision mediump float;

varying vec2 vTextureCoor;

uniform sampler2D vTexture;

void main(){
    // 取出颜色
    // gl_FragColor = texture2D(vTexture,vTextureCoor);
    vec4 color = texture2D(vTexture, vTextureCoor);
    float grayColor = (color.r+color.g+color.b) / 3.0;
    gl_FragColor = vec4(grayColor, grayColor, grayColor, color.a);
}