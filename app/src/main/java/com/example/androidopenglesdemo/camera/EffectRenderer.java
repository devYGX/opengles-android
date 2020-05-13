package com.example.androidopenglesdemo.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.androidopenglesdemo.constants.Matrixs;
import com.example.androidopenglesdemo.glenv.GlesRenderer;
import com.example.androidopenglesdemo.utils.GLESUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EffectRenderer extends GlesRenderer implements IOutputFilter {
    private static final String TAG = "EffectRenderer";
    private ISurfaceTextureListener listener;

    private SurfaceTexture surfaceTexture;

    private int glProgram;
    private int mGlesVPosition;
    private int mGlesVTexturePosition;
    private int mGlesVMatrix;

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
    private float[] mMatrix = new float[16];
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mTextureBuffer;

    public EffectRenderer(Context context, int width, int height, ISurfaceTextureListener listener) {
        super(context, width, height);
        this.listener = listener;
        initBuffer();
    }

    private void initBuffer() {
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

        Matrix.scaleM(mMatrix, 0, Matrixs.ORIGINAL, 0, 1, -1, 1);
    }

    @Override
    public void onRendererCreated(GL10 gl10, EGLConfig eglConfig) {
        createSurfaceTextureId();
        surfaceTexture = new SurfaceTexture(osTexture[0]);
        if (listener != null) {
            listener.onSurfaceTextureAvailable(surfaceTexture);
        }
        GLES20.glClearColor(1, 1, 1, 1);
        glProgram = GLESUtils.createGlProgram(getContext(),
                "camera/vertex/camera_effect.glsl",
                "camera/fragment/camera_effect.glsl");

        mGlesVPosition = GLES20.glGetAttribLocation(glProgram, "vPosition");
        mGlesVTexturePosition = GLES20.glGetAttribLocation(glProgram, "vTexturePosition");
        mGlesVMatrix = GLES20.glGetUniformLocation(glProgram, "vMatrix");

        // -----
        createEnv();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (surfaceTexture != null) {
            surfaceTexture.updateTexImage();
        }

        bindFrameRenderBuffer(fFrame[0], fRender[0], fTexture[0]);
        GLES20.glViewport(0, 0, getWidth(), getHeight());

        GLES20.glUseProgram(glProgram);
        GLES20.glUniformMatrix4fv(mGlesVMatrix, 1, false, mMatrix, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, osTexture[0]);

        GLES20.glEnableVertexAttribArray(mGlesVPosition);
        GLES20.glEnableVertexAttribArray(mGlesVTexturePosition);
        GLES20.glVertexAttribPointer(mGlesVPosition, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer);
        GLES20.glVertexAttribPointer(mGlesVTexturePosition, 2, GLES20.GL_FLOAT, false, 2 * 4, mTextureBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        unBind();
    }

    @Override
    public void onRendererDestory() {
        deleteEnv();
        GLES20.glDeleteTextures(1, osTexture, 0);
    }

    @Override
    public int getOutputTexture() {
        return fTexture[0];
    }

    private int[] osTexture = new int[1];
    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[1];

    private int createSurfaceTextureId() {
        GLES20.glGenTextures(1, osTexture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, osTexture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return osTexture[0];
    }

    private void deleteEnv() {
        GLES20.glDeleteTextures(1, fTexture, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteRenderbuffers(1, fRender, 0);
    }

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
        unBind();
    }

    private void unBind() {
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    private void bindFrameRenderBuffer(int frameBuffer, int renderBuffer, int textureId) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER,
                GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, renderBuffer);
    }


}
