package com.example.androidopenglesdemo.glrendershape;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.example.androidopenglesdemo.constants.Matrixs;

import javax.microedition.khronos.opengles.GL10;

public abstract class ShapeRender implements GLSurfaceView.Renderer {

    private final Context context;

    private float[] mMatrixValue = new float[16];

    public ShapeRender(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float[] frustumM = new float[16];
        float[] setLookAtM = new float[16];
        float[] multiplyResult = new float[16];

        // opengl里, 模也好, 这里的ratio也好, 值大于1时是缩小, 小于1时是放大;
        // https://blog.csdn.net/jamesshaoya/article/details/54342241 参考;
        float ratio;
        if (height > width) {
            ratio = height * 1.0F / width;
            // 正交投影, 图形的大小不受到near和far的改变而改变;
            Matrix.orthoM(frustumM, 0, -1, 1, -ratio, ratio, 5, 10);
        } else {
            ratio = width * 1.0F / height;
            // 正交投影, 图形的大小不受到near和far的改变而改变;
            Matrix.orthoM(frustumM, 0, -ratio, ratio, -1, 1, 5, 10);
        }

        // 设置相机视角
        // eyeX, eyeY, eyeZ表示相机的坐标点, 这里传入的是0,0,5, 表示相机处于图形Z轴正上面
        // upX, upY, upZ表示的是相机的朝向; 设置upZ不为0时, 将看不到图像效果;
        Matrix.setLookAtM(setLookAtM, 0, 0, 0, 6, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(multiplyResult, 0, frustumM, 0, setLookAtM, 0);

        Matrix.multiplyMM(mMatrixValue, 0, multiplyResult, 0, Matrixs.ORIGINAL, 0);
    }

    public float[] getMatrixValue() {
        return mMatrixValue;
    }
}
