precision mediump float;

varying vec2 vTextureCoor;

uniform sampler2D vTexture;

void main(){
    // 取出颜色
    gl_FragColor = texture2D(vTexture,vTextureCoor);

}