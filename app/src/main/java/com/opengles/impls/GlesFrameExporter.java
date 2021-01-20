package com.opengles.impls;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;
import android.util.SparseArray;

import com.opengles.AbsGlesFilter;
import com.opengles.GLESUtils;
import com.opengles.IFrameExportCallback;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;


public class GlesFrameExporter extends AbsGlesFilter {
    private static final String TAG = "GlesFrameExporter";
    private int glProgram;
    private int vMatrix;
    private int vPosition;
    private int textureCoor;
    private SparseArray<IFrameExportCallback> callbacks = new SparseArray<>();

    private ByteBuffer byteBuffer;

    public GlesFrameExporter(Context context) {
        super(context);
        init();
    }

    private float[] mCoorMatrixValue = Arrays.copyOf(ORIGINAL_MASTRIX, ORIGINAL_MASTRIX.length);

    private static float[] ORIGINAL_MASTRIX = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
    };
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

    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[1];

    private void createEnv() {
        GLES20.glGenFramebuffers(1, fFrame, 0);
        GLES20.glGenRenderbuffers(1, fRender, 0);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER,
                GLES20.GL_DEPTH_COMPONENT16,
                getWidth(), getHeight());
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, fTexture[0], 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, fRender[0]);

        GLES20.glGenTextures(1, fTexture, 0);
        Log.d(TAG, "createEnv: " + getWidth() + ", " + getHeight());

        for (int i = 0; i < fTexture.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[i]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D,
                    0,
                    GLES20.GL_RGBA,
                    getWidth(),
                    getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        unBind();
    }


    private void deleteEnv() {
        GLES20.glDeleteTextures(1, fTexture, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteRenderbuffers(1, fRender, 0);
    }

    @Override
    public void onDraw() {

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fTexture[0], 0);


        GLES20.glUseProgram(glProgram);
        GLES20.glViewport(0, 0, getWidth(), getHeight());
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

        if (callbacks.size() > 0) {
            if (byteBuffer == null) {
                byteBuffer = ByteBuffer.allocate(getWidth() * getHeight() * 4);
            }
            byteBuffer.position(0);
            GLES20.glReadPixels(0, 0, getWidth(), getHeight(),
                    GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, byteBuffer);
            byteBuffer.position(0);
            synchronized (this) {
                for (int i = 0; i < callbacks.size(); i++) {
                    callbacks.get(callbacks.keyAt(i))
                            .onFrameExport(byteBuffer, getWidth(), getHeight());
                }
            }
        }
        unBind();
    }

    @Override
    public void onFilterDestory() {
        deleteEnv();
    }


    @Override
    public void onFilterCreated(int width, int height) {
        super.onFilterCreated(width, height);
        glProgram = GLESUtils.createGlProgram(getContext(),
                "opengles/vertex/frame_export.glsl",
                "opengles/fragment/frame_export.glsl");
        vMatrix = GLES20.glGetUniformLocation(glProgram, "vMatrix");
        vPosition = GLES20.glGetAttribLocation(glProgram, "vPosition");
        textureCoor = GLES20.glGetAttribLocation(glProgram, "textureCoor");
        createEnv();
    }

    @Override
    public void onFilterSizeChanged(int width, int height) {
        super.onFilterSizeChanged(width, height);
        deleteEnv();
        createEnv();
    }

    @Override
    public int getOutputTexture() {
        return getInputTexture();
    }


    private void unBind() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    public synchronized void addFrameExportCallback(IFrameExportCallback callback) {
        callbacks.put(callback.hashCode(), callback);
    }

    public synchronized void removeFrameExportCallback(IFrameExportCallback callback) {
        callbacks.remove(callback.hashCode());
    }
}
