package com.example.androidopenglesdemo.glrendershape.impls;

import android.content.Context;
import android.opengl.GLES20;

import com.example.androidopenglesdemo.glrendershape.ShapeRender;
import com.example.androidopenglesdemo.utils.GLESUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 扇形
 */
public class Sector extends ShapeRender {
    private int n = 360;
    private int start = 30;
    private int end = 90;
    private float radius = 0.5F;
    private FloatBuffer mCircleCoorBuffer;
    private int glProgram;
    private int vPosition;
    private int vMatrix;
    private int vColor;
    private FloatBuffer mColorBuffer;

    public Sector(Context context) {
        super(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        initRender();
        glProgram = GLESUtils.createGlProgram(getContext(),
                "sample2/vertex/shape_base_triangle.glsl",
                "sample2/fragment/shape_base_triangle.glsl");
        vPosition = GLES20.glGetAttribLocation(glProgram, "vPosition");
        vMatrix = GLES20.glGetUniformLocation(glProgram, "vMatrix");
        vColor = GLES20.glGetUniformLocation(glProgram, "vColor");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(1, 1, 1, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(glProgram);
        GLES20.glUniform4fv(vColor, 1, mColorBuffer);
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, getMatrixValue(), 0);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition,
                2,
                GLES20.GL_FLOAT,
                false, 3 * 4, mCircleCoorBuffer);
        // count表示绘制的顶点数
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, n + 1 + 1);
        GLES20.glDisableVertexAttribArray(vPosition);
    }

    private void initRender() {

        // n个三角形, 会产生n + 1 + 1个顶点; 1个是圆心, n+1是n个三角形的点数量; 比如: 2个三角形, 需要1个圆心, 3个点; 三个三角形, 需要1个圆心, 4个点;...
        // 每个顶点占3位元素
        float[] vertexCoordinateArray = new float[(n + 1 + 1) * 3];

        int index = 0;

        // 圆心坐标: x = 0; y = 0; z = 0;
        vertexCoordinateArray[index++] = 0;
        vertexCoordinateArray[index++] = 0;
        vertexCoordinateArray[index++] = 0;
        // 将360平分为N分;
        double ag = 360D / n;
        for (double i = start; i < end + ag; i += ag) {

            // i -

            double angle = i * Math.PI / 180d;
            vertexCoordinateArray[index++] = (float) (radius * Math.sin(angle));
            vertexCoordinateArray[index++] = (float) (radius * Math.cos(angle));
            vertexCoordinateArray[index++] = 0;
        }

        // 将定点坐标放到FloatBuffer中;
        // 只能先创建指定长度的ByteBuffer, 而后将其转换为FloatBuffer
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertexCoordinateArray.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        mCircleCoorBuffer = (FloatBuffer) byteBuffer.asFloatBuffer()
                .put(vertexCoordinateArray)
                .position(0);

        float[] color = new float[]{1, 0, 0, 1};
        mColorBuffer = (FloatBuffer) ByteBuffer.allocateDirect(color.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(color)
                .position(0);
    }
}
