precision mediump float;
varying vec2 vTextureCoor;

uniform sampler2D vTexture;
void main(){
    gl_FragColor = texture2D(vTexture, vTextureCoor);
}