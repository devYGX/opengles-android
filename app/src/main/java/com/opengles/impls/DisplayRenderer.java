package com.opengles.impls;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.opengles.AbsGlesFilter;
import com.opengles.GLESUtils;
import com.opengles.MatrixSources;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class DisplayRenderer extends AbsGlesFilter {
    private static final String TAG = "DisplayRenderer";
    private int windowHeight;
    private int windowWidth;
    private boolean hasFilter;
    private int width;

    public int getWindowHeight() {
        return windowHeight;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public DisplayRenderer(Context context) {
        super(context);
        init();
    }

    public void onWindowSizeChanged(int windowWidth, int windowHeight,
                                    int width, int height) {

        this.windowHeight = windowHeight;
        this.windowWidth = windowWidth;

    }

    @Override
    public void onDraw() {

        GLES20.glClearColor(1, 1, 1, 1);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(glProgram);
        GLES20.glViewport(0, 0, getWindowWidth(), getWindowHeight());
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getInputTexture());
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, mCoorMatrixValue, 0);

        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(textureCoor);
        GLES20.glVertexAttribPointer(textureCoor, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(vPosition);
        GLES20.glDisableVertexAttribArray(textureCoor);
    }

    private int glProgram;
    private int vPosition;
    private int vMatrix;
    private int textureCoor;
    private int vTexture;


    private float[] mCoorMatrixValue = MatrixSources.ORIGINAL;

    private static float[] VERTEX = new float[]{
            -1, 1, 0, 0,
            -1, -1, 0, 0,
            1, 1, 0, 0,
            1, -1, 0, 0
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

    public void setHasFilter(boolean hasFilter) {
        if (this.hasFilter == hasFilter) return;

        this.hasFilter = hasFilter;
        mCoorMatrixValue = new float[16];
        Matrix.scaleM(mCoorMatrixValue, 0, MatrixSources.ORIGINAL, 0, 1, hasFilter ? -1 : 1, 1);
    }

    @Override
    public void onFilterCreated(int width, int height) {
        super.onFilterCreated(width, height);
        glProgram = GLESUtils.createGlProgram(getContext(),
                "opengles/vertex/display_renderer.glsl",
                "opengles/fragment/display_renderer.glsl");
        vPosition = GLES20.glGetAttribLocation(glProgram, "vPosition");
        textureCoor = GLES20.glGetAttribLocation(glProgram, "textureCoor");
        vMatrix = GLES20.glGetUniformLocation(glProgram, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(glProgram, "vTexture");
    }

    @Override
    public void onFilterDestory() {

    }
}
