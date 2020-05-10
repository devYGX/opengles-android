package com.example.androidopenglesdemo.sample1.shape;

import android.content.Context;
import android.opengl.GLES20;

import com.example.androidopenglesdemo.sample1.ShapeRender;
import com.example.androidopenglesdemo.utils.GLESUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class Triangle extends ShapeRender {

    private FloatBuffer mColorFloatBuffer;
    private FloatBuffer mVertexCoorBuffer;
    private int glProgram;
    private int vPosition;
    private int vColor;

    public Triangle(Context context) {
        super(context);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        initBuffer();

        glProgram = GLESUtils.createGlProgram(getContext(),
                "sample1/vertex/shape_triangle.glsl",
                "sample1/fragment/shape_triangle.glsl");

        // 找到vertex/shape_triangle.glsl中编写的 attribute vec4 vPosition 句柄;
        // 后续绘制时将通过此句柄将三角形顶点坐标传递给glsl程序
        vPosition = GLES20.glGetAttribLocation(glProgram, "vPosition");

        // 找到fragment/shape_triangle.glsl中编写的 uniform vec4 vColor 句柄
        // 后续绘制时将通过此句柄将颜色传递给glsl程序
        vColor = GLES20.glGetUniformLocation(glProgram, "vColor");
    }

    @Override
    public void onSurfaceChanged(GL10 gl, final int width, final int height) {

        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        // 清屏
        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 使用程式
        GLES20.glUseProgram(glProgram);

        // 将颜色传给glsl vColor句柄
        GLES20.glUniform4fv(vColor, 1, mColorFloatBuffer);

        // 传入顶点坐标
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 3, GLES20.GL_FLOAT, false, 4 * 4, mVertexCoorBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 3);
        GLES20.glDisableVertexAttribArray(vPosition);

    }

    private void initBuffer() {
        float[] color = new float[]{
                1.0F, 0, 0, 1.0F
        };
        mColorFloatBuffer = (FloatBuffer) ByteBuffer.allocateDirect(color.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(color)
                .position(0);

        float[] vertexCoor = new float[]{
                0.0F, 1.0F, 0.0F, 3.0F,
                -1.0F, -1.0F, 0.0F, 3.0F,
                1.0F, -1.0F, 0.0F, 3.0F,
        };
        mVertexCoorBuffer = (FloatBuffer) ByteBuffer.allocateDirect(vertexCoor.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexCoor)
                .position(0);

    }
}
