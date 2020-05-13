package com.example.androidopenglesdemo.camera;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import com.example.androidopenglesdemo.utils.GLESUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DisplayRenderer extends IGlesFilter {

    public DisplayRenderer(Context context) {
        super(context);
        init();
    }

    @Override
    public void onRendererCreated(GL10 gl10, EGLConfig eglConfig) {
        glProgram = GLESUtils.createGlProgram(getContext(),
                "camera/vertex/display_renderer.glsl",
                "camera/fragment/display_renderer.glsl");
        vPosition = GLES20.glGetAttribLocation(glProgram, "vPosition");
        textureCoor = GLES20.glGetAttribLocation(glProgram, "textureCoor");
        vMatrix = GLES20.glGetUniformLocation(glProgram, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(glProgram, "vTexture");
    }

    @Override
    public void onDrawFrame(GL10 gl10) {

        GLES20.glUseProgram(glProgram);
        GLES20.glViewport(0, 0, getWidth(), getHeight());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getInputTexture());
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mCoorMatrixValue, 0);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 4 * 4 , mVertexBuffer);
        GLES20.glEnableVertexAttribArray(textureCoor);
        GLES20.glVertexAttribPointer(textureCoor, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(textureCoor);
    }

    @Override
    public void onRendererDestory() {

    }

    private static final String TAG = "GrayFilter";
    private int glProgram;
    private int vPosition;
    private int vMatrix;
    private int textureCoor;
    private int vTexture;


    private float[] mCoorMatrixValue = Arrays.copyOf(ORIGINAL_MASTRIX, ORIGINAL_MASTRIX.length);

    private static float[] ORIGINAL_MASTRIX = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };
    private static float[] VERTEX = new float[]{
            -1, 1,0,0,
            -1, -1,0,0,
            1, 1,0,0,
            1, -1,0,0
    };

    private static float[] TEXTURE = new float[]{
            0, 0,
            0, 1,
            1, 0,
            1, 1
    };
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    private void init() {
        mVertexBuffer = (FloatBuffer) ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX)
                .position(0);

        mTextureBuffer = (FloatBuffer) ByteBuffer.allocateDirect(TEXTURE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(TEXTURE)
                .position(0);
    }

    @Override
    public int getOutputTexture() {
        return getInputTexture();
    }

}
