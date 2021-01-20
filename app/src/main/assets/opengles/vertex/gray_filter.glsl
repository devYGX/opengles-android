
// 顶点矩阵
uniform mat4 vMatrix;

// 顶点坐标
attribute vec4 vPosition;

// 纹理坐标
attribute vec2 textureCoor;

// 传递给片元着色器的纹理坐标
varying vec2 vTextureCoor;

void main(){
    gl_Position = vMatrix * vPosition;
    vTextureCoor = textureCoor;
}
