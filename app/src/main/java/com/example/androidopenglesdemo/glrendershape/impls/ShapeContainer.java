package com.example.androidopenglesdemo.glrendershape.impls;

import android.content.Context;

import com.example.androidopenglesdemo.glrendershape.ShapeRender;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ShapeContainer extends ShapeRender {

    private Queue<ShapeRender> renderQueue = new ArrayBlockingQueue<>(1);
    private int width;
    private int height;
    private ShapeRender mCurRender;
    private GL10 gl;
    private EGLConfig config;

    public ShapeContainer(Context context) {
        super(context);
    }

    public void setRenderer(ShapeRender renderer) {
        renderQueue.offer(renderer);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        this.gl = gl;
        this.config = config;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        ShapeRender render = renderQueue.poll();
        if (render != null) {
            render.onSurfaceCreated(gl, config);
            render.onSurfaceChanged(gl, width, height);
            render.onDrawFrame(gl);
            this.mCurRender = render;
            return;
        }

        if (this.mCurRender != null) {
            this.mCurRender.onDrawFrame(gl);
        }
    }
}
