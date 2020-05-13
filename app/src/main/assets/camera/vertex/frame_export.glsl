attribute vec4 vPosition;
attribute vec2 textureCoor;
uniform mat4 vMatrix;

varying vec2 vTextureCoor;

void main(){
    gl_Position = vMatrix * vPosition;
    vTextureCoor = textureCoor;
}